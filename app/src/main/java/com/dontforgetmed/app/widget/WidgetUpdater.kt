package com.dontforgetmed.app.widget

import android.content.Context

object WidgetUpdater {
    suspend fun updateAll(context: Context) {
        runCatching { NextDoseWidget().updateAll(context) }
    }
}
