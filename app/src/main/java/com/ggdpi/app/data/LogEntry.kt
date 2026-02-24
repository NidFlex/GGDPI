package com.ggdpi.app.data

data class LogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val type: LogType = LogType.INFO,
    val service: String? = null
) {
    enum class LogType {
        INFO, WARNING, ERROR, DEBUG, BYPASS
    }

    fun formatted(): String {
        val time = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
        return "[$time] [${type.name}] ${service?.let { "$it: " } ?: ""}$message"
    }
}