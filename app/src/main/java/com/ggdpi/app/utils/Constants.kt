// app/src/main/java/com/ggdpi/app/utils/Constants.kt
package com.ggdpi.app.utils

object Constants {
    // VPN настройки
    const val VPN_ADDRESS = "10.0.0.1"
    const val VPN_SUBNET_PREFIX = 24
    const val DEFAULT_MTU = 1500
    const val DEFAULT_DNS = "8.8.8.8"

    // Списки доменов для детекции
    val YOUTUBE_SNIS = listOf(
        "youtube.com", "googlevideo.com", "ytimg.com",
        "youtube-nocookie.com", "youtu.be", "m.youtube.com"
    )

    val DISCORD_SNIS = listOf(
        "discord.com", "discord.gg", "gateway.discord.gg",
        "cdn.discordapp.com", "discordapp.net", "discord.media"
    )

    val TELEGRAM_SNIS = listOf(
        "telegram.org", "t.me", "cdn-telegram.org",
        "web.telegram.org", "core.telegram.org"
    )

    val INSTAGRAM_SNIS = listOf(
        "instagram.com", "cdninstagram.com", "instagram.c10r.facebook.com"
    )

    // IP-диапазоны (CIDR)
    val GOOGLE_IP_RANGES = listOf(
        "172.217.0.0/16", "142.250.0.0/15", "216.58.192.0/19",
        "172.253.0.0/16", "74.125.0.0/16"
    )

    val DISCORD_IP_RANGES = listOf(
        "162.159.128.0/17", "104.16.0.0/12", "172.64.0.0/13"
    )

    val TELEGRAM_IP_RANGES = listOf(
        "149.154.160.0/20", "91.108.4.0/22", "5.28.160.0/18"
    )
}