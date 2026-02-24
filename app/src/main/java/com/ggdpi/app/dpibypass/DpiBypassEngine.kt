package com.ggdpi.app.dpibypass

import android.os.ParcelFileDescriptor
import com.ggdpi.app.core.StrategyManager
import com.ggdpi.app.data.LogRepository
import com.ggdpi.app.dpibypass.handlers.*
import com.ggdpi.app.dpibypass.native.NativeDpiUtils
import com.ggdpi.app.ui.screens.LogEntry
import com.ggdpi.app.utils.PacketUtils
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import com.ggdpi.app.utils.Constants

class DpiBypassEngine(
    private val vpnInterface: ParcelFileDescriptor,
    private val strategyManager: StrategyManager,
    private val logRepository: LogRepository? = null,
    private val scope: CoroutineScope,
    private val onPacketProcessed: (Long) -> Unit
) {
    private val inputStream = FileInputStream(vpnInterface.fileDescriptor)
    private val outputStream = FileOutputStream(vpnInterface.fileDescriptor)
    private val executor = Executors.newSingleThreadExecutor()
    private val bufferPool = ByteBufferPool(10, 32767)
    
    private var isRunning = false
    private val packetCount = AtomicLong(0)
    private val bypassCount = AtomicLong(0)
    
    private val instagramHandler = InstagramHandler()
    private val youtubeHandler = YoutubeHandler()
    private val discordHandler = DiscordHandler()
    private val telegramHandler = TelegramHandler()
    private val genericHandler = GenericDpiHandler()
    
    private val nativeUtils = NativeDpiUtils()

    fun start() {
        isRunning = true
        logRepository?.addLog("DPI Bypass Engine started", LogEntry.LogType.SUCCESS)
        
        scope.launch(executor.asCoroutineDispatcher()) {
            processPackets()
        }
    }

    fun stop() {
        isRunning = false
        executor.shutdown()
        try {
            inputStream.close()
            outputStream.close()
        } catch (e: Exception) {
            Timber.w(e, "Error closing streams")
        }
        logRepository?.addLog("DPI Bypass Engine stopped", LogEntry.LogType.INFO)
    }

    private suspend fun processPackets() {
        val buffer = bufferPool.acquire()
        
        try {
            while (isRunning && scope.isActive) {
                try {
                    val length = withContext(Dispatchers.IO) {
                        inputStream.read(buffer.array())
                    }
                    
                    if (length > 0) {
                        buffer.limit(length)
                        
                        val processed = processPacket(buffer, length)
                        
                        withContext(Dispatchers.IO) {
                            outputStream.write(processed, 0, processed.size)
                        }
                        
                        val count = packetCount.incrementAndGet()
                        if (count % 100 == 0L) {
                            onPacketProcessed(count)
                        }
                        
                        buffer.clear()
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Packet processing error")
                    logRepository?.addLog("Error: ${e.message}", LogEntry.LogType.ERROR)
                    buffer.clear()
                }
            }
        } finally {
            bufferPool.release(buffer)
        }
    }

    private fun processPacket(buffer: ByteBuffer, length: Int): ByteArray {
        val packet = ByteArray(length)
        buffer.get(packet)
        
        val version = (packet[0].toInt() shr 4) and 0xF
        
        return when (version) {
            4 -> processIPv4(packet, length)
            6 -> processIPv6(packet, length)
            else -> packet
        }
    }

    private fun processIPv4(packet: ByteArray, length: Int): ByteArray {
        if (length < 20) return packet
        
        val protocol = packet[9].toInt() and 0xFF
        val service = detectService(packet, length)
        val strategy = strategyManager.getCurrentStrategy()

        val processed = when (protocol) {
            6 -> {
                when (service) {
                    Service.INSTAGRAM -> {
                        bypassCount.incrementAndGet()
                        instagramHandler.process(packet, length, strategy)
                    }
                    Service.YOUTUBE -> {
                        bypassCount.incrementAndGet()
                        youtubeHandler.process(packet, length, strategy)
                    }
                    Service.DISCORD -> {
                        bypassCount.incrementAndGet()
                        discordHandler.process(packet, length, strategy)
                    }
                    Service.TELEGRAM -> {
                        bypassCount.incrementAndGet()
                        telegramHandler.process(packet, length, strategy)
                    }
                    else -> genericHandler.processTcp(packet, length, strategy)
                }
            }
            17 -> {
                when (service) {
                    Service.DISCORD_VOICE -> discordHandler.processUdp(packet, length, strategy)
                    Service.TELEGRAM_VOICE -> telegramHandler.processUdp(packet, length, strategy)
                    else -> genericHandler.processUdp(packet, length, strategy)
                }
            }
            else -> packet
        }
        
        if (processed !== packet) {
            logRepository?.addLog(
                "Bypassed ${service.name} packet with ${strategy.displayName} strategy",
                LogEntry.LogType.SUCCESS
            )
        }
        
        return processed
    }

    private fun processIPv6(packet: ByteArray, length: Int): ByteArray {
        return packet
    }

    private fun detectService(packet: ByteArray, length: Int): Service {
        val ipHeaderLen = (packet[0].toInt() and 0xF) * 4
        val dstPort = PacketUtils.parsePort(packet, ipHeaderLen + 2)
        val dstIp = PacketUtils.parseIPv4Address(packet, 16)
        
        return when {
            isInstagramIp(dstIp) || isInstagramSni(packet, length) -> Service.INSTAGRAM
            isGoogleIp(dstIp) || isYoutubeSni(packet, length) -> Service.YOUTUBE
            isDiscordSni(packet, length) -> Service.DISCORD
            isDiscordVoicePort(dstPort) -> Service.DISCORD_VOICE
            isTelegramIp(dstIp) || isTelegramSni(packet, length) -> Service.TELEGRAM
            isTelegramVoicePort(dstPort) -> Service.TELEGRAM_VOICE
            else -> Service.UNKNOWN
        }
    }

    private fun isInstagramIp(ip: String): Boolean {
        return Constants.INSTAGRAM_IP_RANGES.any { PacketUtils.isInSubnet(ip, it) }
    }

    private fun isInstagramSni(packet: ByteArray, length: Int): Boolean {
        val sni = extractSni(packet, length)
        return sni?.let { host ->
            Constants.INSTAGRAM_SNIS.any { host.contains(it) }
        } ?: false
    }

    private fun isGoogleIp(ip: String): Boolean {
        return Constants.GOOGLE_IP_RANGES.any { PacketUtils.isInSubnet(ip, it) }
    }

    private fun isYoutubeSni(packet: ByteArray, length: Int): Boolean {
        val sni = extractSni(packet, length)
        return sni?.let { host ->
            Constants.YOUTUBE_SNIS.any { host.contains(it) }
        } ?: false
    }

    private fun isDiscordSni(packet: ByteArray, length: Int): Boolean {
        val sni = extractSni(packet, length)
        return sni?.let { host ->
            Constants.DISCORD_SNIS.any { host.contains(it) }
        } ?: false
    }

    private fun isDiscordVoicePort(port: Int): Boolean {
        return port in 5000..65535
    }

    private fun isTelegramIp(ip: String): Boolean {
        return Constants.TELEGRAM_IP_RANGES.any { PacketUtils.isInSubnet(ip, it) }
    }

    private fun isTelegramSni(packet: ByteArray, length: Int): Boolean {
        val sni = extractSni(packet, length)
        return sni?.let { host ->
            Constants.TELEGRAM_SNIS.any { host.contains(it) }
        } ?: false
    }

    private fun isTelegramVoicePort(port: Int): Boolean {
        return port in 10000..65000
    }

    private fun extractSni(packet: ByteArray, length: Int): String? {
        val ipHeaderLen = (packet[0].toInt() and 0xF) * 4
        val tcpHeaderLen = ((packet[ipHeaderLen + 12].toInt() shr 4) and 0xF) * 4
        val payloadOffset = ipHeaderLen + tcpHeaderLen
        
        return nativeUtils.extractSni(packet, payloadOffset, length - payloadOffset)
    }

    fun getStats(): EngineStats {
        return EngineStats(
            totalPackets = packetCount.get(),
            bypassedPackets = bypassCount.get(),
            currentStrategy = strategyManager.getCurrentStrategy().displayName
        )
    }

    data class EngineStats(
        val totalPackets: Long,
        val bypassedPackets: Long,
        val currentStrategy: String
    )

    enum class Service {
        INSTAGRAM, YOUTUBE, DISCORD, DISCORD_VOICE,
        TELEGRAM, TELEGRAM_VOICE, UNKNOWN
    }
}