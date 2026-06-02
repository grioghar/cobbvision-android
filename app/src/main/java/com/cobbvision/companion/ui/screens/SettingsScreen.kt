package com.cobbvision.companion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cobbvision.companion.CobbVisionApp
import com.cobbvision.companion.dataStore
import com.cobbvision.companion.network.ApiClient
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(app: CobbVisionApp, padding: PaddingValues) {
    val scope     = rememberCoroutineScope()
    val keyUrl    = stringPreferencesKey("api_base_url")
    val keyApiKey = stringPreferencesKey("api_key")

    val storedUrl    by app.dataStore.data.map { it[keyUrl]    ?: "https://cobbvision.com" }.collectAsState("https://cobbvision.com")
    val storedApiKey by app.dataStore.data.map { it[keyApiKey] ?: "" }.collectAsState("")

    var urlDraft    by remember(storedUrl)    { mutableStateOf(storedUrl) }
    var apiKeyDraft by remember(storedApiKey) { mutableStateOf(storedApiKey) }
    var showKey     by remember { mutableStateOf(false) }
    var saved       by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("CobbVision Account", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value         = urlDraft,
            onValueChange = { urlDraft = it; saved = false },
            label         = { Text("Server URL") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
        )

        OutlinedTextField(
            value               = apiKeyDraft,
            onValueChange       = { apiKeyDraft = it; saved = false },
            label               = { Text("API Key") },
            singleLine          = true,
            modifier            = Modifier.fillMaxWidth(),
            visualTransformation = if (showKey) VisualTransformation.None
                                   else PasswordVisualTransformation(),
            trailingIcon        = {
                TextButton(onClick = { showKey = !showKey }) {
                    Text(if (showKey) "Hide" else "Show")
                }
            },
        )

        Text(
            "Find your API key on the Account page of the CobbVision web app.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )

        Button(
            onClick = {
                scope.launch {
                    app.dataStore.edit { prefs ->
                        prefs[keyUrl]    = urlDraft
                        prefs[keyApiKey] = apiKeyDraft
                    }
                    ApiClient.baseUrl = urlDraft
                    ApiClient.apiKey  = apiKeyDraft
                    saved = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (saved) "✓ Saved" else "Save")
        }

        HorizontalDivider()

        Text("About", style = MaterialTheme.typography.titleMedium)
        Text("CobbVision Companion  v1.0.0",
             style = MaterialTheme.typography.bodySmall,
             color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}
