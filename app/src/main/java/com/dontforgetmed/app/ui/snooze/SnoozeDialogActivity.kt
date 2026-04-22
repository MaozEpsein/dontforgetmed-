package com.dontforgetmed.app.ui.snooze

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dontforgetmed.app.R
import com.dontforgetmed.app.notifications.DoseActionReceiver
import com.dontforgetmed.app.ui.theme.DontForgetMedTheme
import java.util.Calendar

class SnoozeDialogActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFinishOnTouchOutside(true)
        val logId = intent.getLongExtra(EXTRA_LOG_ID, -1L)
        if (logId <= 0) { finish(); return }

        setContent {
            DontForgetMedTheme {
                SnoozeSheet(
                    onSnoozeMinutes = { minutes ->
                        broadcastSnooze(logId, minutes)
                        finish()
                    },
                    onDismiss = { finish() },
                )
            }
        }
    }

    private fun broadcastSnooze(logId: Long, minutes: Int) {
        val intent = Intent(this, DoseActionReceiver::class.java).apply {
            action = DoseActionReceiver.ACTION_SNOOZE
            `package` = packageName
            putExtra(DoseActionReceiver.EXTRA_LOG_ID, logId)
            putExtra(DoseActionReceiver.EXTRA_SNOOZE_MINUTES, minutes)
        }
        sendBroadcast(intent)
    }

    companion object {
        const val EXTRA_LOG_ID = "logId"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SnoozeSheet(
    onSnoozeMinutes: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var showTimePicker by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                ),
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(
                Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = stringResource(R.string.snooze_title),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
                Text(
                    text = stringResource(R.string.snooze_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                Spacer(Modifier.size(4.dp))
                SnoozeButton(minutes = 15, label = stringResource(R.string.snooze_15), onClick = onSnoozeMinutes)
                SnoozeButton(minutes = 30, label = stringResource(R.string.snooze_30), onClick = onSnoozeMinutes)
                SnoozeButton(minutes = 60, label = stringResource(R.string.snooze_60), onClick = onSnoozeMinutes)
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    Text(stringResource(R.string.snooze_custom), style = MaterialTheme.typography.titleMedium)
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }

    if (showTimePicker) {
        val now = remember { Calendar.getInstance() }
        val state = rememberTimePickerState(
            initialHour = (now.get(Calendar.HOUR_OF_DAY) + 1) % 24,
            initialMinute = now.get(Calendar.MINUTE),
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.snooze_pick_time)) },
            text = { TimePicker(state = state) },
            confirmButton = {
                TextButton(onClick = {
                    showTimePicker = false
                    val delay = minutesUntil(state.hour, state.minute)
                    onSnoozeMinutes(delay)
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun SnoozeButton(minutes: Int, label: String, onClick: (Int) -> Unit) {
    Button(
        onClick = { onClick(minutes) },
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, color = Color.White)
    }
}

private fun minutesUntil(hour: Int, minute: Int): Int {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    if (target.timeInMillis <= now.timeInMillis) {
        target.add(Calendar.DAY_OF_YEAR, 1)
    }
    return ((target.timeInMillis - now.timeInMillis) / 60_000L).toInt().coerceAtLeast(1)
}
