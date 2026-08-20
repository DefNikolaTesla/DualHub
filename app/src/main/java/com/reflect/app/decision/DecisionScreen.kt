package com.reflect.app.decision

import android.graphics.drawable.Drawable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.reflect.app.ui.theme.Accent
import com.reflect.app.ui.theme.Background
import com.reflect.app.ui.theme.DeepBackground
import com.reflect.app.ui.theme.DividerColor
import com.reflect.app.ui.theme.OnBackground
import com.reflect.app.ui.theme.OnSurfaceVariant
import com.reflect.app.ui.theme.PureWhite
import com.reflect.app.ui.theme.SurfaceElevated

@Composable
fun DecisionScreen(
    appName: String,
    appIcon: Drawable?,
    onOpenAnyway: () -> Unit,
    onGoBack: () -> Unit
) {
    // Gentle breathing scale animation for the icon — calm, not urgent
    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.045f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "fadeIn"
    )
    val contentOffset by animateFloatAsState(
        targetValue = if (visible) 0f else 24f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "offsetIn"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(colors = listOf(Background, DeepBackground))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .graphicsLayer(alpha = contentAlpha, translationY = contentOffset),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .size(132.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .clip(CircleShape)
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                if (appIcon != null) {
                    Image(
                        bitmap = appIcon.toBitmap(width = 240, height = 240).asImageBitmap(),
                        contentDescription = appName,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(18.dp))
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            Text(
                text = "You're about to open",
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = appName,
                style = MaterialTheme.typography.displayLarge,
                color = OnBackground,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(28.dp))

            Text(
                text = "Is this how you want to spend your time right now?",
                style = MaterialTheme.typography.titleMedium,
                color = OnBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Take a breath. There's no rush.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onGoBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Accent,
                    contentColor = PureWhite
                )
            ) {
                Text("No, go back", style = MaterialTheme.typography.labelLarge, fontSize = 17.sp)
            }

            Spacer(Modifier.height(14.dp))

            OutlinedButton(
                onClick = onOpenAnyway,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = OnSurfaceVariant
                ),
                border = BorderStroke(1.dp, DividerColor)
            ) {
                Text("Yes, open it", style = MaterialTheme.typography.labelLarge, fontSize = 17.sp)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
