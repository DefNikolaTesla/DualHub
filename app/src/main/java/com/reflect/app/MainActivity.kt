package com.reflect.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.reflect.app.service.ReflectForegroundService
import com.reflect.app.ui.home.HomeScreen
import com.reflect.app.ui.permissions.PermissionsScreen
import com.reflect.app.ui.select.SelectAppsScreen
import com.reflect.app.ui.theme.ReflectTheme
import com.reflect.app.util.PermissionUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startReflectService()

        setContent {
            ReflectTheme {
                val navController = rememberNavController()
                val startDestination = if (
                    PermissionUtils.hasUsageAccess(this) &&
                    PermissionUtils.isAccessibilityServiceEnabled(this)
                ) "home" else "permissions"

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("permissions") {
                        PermissionsScreen(onDone = {
                            navController.navigate("home") {
                                popUpTo("permissions") { inclusive = true }
                            }
                        })
                    }
                    composable("home") {
                        HomeScreen(
                            onPickApps = { navController.navigate("select") },
                            onSetupPermissions = { navController.navigate("permissions") }
                        )
                    }
                    composable("select") {
                        SelectAppsScreen(
                            onBack = { navController.popBackStack() },
                            onDone = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    private fun startReflectService() {
        val intent = Intent(this, ReflectForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
