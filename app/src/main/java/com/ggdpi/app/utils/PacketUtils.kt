package com.ggdpi.app.utils

import java.net.InetAddress

object PacketUtils {

    // Native methods
    external fun nativeUpdateIpChecksum(packet: ByteArray, headerLen: Int)
    external fun nativeUpdateTcpChecksum(packet: ByteArray, ipHeaderLen: Int, tcpHeaderLen: Int, payloadLen: Int)

    // ✅ НОВЫЙ МЕТОД: извлечение порта из TCP/UDP заголовка
    fun parsePort(packet: ByteArray, offset: Int): Int {
        if (offset + 1 >= packet.size) return 0
        return ((packet[offset].toInt() and 0xFF) shl 8) or
                (packet[offset + 1].toInt() and 0xFF)
    }

    // ✅ ИСПРАВЛЕННЫЙ МЕТОД: проверка IP в CIDR
    fun isInSubnet(ip: String, cidr: String): Boolean {
        return try {
            val parts = cidr.split("/")
            val subnetAddress = InetAddress.getByName(parts[0])
            val prefixLength = parts[1].toInt()

            val ipBytes = InetAddress.getByName(ip).address
            val subnetBytes = subnetAddress.address

            if (ipBytes.size != subnetBytes.size) return false

            // Создаём маску
            val mask = ByteArray(ipBytes.size)
            var bits = prefixLength
            for (i in mask.indices) {
                val bitsInByte = minOf(8, bits)
                mask[i] = (if (bitsInByte == 8) -1 else (-1 shl (8 - bitsInByte))).toByte()
                bits -= bitsInByte
            }

            // Сравниваем побайтово
            for (i in ipBytes.indices) {
                if ((ipBytes[i].toInt() and mask[i].toInt()) != (subnetBytes[i].toInt() and mask[i].toInt())) {
                    return false
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun createIPv4Mask(prefixLength: Int): IntArray {
        val mask = IntArray(4)
        var bits = prefixLength
        for (i in 0 until 4) {
            val bitsInOctet = minOf(8, bits)
            mask[i] = if (bitsInOctet == 8) 0xFF else (0xFF shl (8 - bitsInOctet)) and 0xFF
            bits -= bitsInOctet
        }
        return mask
    }

    private fun createIPv6Mask(prefixLength: Int): IntArray {
        val mask = IntArray(16)
        var bits = prefixLength
        for (i in 0 until 16) {
            val bitsInOctet = minOf(8, bits)
            mask[i] = if (bitsInOctet == 8) 0xFF else (0xFF shl (8 - bitsInOctet)) and 0xFF
            bits -= bitsInOctet
        }
        return mask
    }

    // Helper для MatchGroup
    fun MatchGroup?.toLongOrNull(default: Long = 0): Long {
        return this?.value?.toLongOrNull() ?: default
    }
}