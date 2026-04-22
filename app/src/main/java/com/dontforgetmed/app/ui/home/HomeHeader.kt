package com.dontforgetmed.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dontforgetmed.app.R
import com.dontforgetmed.app.ui.theme.Coral
import com.dontforgetmed.app.ui.theme.MintLight
import com.dontforgetmed.app.ui.theme.TealDeep
import com.dontforgetmed.app.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeHeader(
    takenCount: Int,
    totalCount: Int,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = if (totalCount > 0) takenCount.toFloat() / totalCount else 0f
    val gradient = Brush.linearGradient(
        colors = listOf(TealDeep, TealPrimary, MintLight),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(brush = gradient, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = greetingFor(),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = todayString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f),
                    )
                }
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                        .size(44.dp),
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings),
                        tint = Color.White,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                ProgressRing(
                    progress = progress,
                    taken = takenCount,
                    total = totalCount,
                    size = 120.dp,
                    primaryColor = Color.White,
                    secondaryColor = Coral,
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        text = headlineFor(takenCount, totalCount),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = subtitleFor(takenCount, totalCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

private fun greetingFor(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "בוקר טוב ☀️"
        in 12..16 -> "צהריים טובים"
        in 17..20 -> "ערב טוב 🌇"
        else -> "לילה טוב 🌙"
    }
}

private fun todayString(): String {
    val formatter = SimpleDateFormat("EEEE, d בMMMM", Locale("he"))
    return formatter.format(Calendar.getInstance().time)
}

private fun headlineFor(taken: Int, total: Int): String = when {
    total == 0 -> "היום חופשי 🌿"
    taken == total -> "כל הכבוד! 🎉"
    taken == 0 -> "יום חדש מתחיל"
    else -> "התקדמות יפה"
}

private fun subtitleFor(taken: Int, total: Int): String = when {
    total == 0 -> "אין תרופות להיום. תוסיפי חדשה בלחיצה על +"
    taken == total -> "סיימת את כל התרופות להיום"
    taken == 0 -> "עוד $total תרופות להיום"
    else -> {
        val remaining = total - taken
        "עוד $remaining ל${if (remaining == 1) "התרופה האחרונה" else "סיום"}"
    }
}
