package com.ggdpi.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class HostListRepository(private val context: Context) {
    
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "host_list")
    
    companion object {
        val CUSTOM_HOSTS = stringSetPreferencesKey("custom_hosts")
        val EXCLUDED_HOSTS = stringSetPreferencesKey("excluded_hosts")
        val CUSTOM_HOSTS_UPDATE_URL = stringPreferencesKey("custom_hosts_update_url")
        val CUSTOM_HOSTS_LAST_UPDATED_EPOCH_MS = longPreferencesKey("custom_hosts_last_updated_epoch_ms")
    }
    
    val customHosts: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[CUSTOM_HOSTS] ?: emptySet()
        }
    
    val excludedHosts: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[EXCLUDED_HOSTS] ?: emptySet()
        }

    val customHostsUpdateUrl: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[CUSTOM_HOSTS_UPDATE_URL] ?: ""
        }

    val customHostsLastUpdatedEpochMs: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[CUSTOM_HOSTS_LAST_UPDATED_EPOCH_MS] ?: 0L
        }
    
    suspend fun addCustomHost(host: String) {
        val normalized = normalizeHost(host) ?: return
        context.dataStore.edit { preferences ->
            val current = preferences[CUSTOM_HOSTS] ?: emptySet()
            preferences[CUSTOM_HOSTS] = current + normalized
        }
    }
    
    suspend fun removeCustomHost(host: String) {
        val normalized = normalizeHost(host) ?: return
        context.dataStore.edit { preferences ->
            val current = preferences[CUSTOM_HOSTS] ?: emptySet()
            preferences[CUSTOM_HOSTS] = current - normalized
        }
    }
    
    suspend fun addExcludedHost(host: String) {
        val normalized = normalizeHost(host) ?: return
        context.dataStore.edit { preferences ->
            val current = preferences[EXCLUDED_HOSTS] ?: emptySet()
            preferences[EXCLUDED_HOSTS] = current + normalized
        }
    }
    
    suspend fun removeExcludedHost(host: String) {
        val normalized = normalizeHost(host) ?: return
        context.dataStore.edit { preferences ->
            val current = preferences[EXCLUDED_HOSTS] ?: emptySet()
            preferences[EXCLUDED_HOSTS] = current - normalized
        }
    }

    suspend fun setCustomHostsUpdateUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOM_HOSTS_UPDATE_URL] = url.trim()
        }
    }

    suspend fun replaceCustomHosts(hosts: Set<String>) {
        val normalized = hosts.mapNotNull(::normalizeHost).toSet()
        context.dataStore.edit { preferences ->
            preferences[CUSTOM_HOSTS] = normalized
            preferences[CUSTOM_HOSTS_LAST_UPDATED_EPOCH_MS] = System.currentTimeMillis()
        }
    }

    suspend fun updateCustomHostsFromUrl(url: String): Int {
        val trimmedUrl = url.trim()
        require(trimmedUrl.isNotEmpty()) { "Update URL is empty" }

        val text = withContext(Dispatchers.IO) { httpGetText(trimmedUrl) }
        val hosts = parseHosts(text)
        replaceCustomHosts(hosts)
        return hosts.size
    }

    private fun parseHosts(text: String): Set<String> {
        return text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .filterNot { it.startsWith("#") || it.startsWith("//") || it.startsWith(";") }
            .map { it.substringBefore('#').trim() }
            .map { it.substringBefore(';').trim() }
            .map { it.split(Regex("\\s+")).firstOrNull().orEmpty() }
            .mapNotNull(::normalizeHost)
            .toSet()
    }

    private fun normalizeHost(host: String): String? {
        val v = host.trim().lowercase()
        if (v.isEmpty()) return null
        if (v.contains("://")) return null
        if (v.contains("/") || v.contains("\\") || v.contains("@")) return null
        if (v.length > 253) return null
        return v.trim('.')
    }

    private fun httpGetText(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            instanceFollowRedirects = true
            connectTimeout = 10_000
            readTimeout = 15_000
            setRequestProperty("Accept", "text/plain, */*;q=0.8")
            setRequestProperty("User-Agent", "GGDPI/1.0 (Android)")
        }

        connection.connect()
        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
        if (code !in 200..299) {
            throw IllegalStateException("HTTP $code: ${body.take(200)}")
        }
        return body
    }
}