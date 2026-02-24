package com.ggdpi.app.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.util.concurrent.ConcurrentLinkedQueue

class LogRepository {

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val logQueue = ConcurrentLinkedQueue<LogEntry>()
    private val maxLogs = 500

    fun addLog(message: String, type: LogEntry.LogType = LogEntry.LogType.INFO, service: String? = null) {
        val entry = LogEntry(message = message, type = type, service = service)

        // Добавляем в очередь
        logQueue.offer(entry)

        // Обновляем StateFlow (берём последние maxLogs)
        val updatedLogs = logQueue.toList().takeLast(maxLogs)
        _logs.value = updatedLogs

        // Дублируем в Timber для отладки
        when (type) {
            LogEntry.LogType.ERROR -> Timber.e(entry.message)
            LogEntry.LogType.WARNING -> Timber.w(entry.message)
            LogEntry.LogType.DEBUG -> Timber.d(entry.message)
            else -> Timber.i(entry.message)
        }
    }

    fun clear() {
        logQueue.clear()
        _logs.value = emptyList()
    }

    fun getLogsByType(type: LogEntry.LogType): List<LogEntry> {
        return logQueue.filter { it.type == type }
    }
}