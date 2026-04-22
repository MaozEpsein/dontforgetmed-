package com.dontforgetmed.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.dontforgetmed.app.DontForgetMedApp
import com.dontforgetmed.app.MainActivity
import com.dontforgetmed.app.data.entity.DoseStatus
import com.dontforgetmed.app.util.Time
import com.dontforgetmed.app.util.TodayPlanner
import kotlinx.coroutines.flow.first

data class NextDoseSnapshot(
    val logId: Long,
    val name: String,
    val dosage: String,
    val colorHex: String,
    val minuteOfDay: Int,
    val scheduledAt: Long,
    val takenToday: Int,
    val totalToday: Int,
    val allDone: Boolean,
)

class NextDoseWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = runCatching { loadSnapshot(context) }.getOrNull()
        provideContent {
            GlanceTheme {
                WidgetContent(snapshot)
            }
        }
    }

    private suspend fun loadSnapshot(context: Context): NextDoseSnapshot? {
        val app = context.applicationContext as DontForgetMedApp
        val repo = app.repository
        TodayPlanner.ensureTodayLogs(repo)

        val meds = repo.medications.first()
        val schedules = repo.activeSchedules.first()
        val logs = repo.dosesBetween(Time.startOfDay(), Time.endOfDay()).first()

        val medById = meds.associateBy { it.id }
        val schedById = schedules.associateBy { it.id }

        val taken = logs.count { it.status == DoseStatus.TAKEN }
        val total = logs.size
        if (total == 0) {
            return NextDoseSnapshot(0, "", "", "#6A5AE0", 0, 0, 0, 0, allDone = false)
        }

        val pending = logs.filter { it.status == DoseStatus.PENDING }.sortedBy { it.scheduledAt }
        val now = System.currentTimeMillis()
        val next = pending.firstOrNull { it.scheduledAt >= now } ?: pending.firstOrNull()
            ?: return NextDoseSnapshot(0, "", "", "#6A5AE0", 0, taken, total, allDone = true)

        val med = medById[next.medicationId] ?: return null
        val sched = schedById[next.scheduleId]
        return NextDoseSnapshot(
            logId = next.id,
            name = med.name,
            dosage = med.dosage,
            colorHex = med.colorHex,
            minuteOfDay = sched?.minuteOfDay ?: 0,
            scheduledAt = next.scheduledAt,
            takenToday = taken,
            totalToday = total,
            allDone = false,
        )
    }
}

@Composable
private fun WidgetContent(snap: NextDoseSnapshot?) {
    val lavender = Color(0xFF6A5AE0)
    val lavenderDeep = Color(0xFF4E3FC4)
    val textLight = Color.White
    val textMuted = Color(0xFFE6E2F3)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(20.dp)
            .background(ColorProvider(lavender))
            .clickable(actionStartActivity<MainActivity>())
            .padding(14.dp),
    ) {
        when {
            snap == null -> CenteredText("טוען…", textLight)
            snap.totalToday == 0 -> EmptyContent(textLight, textMuted)
            snap.allDone -> DoneContent(snap, textLight, textMuted)
            else -> NextContent(snap, textLight, textMuted, lavenderDeep)
        }
    }
}

@Composable
private fun NextContent(snap: NextDoseSnapshot, text: Color, muted: Color, btnBg: Color) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Text(
            text = "התרופה הבאה",
            style = TextStyle(color = ColorProvider(muted), fontSize = 12.sp, fontWeight = FontWeight.Medium),
        )
        Spacer(GlanceModifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = Time.formatHm(snap.minuteOfDay),
                style = TextStyle(color = ColorProvider(text), fontSize = 26.sp, fontWeight = FontWeight.Bold),
            )
            Spacer(GlanceModifier.width(10.dp))
            Column {
                Text(
                    text = snap.name,
                    style = TextStyle(color = ColorProvider(text), fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    maxLines = 1,
                )
                if (snap.dosage.isNotBlank()) {
                    Text(
                        text = "מינון: ${snap.dosage}",
                        style = TextStyle(color = ColorProvider(muted), fontSize = 12.sp),
                        maxLines = 1,
                    )
                }
            }
        }
        Spacer(GlanceModifier.height(10.dp))
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(40.dp)
                .cornerRadius(12.dp)
                .background(ColorProvider(Color.White))
                .clickable(
                    actionRunCallback<MarkTakenAction>(
                        parameters = actionParametersOf(MarkTakenAction.LOG_ID_KEY to snap.logId),
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "✓  נלקח",
                style = TextStyle(color = ColorProvider(btnBg), fontSize = 16.sp, fontWeight = FontWeight.Bold),
            )
        }
        Spacer(GlanceModifier.height(6.dp))
        Text(
            text = "${snap.takenToday}/${snap.totalToday} להיום",
            style = TextStyle(color = ColorProvider(muted), fontSize = 11.sp),
        )
    }
}

@Composable
private fun DoneContent(snap: NextDoseSnapshot, text: Color, muted: Color) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "🎉", style = TextStyle(color = ColorProvider(text), fontSize = 28.sp))
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = "סיימת להיום!",
            style = TextStyle(color = ColorProvider(text), fontSize = 16.sp, fontWeight = FontWeight.Bold),
        )
        Text(
            text = "${snap.takenToday}/${snap.totalToday}",
            style = TextStyle(color = ColorProvider(muted), fontSize = 12.sp),
        )
    }
}

@Composable
private fun EmptyContent(text: Color, muted: Color) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "אין תרופות להיום",
            style = TextStyle(color = ColorProvider(text), fontSize = 15.sp, fontWeight = FontWeight.Bold),
        )
        Text(
            text = "הקש להוספה",
            style = TextStyle(color = ColorProvider(muted), fontSize = 12.sp),
        )
    }
}

@Composable
private fun CenteredText(text: String, color: Color) {
    Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text, style = TextStyle(color = ColorProvider(color), fontSize = 14.sp))
    }
}
