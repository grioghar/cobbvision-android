package com.cobbvision.companion.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Nav(val route: String, val label: String, val icon: ImageVector) {
    object Record   : Nav("record",   "Record",   Icons.Filled.PlayArrow)
    object History  : Nav("history",  "Sessions", Icons.Filled.List)
    object Settings : Nav("settings", "Settings", Icons.Filled.Settings)

    companion object {
        val items = listOf(Record, History, Settings)
    }
}
