package com.ggdpi.app.dpibypass.handlers

import com.ggdpi.app.core.StrategyManager
import com.ggdpi.app.utils.PacketUtils

class DiscordHandler {

    fun process(packet: ByteArray, length: Int, strategy: StrategyManager.DpiStrategy): ByteArray {
        val ipHeaderLen = (packet[0].toInt() and 0xF) * 4
        val tcpHeaderLen = ((packet[ipHeaderLen + 12].toInt() shr 4) and 0xF) * 4
        val payloadOffset = ipHeaderLen + tcpHeaderLen
        
        if (payloadOffset >= length) return packet
        
        val payload = packet.copyOfRange(payloadOffset, length)
        
        val modified = applyWsizeModification(payload, strategy)
        
        return rebuildPacket(packet, length, ipHeaderLen, tcpHeaderLen, modified)
    }

    fun processUdp(packet: ByteArray, length: Int, strategy: StrategyManager.DpiStrategy): ByteArray {
        val ipHeaderLen = (packet[0].toInt() and 0xF) * 4
        val udpHeaderLen = 8
        val payloadOffset = ipHeaderLen + udpHeaderLen
        
        if (payloadOffset >= length) return packet
        
        val payload = packet.copyOfRange(payloadOffset, length)
        
        val paddingSize = (5..20).random()
        val padding = ByteArray(paddingSize) { (0..255).random().toByte() }
        val modified = payload + byteArrayOf(0xDE.toByte(), 0xAD.toByte()) + padding
        
        return rebuildUdpPacket(packet, length, ipHeaderLen, modified)
    }

    private fun applyWsizeModification(payload: ByteArray, strategy: StrategyManager.DpiStrategy): ByteArray {
        return payload
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
        newPacket[ipHeaderLen + 6] = 0
        newPacket[ipHeaderLen + 7] = 0
        System.arraycopy(newPayload, 0, newPacket, ipHeaderLen + udpHeaderLen, newPayload.size)
        
        newPacket[2] = ((newLength shr 8) and 0xFF).toByte()
        newPacket[3] = (newLength and 0xFF).toByte()
        
        return newPacket
    }
}