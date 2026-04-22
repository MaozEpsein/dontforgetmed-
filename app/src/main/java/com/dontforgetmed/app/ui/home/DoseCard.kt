package com.dontforgetmed.app.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dontforgetmed.app.R
import com.dontforgetmed.app.data.entity.DoseStatus
import com.dontforgetmed.app.ui.icons.MedIconCatalog
import com.dontforgetmed.app.util.Time
import kotlin.math.abs

private const val SWIPE_THRESHOLD = 140f

@Composable
fun DoseCard(
    dose: TodayDose,
    onTake: () -> Unit,
    onSkip: () -> Unit,
    onEdit: () -> Unit,
) {
    val medColor = runCatching { Color(android.graphics.Color.parseColor(dose.medication.colorHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)

    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { SWIPE_THRESHOLD.dp.toPx() }
    var offsetX by remember(dose.log.id) { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(targetValue = offsetX, label = "swipe")
    val resolved = dose.log.status != DoseStatus.PENDING

    Box(Modifier.fillMaxWidth()) {
        // Background action hint (visible as user drags)
        if (!resolved && abs(animatedOffset) > 4f) {
            val dragRight = animatedOffset > 0
            val bgColor = if (dragRight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(bgColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 24.dp),
                contentAlignment = if (dragRight) Alignment.CenterStart else Alignment.CenterEnd,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (dragRight) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = bgColor,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (dragRight) stringResource(R.string.take) else stringResource(R.string.skip),
                        color = bgColor,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEdit() }
                .pointerInput(dose.log.id, resolved) {
                    if (resolved) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                offsetX > swipeThresholdPx -> { onTake(); offsetX = 0f }
                                offsetX < -swipeThresholdPx -> { onSkip(); offsetX = 0f }
                                else -> offsetX = 0f
                            }
                        },
                        onDragCancel = { offsetX = 0f },
                        onHorizontalDrag = { _, delta -> offsetX += delta },
                    )
                }
                .graphicsLayer { if (!resolved) translationX = animatedOffset },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (resolved) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (resolved) 0.dp else 3.dp,
            ),
        ) {
            Row(Modifier.fillMaxWidth()) {
                // Color strip on right (RTL leading)
                Box(
                    Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .background(medColor),
                )
                Column(
                    Modifier
                        .weight(1f)
                        .padding(16.dp),
                ) {
                    DoseCardHeader(dose, medColor)
                    if (!resolved) {
                        Spacer(Modifier.height(14.dp))
                        DoseCardActions(onTake, onSkip)
                    } else {
                        Spacer(Modifier.height(10.dp))
                        StatusChip(dose)
                    }
                }
            }
        }
    }
}

@Composable
private fun DoseCardHeader(dose: TodayDose, medColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(52.dp)
                .background(medColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = MedIconCatalog.iconFor(dose.medication.iconKey),
                contentDescription = null,
                tint = medColor,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = dose.medication.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (dose.medication.dosage.isNotBlank()) {
                Text(
                    text = stringResource(R.string.dosage_prefix, dose.medication.dosage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (dose.medication.stockCount in 1..dose.medication.lowStockThreshold) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "מלאי: ${dose.medication.stockCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = Time.formatHm(dose.schedule.minuteOfDay),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun DoseCardActions(onTake: () -> Unit, onSkip: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = onTake,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.take), style = MaterialTheme.typography.titleMedium)
        }
        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Icon(Icons.Default.Close, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.skip), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun StatusChip(dose: TodayDose) {
    val (label, color) = when (dose.log.status) {
        DoseStatus.TAKEN -> stringResource(R.string.taken_status) to MaterialTheme.colorScheme.primary
        DoseStatus.SKIPPED -> stringResource(R.string.skipped_status) to MaterialTheme.colorScheme.onSurfaceVariant
        else -> "" to MaterialTheme.colorScheme.primary
    }
    Box(
        Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
