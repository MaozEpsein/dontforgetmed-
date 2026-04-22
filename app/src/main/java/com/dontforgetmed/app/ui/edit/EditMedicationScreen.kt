package com.dontforgetmed.app.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
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
    "#6A5AE0", "#9389F0", "#4E3FC4", "#8E24AA", "#FFA6B5", "#E67F92", "#5E72EB", "#546E7A",
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.id == 0L) stringResource(R.string.add_medication)
                        else stringResource(R.string.edit_medication),
                        fontWeight = FontWeight.SemiBold,
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PreviewCard(state)

            SectionCard(title = stringResource(R.string.section_details), icon = Icons.Default.Label) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::setName,
                    label = { Text(stringResource(R.string.field_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
                OutlinedTextField(
                    value = state.dosage,
                    onValueChange = viewModel::setDosage,
                    label = { Text(stringResource(R.string.field_dosage)) },
                    placeholder = { Text(stringResource(R.string.field_dosage_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = viewModel::setNotes,
                    label = { Text(stringResource(R.string.field_notes)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                )
            }

            SectionCard(title = stringResource(R.string.section_look), icon = Icons.Default.Palette) {
                Text(stringResource(R.string.field_icon), style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MedIconCatalog.items.forEach { item ->
                        val selected = item.key == state.iconKey
                        Box(
                            Modifier
                                .size(52.dp)
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
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.field_color), style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PRESET_COLORS.forEach { hex ->
                        val selected = hex.equals(state.colorHex, ignoreCase = true)
                        Box(
                            Modifier
                                .size(40.dp)
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
            }

            SectionCard(title = stringResource(R.string.section_schedule), icon = Icons.Default.Schedule) {
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
                    FrequencyType.EVERY_N_HOURS -> OutlinedTextField(
                        value = state.intervalHours,
                        onValueChange = viewModel::setIntervalHours,
                        label = { Text(stringResource(R.string.field_interval_hours)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                    )
                    FrequencyType.EVERY_N_DAYS -> OutlinedTextField(
                        value = state.intervalDays,
                        onValueChange = viewModel::setIntervalDays,
                        label = { Text(stringResource(R.string.field_interval_days)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                    )
                    else -> {}
                }

                Text(
                    text = if (state.frequencyType == FrequencyType.EVERY_N_HOURS) stringResource(R.string.field_anchor_time)
                    else stringResource(R.string.field_times),
                    style = MaterialTheme.typography.titleSmall,
                )

                state.times.forEachIndexed { index, entry ->
                    TimeRow(
                        entry = entry,
                        showDays = state.frequencyType == FrequencyType.DAILY_AT_TIME,
                        canRemove = state.frequencyType == FrequencyType.DAILY_AT_TIME && state.times.size > 1,
                        onEditTime = { timePickerIndex = index },
                        onRemove = { viewModel.removeTime(index) },
                        onToggleDay = { bit -> viewModel.toggleDay(index, bit) },
                    )
                }

                if (state.frequencyType == FrequencyType.DAILY_AT_TIME) {
                    TextButton(onClick = { showAddTime = true }) {
                        Text(stringResource(R.string.add_time), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            SectionCard(title = stringResource(R.string.section_stock), icon = Icons.Default.Inventory2) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.stockCount,
                        onValueChange = viewModel::setStock,
                        label = { Text(stringResource(R.string.field_stock)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                    )
                    OutlinedTextField(
                        value = state.lowStockThreshold,
                        onValueChange = viewModel::setLowStock,
                        label = { Text(stringResource(R.string.field_low_stock)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = viewModel::save,
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    stringResource(R.string.save),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
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
            onConfirm = { h, m -> viewModel.updateTime(idx, h * 60 + m); timePickerIndex = null },
        )
    }

    if (showAddTime) {
        TimePickerDialog(
            initialHour = 8, initialMinute = 0,
            onDismiss = { showAddTime = false },
            onConfirm = { h, m -> viewModel.addTime(h * 60 + m); showAddTime = false },
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_text)) },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; viewModel.delete() }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun PreviewCard(state: EditUiState) {
    val color = runCatching { Color(android.graphics.Color.parseColor(state.colorHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)
    val timeLabel = state.times.firstOrNull()?.let { Time.formatHm(it.minuteOfDay) } ?: "--:--"
    val name = state.name.ifBlank { stringResource(R.string.preview_name_placeholder) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Row(Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .width(6.dp)
                    .height(74.dp)
                    .background(color),
            )
            Row(
                Modifier.padding(14.dp).weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(46.dp).background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        MedIconCatalog.iconFor(state.iconKey),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (state.dosage.isNotBlank()) {
                        Text(
                            stringResource(R.string.dosage_prefix, state.dosage),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    timeLabel,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            content()
        }
    }
}

@Composable
private fun TimeRow(
    entry: TimeEntry,
    showDays: Boolean,
    canRemove: Boolean,
    onEditTime: () -> Unit,
    onRemove: () -> Unit,
    onToggleDay: (Int) -> Unit,
) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = Time.formatHm(entry.minuteOfDay),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onEditTime() }.padding(8.dp),
                )
                Spacer(Modifier.weight(1f))
                if (canRemove) {
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }
            }
            if (showDays) {
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
                                .clickable { onToggleDay(bit) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        text = { TimePicker(state = state) },
    )
}
