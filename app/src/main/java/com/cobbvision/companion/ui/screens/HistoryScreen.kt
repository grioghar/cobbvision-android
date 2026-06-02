package com.cobbvision.companion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cobbvision.companion.CobbVisionApp
import com.cobbvision.companion.data.DriveSession
import com.cobbvision.companion.service.GPXExporter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(app: CobbVisionApp, padding: PaddingValues) {
    val sessions by app.sessionRepository.sessions.collectAsState()
    val context  = LocalContext.current
    val scope    = rememberCoroutineScope()

    var sessionToUpload by remember { mutableStateOf<DriveSession?>(null) }

    if (sessions.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.LocationOn, contentDescription = null,
                     modifier = Modifier.size(48.dp),
                     tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
                Spacer(Modifier.height(8.dp))
                Text("No sessions yet", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            }
        }
        return
    }

    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = padding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(sessions.reversed(), key = { it.id }) { session ->
            SessionCard(
                session    = session,
                onUpload   = { sessionToUpload = session },
                onDelete   = { scope.launch { app.sessionRepository.delete(session) } }
            )
        }
    }

    sessionToUpload?.let { s ->
        UploadDialog(
            session     = s,
            onDismiss   = { sessionToUpload = null },
            onUpload    = { vehicleId ->
                val gpxFile = GPXExporter.export(s, context.cacheDir)
                com.cobbvision.companion.network.ApiClient.uploadGPX(gpxFile, vehicleId)
                app.sessionRepository.markUploaded(s.id, vehicleId)
                gpxFile.delete()
                sessionToUpload = null
            },
            onSaveLater = { sessionToUpload = null }
        )
    }
}

@Composable
private fun SessionCard(
    session:  DriveSession,
    onUpload: () -> Unit,
    onDelete: () -> Unit,
) {
    val dateStr = SimpleDateFormat("MMM d, yyyy  HH:mm", Locale.getDefault())
        .format(Date(session.startedAtMs))

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier            = Modifier.padding(12.dp),
            verticalAlignment   = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(dateStr, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(formatDuration(session.durationSeconds),
                         style = MaterialTheme.typography.bodySmall,
                         color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text("${session.pointCount} pts",
                         style = MaterialTheme.typography.bodySmall,
                         color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            if (session.uploaded) {
                Icon(Icons.Filled.CheckCircle, contentDescription = "Uploaded",
                     tint = MaterialTheme.colorScheme.primary)
            } else {
                TextButton(onClick = onUpload) { Text("Upload") }
            }
            TextButton(onClick = onDelete) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val m = seconds / 60; val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
