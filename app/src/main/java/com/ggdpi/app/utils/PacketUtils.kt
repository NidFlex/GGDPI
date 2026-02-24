package com.ggdpi.app.utils

import java.net.Inet4Address
import java.net.InetAddress

object PacketUtils {

    // Native methods (реализация в C++)
    external fun nativeUpdateIpChecksum(packet: ByteArray, headerLen: Int)
    external fun nativeUpdateTcpChecksum(packet: ByteArray, ipHeaderLen: Int, tcpHeaderLen: Int, payloadLen: Int)

    // Kotlin helper: проверка IP в CIDR-диапазоне
    fun isInSubnet(ip: String, cidr: String): Boolean {
        return try {
            val parts = cidr.split("/")
            val subnetAddress = InetAddress.getByName(parts[0])
            val prefixLength = parts[1].toInt()

            val ipBytes = InetAddress.getByName(ip).address
            val subnetBytes = subnetAddress.address

            if (ipBytes.size != subnetBytes.size) return false

            val mask = when (ipBytes.size) {
                4 -> createIPv4Mask(prefixLength)  // IPv4
                16 -> createIPv6Mask(prefixLength)  // IPv6
                else -> return false
            }

            ipBytes.zip(subnetBytes).zip(mask).all { (ips, m) ->
                (ips.first.toInt() and 0xFF) and m == (ips.second.toInt() and 0xFF) and m
            }
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

    // Helper для извлечения значения из regex MatchGroup
    fun MatchGroup?.toLongOrNull(default: Long = 0): Long {
        return this?.value?.toLongOrNull() ?: default
    }
}