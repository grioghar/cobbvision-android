package com.cobbvision.companion

import android.app.Application
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cobbvision.companion.data.SessionRepository
import com.cobbvision.companion.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

val Application.dataStore by preferencesDataStore("settings")

class CobbVisionApp : Application() {

    lateinit var sessionRepository: SessionRepository

    override fun onCreate() {
        super.onCreate()
        sessionRepository = SessionRepository(this)
        // Load API settings into the client singleton at startup.
        CoroutineScope(Dispatchers.IO).launch { loadApiSettings() }
    }

    private suspend fun loadApiSettings() {
        val prefs   = dataStore.data.first()
        val baseUrl = prefs[stringPreferencesKey("api_base_url")] ?: "https://cobbvision.com"
        val apiKey  = prefs[stringPreferencesKey("api_key")]      ?: ""
        ApiClient.baseUrl = baseUrl
        ApiClient.apiKey  = apiKey
    }
}
