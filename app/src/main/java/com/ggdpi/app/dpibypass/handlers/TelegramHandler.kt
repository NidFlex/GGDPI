package com.ggdpi.app.dpibypass.handlers

import com.ggdpi.app.core.StrategyManager
import com.ggdpi.app.utils.PacketUtils

class TelegramHandler {

    fun process(packet: ByteArray, length: Int, strategy: StrategyManager.DpiStrategy): ByteArray {
        val ipHeaderLen = (packet[0].toInt() and 0xF) * 4
        val tcpHeaderLen = ((packet[ipHeaderLen + 12].toInt() shr 4) and 0xF) * 4
        val payloadOffset = ipHeaderLen + tcpHeaderLen
        
        if (payloadOffset >= length) return packet
        
        val payload = packet.copyOfRange(payloadOffset, length)
        
        return if (isMtProto(payload)) {
            processMtProto(packet, length, ipHeaderLen, tcpHeaderLen, payload, strategy)
        } else {
            processHttps(packet, length, ipHeaderLen, tcpHeaderLen, payload, strategy)
        }
    }

    fun processUdp(packet: ByteArray, length: Int, strategy: StrategyManager.DpiStrategy): ByteArray {
        val ipHeaderLen = (packet[0].toInt() and 0xF) * 4
        val udpHeaderLen = 8
        val payloadOffset = ipHeaderLen + udpHeaderLen
        
        if (payloadOffset >= length) return packet
        
        val payload = packet.copyOfRange(payloadOffset, length)
        
        val modified = byteArrayOf(0x00, 0x00) + payload
        
        return rebuildUdpPacket(packet, length, ipHeaderLen, modified)
    }

    private fun isMtProto(payload: ByteArray): Boolean {
        if (payload.isEmpty()) return false
        return when (payload[0]) {
            0xEF.toByte() -> true
            0xEE.toByte() -> payload.size > 3 && payload[1] == 0xEE.toByte()
            else -> false
        }
    }

    private fun processMtProto(
        packet: ByteArray, length: Int, ipHeaderLen: Int, tcpHeaderLen: Int,
        payload: ByteArray, strategy: StrategyManager.DpiStrategy
    ): ByteArray {
        val fakeTls = createFakeTlsWrapper(payload)
        return rebuildPacket(packet, length, ipHeaderLen, tcpHeaderLen, fakeTls)
    }

    private fun processHttps(
        packet: ByteArray, length: Int, ipHeaderLen: Int, tcpHeaderLen: Int,
        payload: ByteArray, strategy: StrategyManager.DpiStrategy
    ): ByteArray {
        return rebuildPacket(packet, length, ipHeaderLen, tcpHeaderLen, payload)
    }

    private fun createFakeTlsWrapper(mtProtoPayload: ByteArray): ByteArray {
        val fakeTlsHeader = byteArrayOf(
            0x16, 0x03, 0x01, 0x00, 0x00,
            0x01, 0x00, 0x00, 0x00,
            0x03, 0x03
        )
        
        val random = ByteArray(32) { (0..255).random().toByte() }
        val sessionId = byteArrayOf(0x00)
        val cipherSuites = byteArrayOf(0x00, 0x02, 0x00, 0xFF.toByte())
        val compression = byteArrayOf(0x01, 0x00)
        
        val domain = "telegram.org"
        val sni = byteArrayOf(
            0x00, 0x00,
            0x00, (domain.length + 5).toByte(),
            0x00, (domain.length + 3).toByte(),
            0x00,
            0x00, domain.length.toByte()
        ) + domain.toByteArray()
        
        val handshake = fakeTlsHeader.drop(5).toByteArray() + random + sessionId + cipherSuites + compression +
                       byteArrayOf((sni.size shr 8).toByte(), sni.size.toByte()) + sni
        
        val recordLength = handshake.size
        val finalHeader = byteArrayOf(
            0x16, 0x03, 0x01,
            (recordLength shr 8).toByte(), recordLength.toByte()
        )
        
        return finalHeader + handshake + mtProtoPayload
    }

    private fun rebuildPacket(
        original: ByteArray, length: Int, ipHeaderLen: Int, tcpHeaderLen: Int, newPayload: ByteArray
    ): ByteArray {
        val newLength = ipHeaderLen + tcpHeaderLen + newPayload.size
        val newPacket = ByteArray(newLength)
        
        System.arraycopy(original, 0, newPacket, 0, ipHeaderLen)
        System.arraycopy(original, ipHeaderLen, newPacket, ipHeaderLen, tcpHeaderLen)
        System.arraycopy(newPayload, 0, newPacket, ipHeaderLen + tcpHeaderLen, newPayload.size)
        
        newPacket[2] = ((newLength shr 8) and 0xFF).toByte()
        newPacket[3] = (newLength and 0xFF).toByte()
        
        return newPacket
    }

    private fun rebuildUdpPacket(
        original: ByteArray, length: Int, ipHeaderLen: Int, newPayload: ByteArray
    ): ByteArray {
        val udpHeaderLen = 8
        val newLength = ipHeaderLen + udpHeaderLen + newPayload.size
        val newPacket = ByteArray(newLength)
        
        System.arraycopy(original, 0, newPacket, 0, ipHeaderLen)
        System.arraycopy(original, ipHeaderLen, newPacket, ipHeaderLen, 4)
        newPacket[ipHeaderLen + 4] = (((udpHeaderLen + newPayload.size) shr 8) and 0xFF).toByte()
        newPacket[ipHeaderLen + 5] = ((udpHeaderLen + newPayload.size) and 0xFF).toByte()
        System.arraycopy(newPayload, 0, newPacket, ipHeaderLen + udpHeaderLen, newPayload.size)
        
        newPacket[2] = ((newLength shr 8) and 0xFF).toByte()
        newPacket[3] = (newLength and 0xFF).toByte()
        
        return newPacket
    }
}