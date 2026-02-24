package com.ggdpi.app.dpibypass.handlers

import com.ggdpi.app.core.StrategyManager
import com.ggdpi.app.utils.PacketUtils
import timber.log.Timber

class InstagramHandler {

    fun process(packet: ByteArray, length: Int, strategy: StrategyManager.DpiStrategy): ByteArray {
        val ipHeaderLen = (packet[0].toInt() and 0xF) * 4
        val tcpHeaderLen = ((packet[ipHeaderLen + 12].toInt() shr 4) and 0xF) * 4
        val payloadOffset = ipHeaderLen + tcpHeaderLen
        
        if (payloadOffset >= length) return packet
        
        val dstPort = PacketUtils.parsePort(packet, ipHeaderLen + 2)
        
        return when (dstPort) {
            443 -> processHttps(packet, length, ipHeaderLen, tcpHeaderLen, payloadOffset, strategy)
            80 -> processHttp(packet, length, ipHeaderLen, tcpHeaderLen, payloadOffset, strategy)
            else -> packet
        }
    }

    private fun processHttps(
        packet: ByteArray,
        length: Int,
        ipHeaderLen: Int,
        tcpHeaderLen: Int,
        payloadOffset: Int,
        strategy: StrategyManager.DpiStrategy
    ): ByteArray {
        if (!isTlsClientHello(packet, payloadOffset)) {
            return packet
        }

        Timber.d("Processing Instagram HTTPS packet")

        val payload = packet.copyOfRange(payloadOffset, length)
        
        val fragmented = applySniFragmentation(payload, strategy)
        val withFakeSni = strategy.fakeSni?.let { applyFakeSni(fragmented, it) } ?: fragmented
        val splitRecord = applyRecordSplitting(withFakeSni, strategy)
        
        return applyTcpSegmentation(packet, length, ipHeaderLen, tcpHeaderLen, splitRecord, strategy)
    }

    private fun applySniFragmentation(payload: ByteArray, strategy: StrategyManager.DpiStrategy): ByteArray {
        val sniOffset = findSniExtension(payload)
        if (sniOffset < 0) return payload
        
        val sniLength = ((payload[sniOffset + 2].toInt() and 0xFF) shl 8) or 
                       (payload[sniOffset + 3].toInt() and 0xFF)
        
        if (sniLength < 10) return payload
        
        val splitPoint = sniLength / 2
        
        val modified = payload.copyOf()
        modified[sniOffset + 2] = ((splitPoint shr 8) and 0xFF).toByte()
        modified[sniOffset + 3] = (splitPoint and 0xFF).toByte()
        
        return modified
    }

    private fun applyFakeSni(payload: ByteArray, fakeHostname: String): ByteArray {
        val sniOffset = findSniExtension(payload)
        if (sniOffset < 0) return payload
        
        val hostnameBytes = fakeHostname.toByteArray()
        val newSniLength = hostnameBytes.size + 5
        
        val newExtension = ByteArray(newSniLength + 4).apply {
            this[0] = 0x00
            this[1] = 0x00
            this[2] = ((newSniLength shr 8) and 0xFF).toByte()
            this[3] = (newSniLength and 0xFF).toByte()
            this[4] = ((newSniLength - 2) shr 8 and 0xFF).toByte()
            this[5] = ((newSniLength - 2) and 0xFF).toByte()
            this[6] = 0x00
            this[7] = ((hostnameBytes.size shr 8) and 0xFF).toByte()
            this[8] = (hostnameBytes.size and 0xFF).toByte()
            System.arraycopy(hostnameBytes, 0, this, 9, hostnameBytes.size)
        }
        
        val result = ByteArray(payload.size - findSniLength(payload, sniOffset) + newExtension.size)
        System.arraycopy(payload, 0, result, 0, sniOffset)
        System.arraycopy(newExtension, 0, result, sniOffset, newExtension.size)
        System.arraycopy(
            payload, sniOffset + findSniLength(payload, sniOffset),
            result, sniOffset + newExtension.size,
            payload.size - sniOffset - findSniLength(payload, sniOffset)
        )
        
        return result
    }

    private fun applyRecordSplitting(payload: ByteArray, strategy: StrategyManager.DpiStrategy): ByteArray {
        if (payload.size < 10) return payload
        
        val recordLength = ((payload[3].toInt() and 0xFF) shl 8) or 
                          (payload[4].toInt() and 0xFF)
        
        if (recordLength < 100) return payload
        
        val splitPoint = 50
        
        val firstRecord = payload.copyOfRange(0, 5 + splitPoint)
        val secondRecord = byteArrayOf(
            0x14, 0x03, 0x03,
            ((payload.size - 5 - splitPoint) shr 8).toByte(),
            ((payload.size - 5 - splitPoint) and 0xFF).toByte()
        ) + payload.copyOfRange(5 + splitPoint, payload.size)
        
        return firstRecord + secondRecord
    }

