package com.ggdpi.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HostListRepository(private val context: Context) {
    
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "host_list")
    
    companion object {
        val CUSTOM_HOSTS = stringSetPreferencesKey("custom_hosts")
        val EXCLUDED_HOSTS = stringSetPreferencesKey("excluded_hosts")
    }
    
    val customHosts: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[CUSTOM_HOSTS] ?: emptySet()
        }
    
    val excludedHosts: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[EXCLUDED_HOSTS] ?: emptySet()
        }
    
    suspend fun addCustomHost(host: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[CUSTOM_HOSTS] ?: emptySet()
            preferences[CUSTOM_HOSTS] = current + host
        }
    }
    
    suspend fun removeCustomHost(host: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[CUSTOM_HOSTS] ?: emptySet()
            preferences[CUSTOM_HOSTS] = current - host
        }
    }
    
    suspend fun addExcludedHost(host: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[EXCLUDED_HOSTS] ?: emptySet()
            preferences[EXCLUDED_HOSTS] = current + host
        }
    }
    
    suspend fun removeExcludedHost(host: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[EXCLUDED_HOSTS] ?: emptySet()
            preferences[EXCLUDED_HOSTS] = current - host
        }
    }
}