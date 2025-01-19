package com.myapp.lexicon.common

import com.myapp.lexicon.helpers.firstCap
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.TextStyle
import java.util.Locale

fun Long.getMonthNameFromMillis(): String {
    try {
        val monthName = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneOffset.UTC)
            .month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
        return monthName.firstCap()
    } catch (e: Exception) {
        e.printStackTraceIfDebug()
        return ""
    }
}

fun Long.getPreviousMonthNameFromMillis(): String {
    try {
        val monthName = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneOffset.UTC)
            .minusMonths(1)
            .month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
        return monthName.firstCap()
    } catch (e: Exception) {
        e.printStackTraceIfDebug()
        return ""
    }
}