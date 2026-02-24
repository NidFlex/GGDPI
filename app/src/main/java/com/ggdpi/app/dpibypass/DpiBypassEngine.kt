// app/src/main/java/com/ggdpi/app/dpibypass/DpiBypassEngine.kt
package com.ggdpi.app.dpibypass

import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.system.OsConstants
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong  // ← ДОБАВИТЬ
import com.ggdpi.app.core.StrategyManager
import com.ggdpi.app.core.StrategyManager.DpiStrategy
import com.ggdpi.app.data.LogEntry
import com.ggdpi.app.data.LogRepository
import com.ggdpi.app.dpibypass.handlers.*
import com.ggdpi.app.utils.Constants  // ← Без .kt!
import com.ggdpi.app.utils.NativeDpiUtils
import com.ggdpi.app.utils.PacketUtils
import com.ggdpi.app.utils.PacketUtils.toLongOrNull  // ← Для исправления MatchGroup ошибки

class DpiBypassEngine(
    private val vpnService: VpnService,
    private val strategyManager: StrategyManager,
    private val logRepository: LogRepository? = null
) {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var inputStream: FileInputStream? = null
    private var outputStream: FileOutputStream? = null

    @Volatile private var isRunning = false

    // Executor для обработки пакетов
    private val executor = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors().coerceAtLeast(2).coerceAtMost(4)
    )

    // Статистика
    private val processedPackets = AtomicLong(0)
    private val bypassedPackets = AtomicLong(0)

    // Handlers для разных сервисов
    private val youtubeHandler = YoutubeHandler()
    private val discordHandler = DiscordHandler()
    private val telegramHandler = TelegramHandler()
    private val genericHandler = GenericDpiHandler()

    fun start(vpnInterface: ParcelFileDescriptor, strategy: DpiStrategy) {
        if (isRunning) return

        this.vpnInterface = vpnInterface
        this.inputStream = FileInputStream(vpnInterface.fileDescriptor)
        this.outputStream = FileOutputStream(vpnInterface.fileDescriptor)

        isRunning = true
        logRepository?.addLog("DPI Bypass Engine started with strategy: ${strategy.name}", LogEntry.LogType.INFO)

        // Запускаем обработку в background
        executor.submit {
            processPackets(strategy)
        }
    }

    private fun processPackets(strategy: DpiStrategy) {
        val buffer = ByteArray(Constants.DEFAULT_MTU)

        try {
            while (isRunning) {
                val length = inputStream?.read(buffer) ?: break
                if (length <= 0) continue

                processedPackets.incrementAndGet()

                // Определяем протокол
                val protocol = NativeDpiUtils.getIpProtocol(buffer, length)

                when (protocol) {
                    OsConstants.IPPROTO_TCP -> {
                        processTcpPacket(buffer, length, strategy)
                    }
                    OsConstants.IPPROTO_UDP -> {
                        processUdpPacket(buffer, length, strategy)
                    }
                }

                // Отправляем пакет дальше
                outputStream?.write(buffer, 0, length)
            }
        } catch (e: IOException) {
            if (isRunning) {  // Только если это не штатная остановка
                logRepository?.addLog("Packet processing error: ${e.message}", LogEntry.LogType.ERROR)
                Timber.e(e, "Error processing packets")
            }
        }
    }

    private fun processTcpPacket(packet: ByteArray, length: Int, strategy: DpiStrategy) {
        // Проверяем TLS ClientHello для SNI extraction
        if (NativeDpiUtils.isTlsClientHello(packet, length)) {
            val sni = NativeDpiUtils.extractSni(packet, length)
            val service = detectService(sni, packet)

            when (service) {
                Service.YOUTUBE -> youtubeHandler.processTcp(packet, length, strategy, sni)
                Service.DISCORD -> discordHandler.processTcp(packet, length, strategy, sni)
                Service.TELEGRAM -> telegramHandler.processTcp(packet, length, strategy, sni)
                else -> genericHandler.processTcp(packet, length, strategy, sni)
            }

            if (service != Service.UNKNOWN) {
                bypassedPackets.incrementAndGet()
                logRepository?.addLog(
                    "Bypassed ${service.name} packet (SNI: $sni)",
                    LogEntry.LogType.BYPASS,
                    service.name
                )
            }
        } else {
            genericHandler.processTcp(packet, length, strategy, null)
        }

        // Пересчитываем checksum после модификаций
        val ipHeaderLen = (packet[0].toInt() and 0xF) * 4
        PacketUtils.nativeUpdateIpChecksum(packet, ipHeaderLen)

        // Для TCP нужен полный расчёт с pseudo-header (упрощённо)
        // В production здесь должен быть вызов nativeUpdateTcpChecksum с правильными параметрами
    }

    private fun processUdpPacket(packet: ByteArray, length: Int, strategy: DpiStrategy) {
        // Для UDP обычно не требуется DPI bypass, но обрабатываем TTL если нужно
        genericHandler.processUdp(packet, length, strategy)
    }

    private fun detectService(sni: String?, packet: ByteArray): Service {
        // Проверяем по SNI
        sni?.let {
            when {
                Constants.YOUTUBE_SNIS.any { domain -> sni.endsWith(domain) } -> return Service.YOUTUBE
                Constants.DISCORD_SNIS.any { domain -> sni.endsWith(domain) } -> return Service.DISCORD
                Constants.TELEGRAM_SNIS.any { domain -> sni.endsWith(domain) } -> return Service.TELEGRAM
                Constants.INSTAGRAM_SNIS.any { domain -> sni.endsWith(domain) } -> return Service.INSTAGRAM
            }
        }

        // Fallback: проверка по IP-адресам
        val (_, dstIp) = NativeDpiUtils.getIpAddresses(packet, packet.size)
        when {
            Constants.GOOGLE_IP_RANGES.any { PacketUtils.isInSubnet(dstIp, it) } -> return Service.YOUTUBE
            Constants.DISCORD_IP_RANGES.any { PacketUtils.isInSubnet(dstIp, it) } -> return Service.DISCORD
            Constants.TELEGRAM_IP_RANGES.any { PacketUtils.isInSubnet(dstIp, it) } -> return Service.TELEGRAM
        }

        return Service.UNKNOWN
    }

    fun stop() {
        isRunning = false

        // Graceful shutdown executor
        executor.shutdown()
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            executor.shutdownNow()
            Thread.currentThread().interrupt()
        }

        // Закрываем ресурсы
        try { inputStream?.close() } catch (e: IOException) { Timber.w(e, "Error closing input") }
        try { outputStream?.close() } catch (e: IOException) { Timber.w(e, "Error closing output") }
        try { vpnInterface?.close() } catch (e: IOException) { Timber.w(e, "Error closing VPN") }

        logRepository?.addLog(
            "Engine stopped. Processed: ${processedPackets.get()}, Bypassed: ${bypassedPackets.get()}",
            LogEntry.LogType.INFO
        )
    }

    enum class Service {
        YOUTUBE, DISCORD, TELEGRAM, INSTAGRAM, UNKNOWN
    }
}