package com.dontforgetmed.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CelebrationOverlay(visible: Boolean, onDismiss: () -> Unit) {
    val scale = remember { Animatable(0.8f) }
    LaunchedEffect(visible) {
        if (visible) {
            scale.snapTo(0.6f)
            scale.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))
            kotlinx.coroutines.delay(1800)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.7f),
        exit = fadeOut() + scaleOut(targetScale = 0.7f),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .padding(24.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                    .padding(horizontal = 36.dp, vertical = 28.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🎉", style = MaterialTheme.typography.displayLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "סיימת להיום!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "כל הכבוד 💚",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
