package com.cobbvision.companion.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.cobbvision.companion.CobbVisionApp
import com.cobbvision.companion.service.LocationService
import com.cobbvision.companion.service.GPXExporter
import kotlinx.coroutines.launch

@Composable
fun SessionScreen(app: CobbVisionApp, padding: PaddingValues) {
    val context     = LocalContext.current
    val scope       = rememberCoroutineScope()
    val isRecording by LocationService.isRecording.collectAsState()
    val session     by LocationService.currentSession.collectAsState()

    var showUploadDialog by remember { mutableStateOf(false) }
    var finishedSession  by remember { mutableStateOf<com.cobbvision.companion.data.DriveSession?>(null) }

    // Track the previous recording state to detect session end
    var wasRecording by remember { mutableStateOf(false) }
    LaunchedEffect(isRecording) {
        if (wasRecording && !isRecording) {
            // Service just stopped — pick up the completed session
            finishedSession  = LocationService.currentSession.value
            showUploadDialog = true
        }
        wasRecording = isRecording
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            context.startForegroundService(
                Intent(context, LocationService::class.java)
                    .setAction(LocationService.ACTION_START)
            )
        }
    }

    Column(
        modifier            = Modifier.fillMaxSize().padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        // Stats card
        if (isRecording && session != null) {
            Card(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier            = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("Duration", formatDuration(session!!.durationSeconds))
                    StatItem("Points",   "${session!!.pointCount}")
                    val lastSpeed = session?.trackPoints?.lastOrNull()?.speedKPH
                    StatItem("Speed", if (lastSpeed != null) "%.0f km/h".format(lastSpeed) else "—")
                }
            }
        } else {
            Text(
                "Ready to record",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Start a session to begin GPS logging.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
        }

        Spacer(Modifier.height(48.dp))

        // Record button
        Button(
            onClick = {
                if (isRecording) {
                    context.startService(
                        Intent(context, LocationService::class.java)
                            .setAction(LocationService.ACTION_STOP)
                    )
                } else {
                    val fineGranted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    if (fineGranted) {
                        context.startForegroundService(
                            Intent(context, LocationService::class.java)
                                .setAction(LocationService.ACTION_START)
                        )
                    } else {
                        permLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                            )
                        )
                    }
                }
            },
            shape  = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRecording) Color.Red else MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.size(96.dp),
        ) {
            Text(if (isRecording) "■" else "●", fontSize = 32.sp)
        }

        Spacer(Modifier.height(12.dp))
        Text(
            if (isRecording) "Tap to stop" else "Tap to record",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }

    // Upload dialog shown when session ends
    if (showUploadDialog && finishedSession != null) {
        UploadDialog(
            session = finishedSession!!,
            onDismiss = { showUploadDialog = false },
            onUpload  = { vehicleId ->
                scope.launch {
                    val gpxFile = GPXExporter.export(finishedSession!!, context.cacheDir)
                    val result  = com.cobbvision.companion.network.ApiClient.uploadGPX(gpxFile, vehicleId)
                    app.sessionRepository.save(finishedSession!!.copy(uploaded = true, vehicleId = vehicleId))
                    gpxFile.delete()
                    showUploadDialog = false
                }
            },
            onSaveLater = {
                scope.launch {
                    app.sessionRepository.save(finishedSession!!)
                    showUploadDialog = false
                }
            }
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontFamily = FontFamily.Monospace,
             style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.labelSmall,
             color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

private fun formatDuration(seconds: Long): String {
    val m = seconds / 60; val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
