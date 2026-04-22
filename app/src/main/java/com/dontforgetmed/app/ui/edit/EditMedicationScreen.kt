package com.dontforgetmed.app.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dontforgetmed.app.R
import com.dontforgetmed.app.data.entity.FrequencyType
import com.dontforgetmed.app.ui.icons.MedIconCatalog
import com.dontforgetmed.app.util.Time

private val PRESET_COLORS = listOf(
    "#00897B", "#43A047", "#1E88E5", "#8E24AA", "#E53935", "#FB8C00", "#6D4C41", "#546E7A",
)
private val DAY_LABELS = listOf("א", "ב", "ג", "ד", "ה", "ו", "ש")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditMedicationScreen(
    viewModel: EditMedicationViewModel,
    onDone: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) { if (state.saved) onDone() }

    var timePickerIndex by remember { mutableStateOf<Int?>(null) }
    var showAddTime by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.id == 0L) stringResource(R.string.add_medication)
                        else stringResource(R.string.edit_medication)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (state.id > 0) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::setName,
                label = { Text(stringResource(R.string.field_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = state.dosage,
                onValueChange = viewModel::setDosage,
                label = { Text(stringResource(R.string.field_dosage)) },
                placeholder = { Text(stringResource(R.string.field_dosage_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.stockCount,
                    onValueChange = viewModel::setStock,
                    label = { Text(stringResource(R.string.field_stock)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.lowStockThreshold,
                    onValueChange = viewModel::setLowStock,
                    label = { Text(stringResource(R.string.field_low_stock)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
            }

            OutlinedTextField(
                value = state.notes,
                onValueChange = viewModel::setNotes,
                label = { Text(stringResource(R.string.field_notes)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )

            Text(stringResource(R.string.field_icon), style = MaterialTheme.typography.titleMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MedIconCatalog.items.forEach { item ->
                    val selected = item.key == state.iconKey
                    Box(
                        Modifier
                            .size(48.dp)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                                CircleShape,
                            )
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                shape = CircleShape,
                            )
                            .clickable { viewModel.setIcon(item.key) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            Text(stringResource(R.string.field_color), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PRESET_COLORS.forEach { hex ->
                    val selected = hex.equals(state.colorHex, ignoreCase = true)
                    Box(
                        Modifier
                            .size(36.dp)
                            .background(Color(android.graphics.Color.parseColor(hex)), CircleShape)
                            .border(
                                width = if (selected) 3.dp else 0.dp,
                                color = MaterialTheme.colorScheme.onBackground,
                                shape = CircleShape,
                            )
                            .clickable { viewModel.setColor(hex) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.field_frequency), style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.frequencyType == FrequencyType.DAILY_AT_TIME,
                    onClick = { viewModel.setFrequencyType(FrequencyType.DAILY_AT_TIME) },
                    label = { Text(stringResource(R.string.freq_daily)) },
                )
                FilterChip(
                    selected = state.frequencyType == FrequencyType.EVERY_N_HOURS,
                    onClick = { viewModel.setFrequencyType(FrequencyType.EVERY_N_HOURS) },
                    label = { Text(stringResource(R.string.freq_hours)) },
                )
                FilterChip(
                    selected = state.frequencyType == FrequencyType.EVERY_N_DAYS,
                    onClick = { viewModel.setFrequencyType(FrequencyType.EVERY_N_DAYS) },
                    label = { Text(stringResource(R.string.freq_days)) },
                )
            }

            when (state.frequencyType) {
                FrequencyType.EVERY_N_HOURS -> {
                    OutlinedTextField(
                        value = state.intervalHours,
                        onValueChange = viewModel::setIntervalHours,
                        label = { Text(stringResource(R.string.field_interval_hours)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }
                FrequencyType.EVERY_N_DAYS -> {
                    OutlinedTextField(
                        value = state.intervalDays,
                        onValueChange = viewModel::setIntervalDays,
                        label = { Text(stringResource(R.string.field_interval_days)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }
                else -> {}
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = when (state.frequencyType) {
                    FrequencyType.DAILY_AT_TIME -> stringResource(R.string.field_times)
                    FrequencyType.EVERY_N_HOURS -> stringResource(R.string.field_anchor_time)
                    FrequencyType.EVERY_N_DAYS -> stringResource(R.string.field_times)
                },
                style = MaterialTheme.typography.titleMedium,
            )

            state.times.forEachIndexed { index, entry ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = Time.formatHm(entry.minuteOfDay),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { timePickerIndex = index }
                                    .padding(8.dp),
                            )
                            Spacer(Modifier.weight(1f))
                            if (state.frequencyType == FrequencyType.DAILY_AT_TIME && state.times.size > 1) {
                                IconButton(onClick = { viewModel.removeTime(index) }) {
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.remove))
                                }
                            }
                        }
                        if (state.frequencyType == FrequencyType.DAILY_AT_TIME) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                DAY_LABELS.forEachIndexed { dayIdx, label ->
                                    val bit = 1 shl dayIdx
                                    val on = (entry.daysOfWeek and bit) != 0
                                    Box(
                                        Modifier
                                            .size(36.dp)
                                            .background(
                                                if (on) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                CircleShape,
                                            )
                                            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                            .clickable { viewModel.toggleDay(index, bit) },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (state.frequencyType == FrequencyType.DAILY_AT_TIME) {
                TextButton(onClick = { showAddTime = true }) {
                    Text(stringResource(R.string.add_time))
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = viewModel::save,
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(stringResource(R.string.save), style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (timePickerIndex != null) {
        val idx = timePickerIndex!!
        val current = state.times[idx]
        TimePickerDialog(
            initialHour = current.minuteOfDay / 60,
            initialMinute = current.minuteOfDay % 60,
            onDismiss = { timePickerIndex = null },
            onConfirm = { h, m ->
                viewModel.updateTime(idx, h * 60 + m)
                timePickerIndex = null
            },
        )
    }

    if (showAddTime) {
        TimePickerDialog(
            initialHour = 8,
            initialMinute = 0,
            onDismiss = { showAddTime = false },
            onConfirm = { h, m ->
                viewModel.addTime(h * 60 + m)
                showAddTime = false
            },
        )
    }

    if (showDeleteConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_text)) },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; viewModel.delete() }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = true)
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
        text = { TimePicker(state = state) },
    )
}
