package com.dontforgetmed.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dontforgetmed.app.R

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddMedication: () -> Unit,
    onEditMedication: (Long) -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    val doses by viewModel.doses.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }
    var showCelebration by remember { mutableStateOf(false) }
    var lastCelebratedKey by remember { mutableStateOf(0 to 0) }

    LaunchedEffect(stats.taken, stats.total) {
        val current = stats.taken to stats.total
        if (stats.total > 0 && stats.taken == stats.total && current != lastCelebratedKey) {
            lastCelebratedKey = current
            showCelebration = true
        }
    }
    val haptic = LocalHapticFeedback.current
    val takenMessage = stringResource(R.string.dose_marked_taken)
    val skippedMessage = stringResource(R.string.dose_marked_skipped)
    val undoLabel = stringResource(R.string.undo)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.DoseMarked -> {
                    val result = snackbarHost.showSnackbar(
                        message = if (event.takenLabel) takenMessage else skippedMessage,
                        actionLabel = undoLabel,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undo(event.logId)
                    }
                }
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddMedication,
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_medication), fontWeight = FontWeight.SemiBold) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .navigationBarsPadding(),
        ) {
            HomeHeader(
                takenCount = stats.taken,
                totalCount = stats.total,
                onOpenSettings = onOpenSettings,
            )

            if (doses.isEmpty()) {
                EmptyState(Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(doses, key = { it.log.id }) { dose ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(280)) + slideInVertically(tween(280)) { it / 4 },
                            exit = fadeOut(tween(180)),
                        ) {
                            DoseCard(
                                dose = dose,
                                onTake = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.markTaken(dose.log)
                                },
                                onSkip = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.markSkipped(dose.log)
                                },
                                onEdit = { onEditMedication(dose.medication.id) },
                            )
                        }
                    }
                }
            }
        }
    }
    CelebrationOverlay(visible = showCelebration, onDismiss = { showCelebration = false })
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.illustration_empty),
                contentDescription = null,
                modifier = Modifier.size(200.dp),
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.empty_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.empty_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
