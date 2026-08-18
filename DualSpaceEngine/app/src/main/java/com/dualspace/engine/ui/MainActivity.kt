package com.dualspace.engine.ui

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.dualspace.engine.core.VirtualCore
import com.dualspace.engine.model.VirtualAppInfo
import com.dualspace.engine.service.KeepAliveService

/**
 * Host launcher UI.
 *
 * Shows:
 *  - List of installed user apps that can be cloned
 *  - List of already created virtual clones
 *  - Buttons to Clone / Launch / Delete
 *
 * This is the control surface for the VirtualCore engine.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start keep-alive early
        startForegroundService(Intent(this, KeepAliveService::class.java))

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = androidx.compose.ui.graphics.Color(0xFF7C4DFF),
                    background = androidx.compose.ui.graphics.Color(0xFF0F0F1A),
                    surface = androidx.compose.ui.graphics.Color(0xFF1A1A2E)
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DualSpaceScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DualSpaceScreen() {
    val context = LocalContext.current
    val pm = context.packageManager
    val core = VirtualCore.get()

    var installedApps by remember { mutableStateOf<List<AppItem>>(emptyList()) }
    var virtualApps by remember { mutableStateOf<List<VirtualAppInfo>>(emptyList()) }
    var status by remember { mutableStateOf("Engine ready. Select an app to clone.") }
    var loading by remember { mutableStateOf(false) }

    fun refresh() {
        // Load launchable third-party apps
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = pm.queryIntentActivities(intent, 0)
        installedApps = resolved
            .map { it.activityInfo.applicationInfo }
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .map {
                AppItem(
                    packageName = it.packageName,
                    label = pm.getApplicationLabel(it).toString(),
                    icon = pm.getApplicationIcon(it)
                )
            }
            .sortedBy { it.label.lowercase() }

        virtualApps = core.getAllVirtualApps()
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DualSpace Engine") },
                actions = {
                    IconButton(onClick = { refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
            }

            Text("Existing Clones", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (virtualApps.isEmpty()) {
                Text(
                    "No clones yet. Pick an app below and tap Clone.",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                LazyColumn(modifier = Modifier.weight(0.4f)) {
                    items(virtualApps, key = { it.virtualPackage }) { vApp ->
                        VirtualAppRow(
                            info = vApp,
                            onLaunch = {
                                val ok = core.launchVirtualApp(vApp.virtualPackage)
                                status = if (ok) {
                                    "Launch requested for ${vApp.label}. Full process start still requires proxy hooks."
                                } else {
                                    "Launch failed for ${vApp.virtualPackage}"
                                }
                            },
                            onDelete = {
                                core.removeClone(vApp.virtualPackage)
                                refresh()
                                status = "Deleted ${vApp.label}"
                            }
                        )
                        Divider()
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Cloneable Apps", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(0.6f)) {
                items(installedApps, key = { it.packageName }) { app ->
                    InstallableAppRow(
                        app = app,
                        onClone = {
                            loading = true
                            status = "Cloning ${app.label}…"
                            // Run clone on background thread in real usage
                            val result = core.cloneApp(app.packageName)
                            loading = false
                            result.onSuccess { info ->
                                status = "Created ${info.label} → ${info.virtualPackage}\nAPK rewritten. Signing + install still required for full launch."
                                refresh()
                                Toast.makeText(context, "Clone created: ${info.virtualPackage}", Toast.LENGTH_LONG).show()
                            }.onFailure { e ->
                                status = "Clone failed: ${e.message}"
                                Toast.makeText(context, "Clone failed: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                    Divider()
                }
            }
        }
    }
}

data class AppItem(
    val packageName: String,
    val label: String,
    val icon: Drawable?
)

@Composable
fun InstallableAppRow(app: AppItem, onClone: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(app.icon)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(app.label, style = MaterialTheme.typography.bodyLarge)
            Text(app.packageName, style = MaterialTheme.typography.bodySmall)
        }
        Button(onClick = onClone) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Clone")
        }
    }
}

@Composable
fun VirtualAppRow(
    info: VirtualAppInfo,
    onLaunch: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(info.icon)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(info.label, style = MaterialTheme.typography.bodyLarge)
            Text(info.virtualPackage, style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = onLaunch) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Launch")
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}

@Composable
fun AppIcon(icon: Drawable?) {
    if (icon == null) {
        Box(modifier = Modifier.size(40.dp))
        return
    }
    val bitmap = remember(icon) { icon.toBitmap(96, 96).asImageBitmap() }
    Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.size(40.dp))
}
