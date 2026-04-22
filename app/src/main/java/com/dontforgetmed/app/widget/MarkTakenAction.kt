package com.dontforgetmed.app.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.dontforgetmed.app.DontForgetMedApp
import com.dontforgetmed.app.data.entity.DoseStatus
import com.dontforgetmed.app.notifications.ReminderNotifications
import com.dontforgetmed.app.notifications.StockNotifications

class MarkTakenAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val logId = parameters[LOG_ID_KEY] ?: return
        val app = context.applicationContext as DontForgetMedApp
        val med = app.repository.markDose(logId, DoseStatus.TAKEN)
        ReminderNotifications.cancel(context, logId)
        med?.let { StockNotifications.maybeNotifyLowStock(context, it) }
        NextDoseWidget().update(context, glanceId)
    }

    companion object {
        val LOG_ID_KEY = ActionParameters.Key<Long>("logId")
    }
}
