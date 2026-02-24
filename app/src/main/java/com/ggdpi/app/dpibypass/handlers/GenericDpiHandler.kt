// app/src/main/java/com/ggdpi/app/dpibypass/handlers/GenericDpiHandler.kt
package com.ggdpi.app.dpibypass.handlers

import com.ggdpi.app.core.StrategyManager.DpiStrategy
import com.ggdpi.app.utils.PacketUtils
import timber.log.Timber

class GenericDpiHandler {

    fun processTcp(packet: ByteArray, length: Int, strategy: DpiStrategy, sni: String?): ByteArray {
        // Базовая обработка: применяем стратегию если нужно
        // Для generic пакетов обычно не требуется модификация
        return packet
    }

    fun processUdp(packet: ByteArray, length: Int, strategy: DpiStrategy): ByteArray {
        // Обработка UDP: TTL manipulation если указано в стратегии
        if (strategy.autoTtl != null && strategy.autoTtl!!.isNotEmpty()) {
            val ipHeaderLen = (packet[0].toInt() and 0xF) * 4
            if (length > ipHeaderLen + 8) {  // Минимум IP + UDP header
                val currentTtl = packet[8].toInt() and 0xFF
                val newTtl = parseTtlValue(strategy.autoTtl!!, currentTtl)

                if (newTtl != currentTtl) {
                    packet[8] = newTtl.toByte()
                    PacketUtils.nativeUpdateIpChecksum(packet, ipHeaderLen)
                }
            }
        }
        return packet
    }

    private fun parseTtlValue(ttlSpec: String, default: Int): Int {
        return try {
            when {
                ttlSpec.contains(":") -> {
                    // Формат "base:variance" например "64:10"
                    val parts = ttlSpec.split(":")
                    val base = parts[0].toIntOrNull() ?: default
                    val variance = parts[1].substringBefore("-").toIntOrNull() ?: 0
                    (base + (Math.random() * variance).toInt()).coerceIn(1, 255)
                }
                else -> ttlSpec.toIntOrNull() ?: default
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to parse TTL spec: $ttlSpec")
            default
        }
    }
}