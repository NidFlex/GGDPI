package com.ggdpi.app.dpibypass.handlers

import com.ggdpi.app.core.StrategyManager
import com.ggdpi.app.utils.PacketUtils

class YoutubeHandler {

    fun process(packet: ByteArray, length: Int, strategy: StrategyManager.DpiStrategy): ByteArray {
        val ipHeaderLen = (packet[0].toInt() and 0xF) * 4
        val tcpHeaderLen = ((packet[ipHeaderLen + 12].toInt() shr 4) and 0xF) * 4
        val payloadOffset = ipHeaderLen + tcpHeaderLen
        
        if (payloadOffset >= length) return packet
        
        val payload = packet.copyOfRange(payloadOffset, length)
        
        val modifiedPayload = applyMultisplit(payload, strategy)
        
        return rebuildPacket(packet, length, ipHeaderLen, tcpHeaderLen, modifiedPayload)
    }

    private fun applyMultisplit(payload: ByteArray, strategy: StrategyManager.DpiStrategy): ByteArray {
        if (payload.size < 100) return payload
        
        val splitPositions = listOf(1, 50)
        
        var result = payload
        for (pos in splitPositions.sortedDescending()) {
            if (pos < result.size) {
                result = result.copyOfRange(0, pos) + 
                        byteArrayOf(0x16, 0x03, 0x01, 0x00, 0x00) +
                        result.copyOfRange(pos, result.size)
            }
        }
        
        return result
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
}