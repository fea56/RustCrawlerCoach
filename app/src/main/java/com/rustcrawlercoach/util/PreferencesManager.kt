package com.rustcrawlercoach.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rustcrawlercoach.network.DeepSeekConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    companion object {
        private val API_KEY = stringPreferencesKey("deepseek_api_key")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val LEARNING_ADVICE = stringPreferencesKey("learning_advice")
        private val ADVICE_LAST_REFRESH = stringPreferencesKey("advice_last_refresh")
    }

    val apiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[API_KEY] ?: ""
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "system"
    }

    val learningAdvice: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LEARNING_ADVICE] ?: ""
    }

    val adviceLastRefresh: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[ADVICE_LAST_REFRESH] ?: ""
    }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = key
        }
        DeepSeekConfig.setApiKey(key)
    }

    suspend fun getApiKey(): String {
        return context.dataStore.data.first()[API_KEY] ?: ""
    }

    suspend fun saveThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun saveLearningAdvice(advice: String) {
        context.dataStore.edit { preferences ->
            preferences[LEARNING_ADVICE] = advice
        }
    }

    suspend fun saveAdviceLastRefresh(timestamp: String) {
        context.dataStore.edit { preferences ->
            preferences[ADVICE_LAST_REFRESH] = timestamp
        }
    }

    suspend fun canRefreshAdvice(): Boolean {
        val lastRefresh = context.dataStore.data.first()[ADVICE_LAST_REFRESH]?.toLongOrNull() ?: 0
        val now = System.currentTimeMillis()
        val hoursSinceLastRefresh = (now - lastRefresh) / (1000 * 60 * 60)
        return hoursSinceLastRefresh >= 24
    }
}