    private fun applyTcpSegmentation(
        originalPacket: ByteArray,
        length: Int,
        ipHeaderLen: Int,
        tcpHeaderLen: Int,
        payload: ByteArray,
        strategy: StrategyManager.DpiStrategy
    ): ByteArray {
        if (payload.size < 100) {
            return rebuildPacket(originalPacket, length, ipHeaderLen, tcpHeaderLen, payload)
        }
        
        val splitPos = payload.size / 2
        val segment2 = payload.copyOfRange(splitPos, payload.size)
        val segment1 = payload.copyOfRange(0, splitPos)
        
        val modifiedPacket = rebuildPacket(originalPacket, length, ipHeaderLen, tcpHeaderLen, segment2 + segment1)
        
        val seqOffset = ipHeaderLen + 4
        val originalSeq = ((modifiedPacket[seqOffset].toInt() and 0xFF) shl 24) or
                         ((modifiedPacket[seqOffset + 1].toInt() and 0xFF) shl 16) or
                         ((modifiedPacket[seqOffset + 2].toInt() and 0xFF) shl 8) or
                         (modifiedPacket[seqOffset + 3].toInt() and 0xFF)
        
        val newSeq = originalSeq + splitPos
        modifiedPacket[seqOffset] = ((newSeq shr 24) and 0xFF).toByte()
        modifiedPacket[seqOffset + 1] = ((newSeq shr 16) and 0xFF).toByte()
        modifiedPacket[seqOffset + 2] = ((newSeq shr 8) and 0xFF).toByte()
        modifiedPacket[seqOffset + 3] = (newSeq and 0xFF).toByte()
        
        return modifiedPacket
    }

    private fun processHttp(
        packet: ByteArray,
        length: Int,
        ipHeaderLen: Int,
        tcpHeaderLen: Int,
        payloadOffset: Int,
        strategy: StrategyManager.DpiStrategy
    ): ByteArray {
        val payload = packet.copyOfRange(payloadOffset, length)
        val payloadStr = String(payload, Charsets.UTF_8)
        
        if (!payloadStr.startsWith("GET ") && !payloadStr.startsWith("POST ")) {
            return packet
        }
        
        var modified = payloadStr
        modified = modified.replace("Host:", "host:")
        modified = modified.replace("host: ", "host:")
        modified = "\r\n" + modified
        modified = modified.replace("HTTP/1.1", "  HTTP/1.1")
        
        return rebuildPacket(packet, length, ipHeaderLen, tcpHeaderLen, modified.toByteArray())
    }

    private fun isTlsClientHello(packet: ByteArray, offset: Int): Boolean {
        if (offset + 6 > packet.size) return false
        return packet[offset] == 0x16.toByte() && 
               packet[offset + 1] == 0x03.toByte() &&
               packet[offset + 5] == 0x01.toByte()
    }

    private fun findSniExtension(payload: ByteArray): Int {
        if (payload.size < 43) return -1
        
        var offset = 43
        val sessionIdLen = payload[offset++].toInt() and 0xFF
        offset += sessionIdLen
        
        if (offset + 2 > payload.size) return -1
        val cipherSuitesLen = ((payload[offset].toInt() and 0xFF) shl 8) or 
                             (payload[offset + 1].toInt() and 0xFF)
        offset += 2 + cipherSuitesLen
        
        if (offset >= payload.size) return -1
        val compressionLen = payload[offset++].toInt() and 0xFF
        offset += compressionLen
        
        if (offset + 2 > payload.size) return -1
        val extensionsLen = ((payload[offset].toInt() and 0xFF) shl 8) or 
                           (payload[offset + 1].toInt() and 0xFF)
        offset += 2
        
        val endOffset = offset + extensionsLen
        
        while (offset < endOffset) {
            if (offset + 4 > payload.size) return -1
            val extType = ((payload[offset].toInt() and 0xFF) shl 8) or 
                         (payload[offset + 1].toInt() and 0xFF)
            val extLen = ((payload[offset + 2].toInt() and 0xFF) shl 8) or 
                        (payload[offset + 3].toInt() and 0xFF)
            
            if (extType == 0x0000) return offset
            
            offset += 4 + extLen
        }
        
        return -1
    }

    private fun findSniLength(payload: ByteArray, offset: Int): Int {
        if (offset + 4 > payload.size) return 0
        val extLen = ((payload[offset + 2].toInt() and 0xFF) shl 8) or 
                    (payload[offset + 3].toInt() and 0xFF)
        return 4 + extLen
    }

    private fun rebuildPacket(
        original: ByteArray,
        length: Int,
        ipHeaderLen: Int,
        tcpHeaderLen: Int,
        newPayload: ByteArray
    ): ByteArray {
        val newLength = ipHeaderLen + tcpHeaderLen + newPayload.size
        val newPacket = ByteArray(newLength)
        
        System.arraycopy(original, 0, newPacket, 0, ipHeaderLen)
        System.arraycopy(original, ipHeaderLen, newPacket, ipHeaderLen, tcpHeaderLen)
        System.arraycopy(newPayload, 0, newPacket, ipHeaderLen + tcpHeaderLen, newPayload.size)
        
        newPacket[2] = ((newLength shr 8) and 0xFF).toByte()
        newPacket[3] = (newLength and 0xFF).toByte()
        newPacket[10] = 0
        newPacket[11] = 0
        newPacket[ipHeaderLen + 16] = 0
        newPacket[ipHeaderLen + 17] = 0
        
        return newPacket
    }
    // В каждый handler добавить те же три метода-заглушки:
    fun process(packet: ByteArray, length: Int, strategy: DpiStrategy): ByteArray { return packet }
    fun processTcp(packet: ByteArray, length: Int, strategy: DpiStrategy, sni: String?): ByteArray { return packet }
    fun processUdp(packet: ByteArray, length: Int, strategy: DpiStrategy): ByteArray { return packet }
}