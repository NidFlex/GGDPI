package com.ggdpi.app.utils

object Constants {
    const val VPN_INTERFACE_MTU = 1400
    const val VPN_INTERFACE_ADDRESS = "10.200.200.1"
    const val VPN_INTERFACE_PREFIX = 24
    
    const val BUFFER_SIZE = 32767
    const val PACKET_QUEUE_SIZE = 1024
    
    // Instagram CDN ranges (Facebook)
    val INSTAGRAM_IP_RANGES = listOf(
        "157.240.0.0/16",
        "31.13.64.0/18",
        "69.171.224.0/19",
        "66.220.144.0/20",
        "204.15.20.0/22",
        "173.252.64.0/19",
        "179.60.192.0/22",
        "185.60.216.0/22",
        "199.201.64.0/22"
    )
    
    // Google/YouTube ranges
    val GOOGLE_IP_RANGES = listOf(
        "172.217.0.0/16",
        "216.58.192.0/19",
        "142.250.0.0/15",
        "74.125.0.0/16",
        "108.177.0.0/17",
        "172.253.0.0/16",
        "209.85.128.0/17"
    )
    
    // Telegram ranges
    val TELEGRAM_IP_RANGES = listOf(
        "149.154.160.0/20",
        "149.154.176.0/20",
        "91.108.4.0/22",
        "91.108.8.0/21",
        "91.108.16.0/21",
        "91.108.56.0/22",
        "95.161.64.0/20",
        "2001:67c:4e8::/48",
        "2001:b28:f23d::/48",
        "2001:b28:f23f::/48"
    )
    
    // Discord ranges (ASN AS64259, AS13335)
    val DISCORD_IP_RANGES = listOf(
        "104.16.0.0/12",
        "162.159.0.0/16",
        "172.64.0.0/13"
    )
    
    // Common SNI patterns
    val INSTAGRAM_SNIS = listOf(
        "instagram.com",
        "www.instagram.com",
        "i.instagram.com",
        "graph.instagram.com",
        "cdninstagram.com",
        "fbcdn.net",
        "facebook.com",
        "fb.com"
    )
    
    val YOUTUBE_SNIS = listOf(
        "youtube.com",
        "www.youtube.com",
        "youtu.be",
        "googlevideo.com",
        "ytimg.com",
        "ggpht.com",
        "youtube-nocookie.com",
        "yt.be"
    )
    
    val DISCORD_SNIS = listOf(
        "discord.com",
        "discord.gg",
        "discordapp.com",
        "discord.media",
        "discordapp.net",
        "discordstatus.com",
        "discord.co"
    )
    
    val TELEGRAM_SNIS = listOf(
        "telegram.org",
        "telegram.com",
        "t.me",
        "api.telegram.org",
        "core.telegram.org",
        "my.telegram.org",
        "web.telegram.org",
        "webk.telegram.org"
    )
    
    // Default ports
    const val PORT_HTTP = 80
    const val PORT_HTTPS = 443
    const val PORT_HTTP_ALT = 8080
    const val PORT_HTTPS_ALT = 8443
    
    // Timeouts
    const val CONNECTION_TIMEOUT_MS = 30000
    const val READ_TIMEOUT_MS = 30000
    
    // Buffer pool settings
    const val BUFFER_POOL_SIZE = 10
    const val BUFFER_POOL_MAX_SIZE = 20
}