package com.ggdpi.app.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

class StrategyManager(private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "strategies")

    companion object {
        val CURRENT_STRATEGY = stringPreferencesKey("current_strategy")
        
        val strategies = mapOf(
            StrategyId.GENERAL to DpiStrategy(
                id = StrategyId.GENERAL,
                displayName = "General",
                description = "Для большинства сайтов",
                desyncMode = "fake,multidisorder",
                splitPos = listOf(1, "midsld"),
                foolingMethods = listOf("badseq", "md5sig"),
                repeats = 6
            ),
            StrategyId.INSTAGRAM to DpiStrategy(
                id = StrategyId.INSTAGRAM,
                displayName = "Instagram",
                description = "Оптимизировано для Instagram/Facebook",
                desyncMode = "fake,multisplit",
                splitPos = listOf(1, "sniext+1", "endhost-2"),
                foolingMethods = listOf("badseq", "md5sig", "ts"),
                repeats = 8,
                fakeSni = "facebook.com"
            ),
            StrategyId.YOUTUBE to DpiStrategy(
                id = StrategyId.YOUTUBE,
                displayName = "YouTube",
                description = "Для YouTube и Google Video",
                desyncMode = "fake,multidisorder",
                splitPos = listOf(1, "sniext+1"),
                foolingMethods = listOf("badseq", "md5sig"),
                repeats = 8,
                wsize = "1:6"
            ),
            StrategyId.DISCORD to DpiStrategy(
                id = StrategyId.DISCORD,
                displayName = "Discord",
                description = "Для Discord и голосовых чатов",
                desyncMode = "fake,multidisorder",
                splitPos = listOf(1, "midsld"),
                foolingMethods = listOf("badseq", "md5sig"),
                repeats = 6,
                wsize = "1:6"
            ),
            StrategyId.TELEGRAM to DpiStrategy(
                id = StrategyId.TELEGRAM,
                displayName = "Telegram",
                description = "Для Telegram (MTProto)",
                desyncMode = "fake,multidisorder",
                splitPos = listOf(1, "sniext+1", "endhost-2"),
                foolingMethods = listOf("badseq", "md5sig", "ts"),
                repeats = 8,
                autoTtl = "2:3-15"
            ),
            StrategyId.ALT to DpiStrategy(
                id = StrategyId.ALT,
                displayName = "ALT",
                description = "Альтернативная стратегия",
                desyncMode = "fakedsplit",
                splitPos = listOf("midsld"),
                foolingMethods = listOf("badseq", "md5sig"),
                repeats = 8
            ),
            StrategyId.FAKE to DpiStrategy(
                id = StrategyId.FAKE,
                displayName = "FAKE",
                description = "Максимальная агрессивность",
                desyncMode = "fake",
                splitPos = listOf(1, "midsld", "endhost-2"),
                foolingMethods = listOf("badseq", "md5sig", "ts"),
                repeats = 10,
                autoTtl = "1:3-20"
            )
        )
    }

    val currentStrategy: Flow<DpiStrategy> = context.dataStore.data
        .map { preferences ->
            val id = preferences[CURRENT_STRATEGY] ?: StrategyId.GENERAL.name
            strategies[StrategyId.valueOf(id)] ?: strategies[StrategyId.GENERAL]!!
        }

    fun getCurrentStrategy(): DpiStrategy = runBlocking {
        currentStrategy.first()
    }

    suspend fun setStrategy(id: StrategyId) {
        context.dataStore.edit { it[CURRENT_STRATEGY] = id.name }
    }

    enum class StrategyId {
        GENERAL, INSTAGRAM, YOUTUBE, DISCORD, TELEGRAM, ALT, FAKE
    }

    data class DpiStrategy(
        val id: StrategyId,
        val name: String = "",
        val displayName: String,
        val description: String,
        val desyncMode: String,
        val splitPos: List<Any>,
        val foolingMethods: List<String>,
        val repeats: Int,
        val fakeSni: String? = null,
        val wsize: String? = null,
        val autoTtl: String? = null
    )
    fun loadCurrentStrategy(): DpiStrategy {
        return getDefaultStrategy()  // или вернуть первую из availableStrategies
    }

    fun getDefaultStrategy(): DpiStrategy {
        return DpiStrategy(
            id = StrategyId.DEFAULT,  // или просто "default" если StrategyId — enum
            displayName = "Default",
            description = "Default strategy",
            desyncMode = "none",
            splitPos = 0,
            foolingMethods = emptyList(),
            repeats = 1
        )
    }
}