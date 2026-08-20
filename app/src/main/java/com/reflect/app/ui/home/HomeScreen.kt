package com.reflect.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reflect.app.ui.theme.Accent
import com.reflect.app.ui.theme.AccentSoft
import com.reflect.app.ui.theme.Background
import com.reflect.app.ui.theme.Danger
import com.reflect.app.ui.theme.DangerSoft
import com.reflect.app.ui.theme.OnBackground
import com.reflect.app.ui.theme.OnSurfaceVariant
import com.reflect.app.ui.theme.SurfaceElevated

@Composable
fun HomeScreen(
    onPickApps: () -> Unit,
    onSetupPermissions: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val count by viewModel.restrictedCount.collectAsStateWithLifecycle()
    val permissionsOk by viewModel.permissionsGranted.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.refreshPermissions() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Spacer(Modifier.height(24.dp))

            Text("Reflect", style = MaterialTheme.typography.headlineLarge, color = OnBackground, fontWeight = FontWeight.Bold)
            Text(
                "A calm pause before the apps that pull you in.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )

            Spacer(Modifier.height(40.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(AccentSoft, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Shield, contentDescription = null, tint = Accent, modifier = Modifier.size(34.dp))
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.displayLarge,
                        color = OnBackground
                    )
                    Text(
                        text = if (count == 1) "app being watched" else "apps being watched",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSurfaceVariant
                    )
                }
            }

            if (!permissionsOk) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DangerSoft)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Setup needed", style = MaterialTheme.typography.titleMedium, color = OnBackground)
                        Text(
                            "Reflect needs permissions to watch for apps.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onSetupPermissions,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Danger)
                ) {
                    Text("Finish setup")
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onPickApps,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent)
            ) {
                Text("Manage apps", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
