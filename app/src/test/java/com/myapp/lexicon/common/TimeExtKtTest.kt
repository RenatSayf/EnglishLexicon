package com.myapp.lexicon.common

import com.myapp.lexicon.helpers.toLongTime
import org.junit.Assert
import org.junit.Test


class TimeExtKtTest {

    @Test
    fun getMonthNameFromMillis() {

        val time = "2024-01-13 16:03:40".toLongTime()
        val month = time.getMonthNameFromMillis()

        Assert.assertEquals("Январь", month)
    }

    @Test
    fun getPreviousMonthNameFromMillis() {
        val time = "2023-12-13 16:03:40".toLongTime()
        val month = time.getPreviousMonthNameFromMillis()

        Assert.assertEquals("Декабрь", month)
    }
}