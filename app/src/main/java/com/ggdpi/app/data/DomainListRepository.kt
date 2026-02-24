// app/src/main/java/com/ggdpi/app/data/DomainListRepository.kt
package com.ggdpi.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import com.ggdpi.app.utils.PacketUtils  // Для isInSubnet

class DomainListRepository(private val context: Context) {

    companion object {
        private const val ASSETS_PATH = "lists"

        private val DEFAULT_DOMAINS = mapOf(
            "youtube" to listOf(
                "youtube.com", "googlevideo.com", "ytimg.com",
                "youtube-nocookie.com", "youtu.be"
            ),
            "discord" to listOf(
                "discord.com", "discord.gg", "gateway.discord.gg",
                "cdn.discordapp.com", "discordapp.net"
            ),
            "telegram" to listOf(
                "telegram.org", "t.me", "cdn-telegram.org",
                "web.telegram.org", "core.telegram.org"
            )
        )

        private val DEFAULT_IP_RANGES = mapOf(
            "google" to listOf("172.217.0.0/16", "142.250.0.0/15", "216.58.192.0/19"),
            "discord" to listOf("162.159.128.0/17", "104.16.0.0/12"),
            "telegram" to listOf("149.154.160.0/20", "91.108.4.0/22")
        )
    }

    suspend fun loadInitialLists(): DomainLists {
        return withContext(Dispatchers.IO) {
            val domains = mutableMapOf<String, List<String>>()
            val ipRanges = mutableMapOf<String, List<String>>()

            try {
                context.assets.list(ASSETS_PATH)?.forEach { fileName ->
                    val content = context.assets.open("$ASSETS_PATH/$fileName")
                        .bufferedReader()
                        .use { it.readText() }

                    val key = fileName.removeSuffix(".txt")
                    val entries = content.lines()
                        .map { it.trim() }
                        .filter { it.isNotEmpty() && !it.startsWith("#") }

                    if (entries.firstOrNull()?.contains(".") == true &&
                        entries.firstOrNull()?.contains("/") == true) {
                        ipRanges[key] = entries
                    } else {
                        domains[key] = entries
                    }
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to load lists from assets, using defaults")
            }

            DomainLists(
                domains = domains.ifEmpty { DEFAULT_DOMAINS },
                ipRanges = ipRanges.ifEmpty { DEFAULT_IP_RANGES }
            )
        }
    }

    fun isInTargetList(ip: String, service: String): Boolean {
        val ranges = DEFAULT_IP_RANGES[service] ?: return false
        return ranges.any { PacketUtils.isInSubnet(ip, it) }
    }

    fun matchesSni(sni: String?, service: String): Boolean {
        if (sni == null) return false
        val domains = DEFAULT_DOMAINS[service] ?: return false
        return domains.any { sni.endsWith(it) || sni.contains(".$it") }
    }
}

data class DomainLists(
    val domains: Map<String, List<String>>,
    val ipRanges: Map<String, List<String>>
)