package com.myapp.lexicon.common

import com.myapp.lexicon.helpers.firstCap
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.TextStyle
import java.util.Locale

fun Long.getMonthNameFromMillis(): String {
    val monthName = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneOffset.UTC)
        .month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
    return monthName.firstCap()
}

fun Long.getPreviousMonthNameFromMillis(): String {
    val monthName = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneOffset.UTC)
        .minusMonths(1)
        .month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
    return monthName.firstCap()
}