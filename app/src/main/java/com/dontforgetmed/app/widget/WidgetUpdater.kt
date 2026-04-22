package com.dontforgetmed.app.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

object WidgetUpdater {
    suspend fun updateAll(context: Context) {
        runCatching { NextDoseWidget().updateAll(context) }
    }
}
