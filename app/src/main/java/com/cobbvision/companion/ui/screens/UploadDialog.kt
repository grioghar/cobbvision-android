package com.cobbvision.companion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cobbvision.companion.data.DriveSession
import com.cobbvision.companion.network.ApiClient
import kotlinx.coroutines.launch

@Composable
fun UploadDialog(
    session:    DriveSession,
    onDismiss:  () -> Unit,
    onUpload:   suspend (vehicleId: String) -> Unit,
    onSaveLater: () -> Unit,
) {
    val scope    = rememberCoroutineScope()
    var vehicles by remember { mutableStateOf<List<ApiClient.Vehicle>>(emptyList()) }
    var selected by remember { mutableStateOf<ApiClient.Vehicle?>(null) }
    var loading  by remember { mutableStateOf(false) }
    var error    by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        runCatching { vehicles = ApiClient.fetchVehicles() }
            .onFailure { error = it.message }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("Session complete") },
        text    = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Duration: ${formatDuration(session.durationSeconds)}")
                Text("GPS points: ${session.pointCount}")

                Spacer(Modifier.height(4.dp))

                if (vehicles.isEmpty()) {
                    if (error != null)
                        Text("Could not load vehicles: $error",
                             color = MaterialTheme.colorScheme.error)
                    else
                        CircularProgressIndicator(modifier = Modifier.size(20.dp),
                                                  strokeWidth = 2.dp)
                } else {
                    Text("Upload to vehicle:", style = MaterialTheme.typography.labelMedium)
                    vehicles.forEach { v ->
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            RadioButton(
                                selected = selected?.id == v.id,
                                onClick  = { selected = v }
                            )
                            Text("${v.year} ${v.make} ${v.model}")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = selected != null && !loading,
                onClick = {
                    val v = selected ?: return@Button
                    loading = true
                    scope.launch {
                        runCatching { onUpload(v.id) }
                            .onFailure { error = it.message; loading = false }
                    }
                }
            ) {
                if (loading) CircularProgressIndicator(
                    modifier    = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color       = MaterialTheme.colorScheme.onPrimary
                )
                else Text("Upload")
            }
        },
        dismissButton = {
            TextButton(onClick = onSaveLater) { Text("Save for later") }
        }
    )
}

private fun formatDuration(seconds: Long): String {
    val m = seconds / 60; val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
