package com.rustcrawlercoach.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rustcrawlercoach.network.DeepSeekConfig
import com.rustcrawlercoach.util.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val apiKey: String = "",
    val themeMode: String = "system",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            preferencesManager.apiKey.collect { apiKey ->
                _uiState.value = _uiState.value.copy(apiKey = apiKey)
                if (apiKey.isNotEmpty()) {
                    DeepSeekConfig.setApiKey(apiKey)
                }
            }
        }
        viewModelScope.launch {
            preferencesManager.themeMode.collect { theme ->
                _uiState.value = _uiState.value.copy(themeMode = theme)
            }
        }
    }

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            preferencesManager.saveApiKey(key)
            _uiState.value = _uiState.value.copy(
                isSaving = false,
                saveSuccess = true
            )
        }
    }

    fun saveThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesManager.saveThemeMode(mode)
            _uiState.value = _uiState.value.copy(themeMode = mode)
        }
    }

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }

    fun isApiKeyConfigured(): Boolean = DeepSeekConfig.isConfigured()
}
