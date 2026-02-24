package com.ggdpi.app.utils

object NativeDpiUtils {
    init {
        System.loadLibrary("ggdpi-native")
    }

    // SNI extraction из TLS ClientHello
    external fun extractSni(packet: ByteArray, length: Int): String?

    // Определение типа пакета (TCP/UDP/ICMP)
    external fun getIpProtocol(packet: ByteArray, length: Int): Int

    // Извлечение IP-адресов
    external fun getIpAddresses(packet: ByteArray, length: Int): Pair<String, String>

    // Проверка, является ли пакет TLS ClientHello
    external fun isTlsClientHello(packet: ByteArray, length: Int): Boolean
}