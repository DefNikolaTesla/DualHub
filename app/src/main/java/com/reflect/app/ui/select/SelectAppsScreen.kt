package com.reflect.app.ui.select

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.reflect.app.data.InstalledAppInfo
import com.reflect.app.ui.theme.Accent
import com.reflect.app.ui.theme.Background
import com.reflect.app.ui.theme.DividerColor
import com.reflect.app.ui.theme.OnBackground
import com.reflect.app.ui.theme.OnSurfaceVariant
import com.reflect.app.ui.theme.PureWhite
import com.reflect.app.ui.theme.Surface as SurfaceColor
import com.reflect.app.ui.theme.SurfaceElevated

@Composable
fun SelectAppsScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: SelectAppsViewModel = viewModel()
) {
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val apps by viewModel.filteredApps.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = OnBackground)
                }
                Text("Choose apps", style = MaterialTheme.typography.titleLarge, color = OnBackground)
            }
        },
        bottomBar = {
            Surface(color = SurfaceElevated, shadowElevation = 12.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "${selected.size} selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Button(
                        onClick = {
                            viewModel.save()
                            onDone()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent)
                    ) {
                        Text("Save", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search apps") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = OnSurfaceVariant) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceColor,
                    unfocusedContainerColor = SurfaceColor,
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = OnBackground,
                    unfocusedTextColor = OnBackground
                )
            )

            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent)
                }
            } else if (apps.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(apps, key = { it.packageName }) { app ->
                        AppRow(
                            app = app,
                            isSelected = app.packageName in selected,
                            onToggle = { viewModel.toggle(app.packageName) }
                        )
                    }
                    item { Spacer(Modifier.height(140.dp)) }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(SurfaceElevated, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Shield, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("No apps found", style = MaterialTheme.typography.titleMedium, color = OnBackground)
        Spacer(Modifier.height(4.dp))
        Text(
            "Try a different search term.",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )
    }
}

@Composable
private fun AppRow(app: InstalledAppInfo, isSelected: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (app.icon != null) {
            Image(
                bitmap = app.icon.toBitmap(width = 96, height = 96).asImageBitmap(),
                contentDescription = app.label,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        } else {
            Box(
                Modifier
                    .size(44.dp)
                    .background(SurfaceElevated, RoundedCornerShape(12.dp))
            )
        }

        Spacer(Modifier.width(16.dp))

        Text(
            app.label,
            style = MaterialTheme.typography.titleMedium,
            color = OnBackground,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Medium
        )

        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (isSelected) Accent else SurfaceColor),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
            }
        }
    }
}
