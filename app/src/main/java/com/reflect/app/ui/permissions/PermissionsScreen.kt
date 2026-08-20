package com.reflect.app.ui.permissions

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.reflect.app.ui.theme.Accent
import com.reflect.app.ui.theme.Background
import com.reflect.app.ui.theme.OnBackground
import com.reflect.app.ui.theme.OnSurfaceVariant
import com.reflect.app.ui.theme.SurfaceElevated

@Composable
fun PermissionsScreen(onDone: () -> Unit) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(32.dp))
            Text("Let's set up Reflect", style = MaterialTheme.typography.headlineMedium, color = OnBackground)
            Spacer(Modifier.height(8.dp))
            Text(
                "Two permissions are needed so Reflect can notice when you open an app you want to be mindful about.",
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            PermissionCard(
                title = "Usage access",
                description = "Lets Reflect see which app is currently open.",
                buttonText = "Open settings"
            ) {
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }

            Spacer(Modifier.height(16.dp))

            PermissionCard(
                title = "Accessibility service",
                description = "Lets Reflect show the pause screen at the right moment.",
                buttonText = "Open settings"
            ) {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent)
            ) {
                Text("Done", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PermissionCard(title: String, description: String, buttonText: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = OnBackground)
            Spacer(Modifier.height(6.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
            Spacer(Modifier.height(14.dp))
            OutlinedButton(onClick = onClick, shape = RoundedCornerShape(14.dp)) {
                Text(buttonText)
            }
        }
    }
}
