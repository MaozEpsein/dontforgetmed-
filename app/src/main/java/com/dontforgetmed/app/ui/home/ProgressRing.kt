package com.dontforgetmed.app.ui.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ProgressRing(
    progress: Float,
    taken: Int,
    total: Int,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 10.dp,
    trackColor: Color = Color.White.copy(alpha = 0.25f),
    primaryColor: Color = Color.White,
    secondaryColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "progress",
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2
            val rect = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = rect,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
            if (animated > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0f to secondaryColor,
                        1f to primaryColor,
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = topLeft,
                    size = rect,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "$taken/$total",
                color = primaryColor,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (total > 0) "${(progress * 100).toInt()}%" else "–",
                color = primaryColor.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun Column(
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable () -> Unit,
) {
    androidx.compose.foundation.layout.Column(
        horizontalAlignment = horizontalAlignment,
        content = { content() },
    )
}
