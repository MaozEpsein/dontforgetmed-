package com.dontforgetmed.app.util

import java.util.Calendar

object Time {
    /** Start of the local day (midnight) as epoch millis. */
    fun startOfDay(millis: Long = System.currentTimeMillis()): Long {
        val c = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return c.timeInMillis
    }

    fun endOfDay(millis: Long = System.currentTimeMillis()): Long =
        startOfDay(millis) + 24L * 60 * 60 * 1000 - 1

    /** Bit index 0..6 for Sunday..Saturday, matching Calendar.SUNDAY..SATURDAY. */
    fun dayOfWeekBit(millis: Long = System.currentTimeMillis()): Int {
        val c = Calendar.getInstance().apply { timeInMillis = millis }
        // Calendar.SUNDAY = 1 .. SATURDAY = 7
        return 1 shl (c.get(Calendar.DAY_OF_WEEK) - 1)
    }

    fun formatHm(minuteOfDay: Int): String {
        val h = minuteOfDay / 60
        val m = minuteOfDay % 60
        return "%02d:%02d".format(h, m)
    }

    fun scheduledAtToday(minuteOfDay: Int, base: Long = System.currentTimeMillis()): Long =
        startOfDay(base) + minuteOfDay * 60_000L
}
