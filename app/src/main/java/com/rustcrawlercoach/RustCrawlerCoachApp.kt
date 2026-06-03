package com.rustcrawlercoach

import android.app.Application
import com.rustcrawlercoach.data.database.AppDatabase
import com.rustcrawlercoach.network.DeepSeekConfig
import com.rustcrawlercoach.util.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RustCrawlerCoachApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    lateinit var database: AppDatabase
        private set

    lateinit var preferencesManager: PreferencesManager
        private set

    override fun onCreate() {
        super.onCreate()

        // 初始化数据库
        database = AppDatabase.getDatabase(this)

        // 初始化偏好设置
        preferencesManager = PreferencesManager(this)

        // 加载保存的 API Key
        applicationScope.launch {
            val apiKey = preferencesManager.getApiKey()
            if (apiKey.isNotBlank()) {
                DeepSeekConfig.setApiKey(apiKey)
            }
        }
    }
}
