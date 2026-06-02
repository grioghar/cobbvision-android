package com.cobbvision.companion.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/** Persists completed sessions as JSON in the app's files directory. */
class SessionRepository(context: Context) {

    private val file = File(context.filesDir, "sessions.json")
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private val _sessions = MutableStateFlow<List<DriveSession>>(emptyList())
    val sessions: StateFlow<List<DriveSession>> = _sessions

    init { load() }

    suspend fun save(session: DriveSession) = withContext(Dispatchers.IO) {
        val updated = _sessions.value.toMutableList()
        val existing = updated.indexOfFirst { it.id == session.id }
        if (existing >= 0) updated[existing] = session else updated.add(session)
        _sessions.value = updated
        persist(updated)
    }

    suspend fun delete(session: DriveSession) = withContext(Dispatchers.IO) {
        val updated = _sessions.value.filter { it.id != session.id }
        _sessions.value = updated
        persist(updated)
    }

    suspend fun markUploaded(sessionId: String, vehicleId: String) = withContext(Dispatchers.IO) {
        val updated = _sessions.value.map {
            if (it.id == sessionId) it.copy(uploaded = true, vehicleId = vehicleId) else it
        }
        _sessions.value = updated
        persist(updated)
    }

    private fun load() {
        runCatching {
            val text = file.readText()
            _sessions.value = json.decodeFromString<List<DriveSession>>(text)
        }
    }

    private fun persist(sessions: List<DriveSession>) {
        runCatching { file.writeText(json.encodeToString(sessions)) }
    }
}
