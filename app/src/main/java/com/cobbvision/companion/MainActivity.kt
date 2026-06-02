package com.cobbvision.companion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cobbvision.companion.ui.screens.HistoryScreen
import com.cobbvision.companion.ui.screens.SessionScreen
import com.cobbvision.companion.ui.screens.SettingsScreen
import com.cobbvision.companion.ui.theme.CobbVisionTheme
import com.cobbvision.companion.ui.Nav

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as CobbVisionApp

        setContent {
            CobbVisionTheme {
                val navController = rememberNavController()
                val navBackStack  by navController.currentBackStackEntryAsState()
                val currentDest   = navBackStack?.destination

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            Nav.items.forEach { item ->
                                NavigationBarItem(
                                    selected = currentDest?.hierarchy?.any { it.route == item.route } == true,
                                    onClick  = {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState    = true
                                        }
                                    },
                                    icon  = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label) }
                                )
                            }
                        }
                    }
                ) { padding ->
                    NavHost(navController, startDestination = Nav.Record.route) {
                        composable(Nav.Record.route)   { SessionScreen(app, padding) }
                        composable(Nav.History.route)  { HistoryScreen(app, padding) }
                        composable(Nav.Settings.route) { SettingsScreen(app, padding) }
                    }
                }
            }
        }
    }
}
