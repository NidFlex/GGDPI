package com.ggdpi.app.utils

object PacketUtils {

    fun parseIPv4Address(packet: ByteArray, offset: Int): String {
        return "${packet[offset].toInt() and 0xFF}.${packet[offset + 1].toInt() and 0xFF}." +
               "${packet[offset + 2].toInt() and 0xFF}.${packet[offset + 3].toInt() and 0xFF}"
    }

    fun parsePort(packet: ByteArray, offset: Int): Int {
        return ((packet[offset].toInt() and 0xFF) shl 8) or 
               (packet[offset + 1].toInt() and 0xFF)
    }

    fun isInSubnet(ip: String, cidr: String): Boolean {
        val (subnet, bits) = cidr.split("/")
        val mask = (0xFFFFFFFF shl (32 - bits.toInt())) and 0xFFFFFFFF
        
        val ipLong = ipToLong(ip)
        val subnetLong = ipToLong(subnet)
        
        return (ipLong and mask) == (subnetLong and mask)
    }

    private fun ipToLong(ip: String): Long {
        return ip.split(".").fold(0L) { acc, octet ->
            (acc shl 8) or octet.toLong()
        }
    }

    fun updateIpChecksum(packet: ByteArray, headerLen: Int) {
        packet[10] = 0
        packet[11] = 0
        
        var sum = 0
        for (i in 0 until headerLen step 2) {
            sum += ((packet[i].toInt() and 0xFF) shl 8) or 
                   (packet[i + 1].toInt() and 0xFF)
        }
        
        while (sum shr 16 != 0) {
            sum = (sum and 0xFFFF) + (sum shr 16)
        }
        
        val checksum = sum.inv() and 0xFFFF
        packet[10] = (checksum shr 8).toByte()
        packet[11] = (checksum and 0xFF).toByte()
    }
}