package com.ggdpi.app.data

import com.ggdpi.app.ui.screens.LogEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.CopyOnWriteArrayList

class LogRepository(private val maxSize: Int = 1000) {
    
    private val logs = CopyOnWriteArrayList<LogEntry>()
    private val _logsFlow = MutableStateFlow<List<LogEntry>>(emptyList())
    val logsFlow: StateFlow<List<LogEntry>> = _logsFlow.asStateFlow()
    
    fun addLog(entry: LogEntry) {
        logs.add(entry)
        if (logs.size > maxSize) {
            logs.removeAt(0)
        }
        _logsFlow.value = logs.toList()
    }
    
    fun addLog(message: String, type: LogEntry.LogType = LogEntry.LogType.INFO) {
        addLog(LogEntry(message = message, type = type))
    }
    
    fun clearLogs() {
        logs.clear()
        _logsFlow.value = emptyList()
    }
    
    fun getRecentLogs(count: Int): List<LogEntry> {
        return logs.takeLast(count)
    }
}