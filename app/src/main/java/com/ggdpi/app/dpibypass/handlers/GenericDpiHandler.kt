package com.ggdpi.app.dpibypass.handlers

import com.ggdpi.app.core.StrategyManager
import com.ggdpi.app.utils.PacketUtils

class GenericDpiHandler {

    fun processTcp(packet: ByteArray, length: Int, strategy: StrategyManager.DpiStrategy): ByteArray {
        val ipHeaderLen = (packet[0].toInt() and 0xF) * 4
        val tcpHeaderLen = ((packet[ipHeaderLen + 12].toInt() shr 4) and 0xF) * 4
        val payloadOffset = ipHeaderLen + tcpHeaderLen
        
        if (payloadOffset >= length) return packet
        
        val dstPort = PacketUtils.parsePort(packet, ipHeaderLen + 2)
        val payload = packet.copyOfRange(payloadOffset, length)
        
        val modified = when (dstPort) {
            80 -> processHttp(payload, strategy)
            443 -> processHttps(payload, strategy)
            else -> payload
        }
        
        return rebuildPacket(packet, length, ipHeaderLen, tcpHeaderLen, modified)
    }

    fun processUdp(packet: ByteArray, length: Int, strategy: StrategyManager.DpiStrategy): ByteArray {
        return packet
    }

    private fun processHttp(payload: ByteArray, strategy: StrategyManager.DpiStrategy): ByteArray {
        val str = String(payload, Charsets.UTF_8)
        if (!str.startsWith("GET ") && !str.startsWith("POST ")) return payload
        
        var modified = str
        modified = modified.replace("Host:", "host:")
        modified = "\r\n" + modified
        
        return modified.toByteArray()
    }

    private fun processHttps(payload: ByteArray, strategy: StrategyManager.DpiStrategy): ByteArray {
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
}