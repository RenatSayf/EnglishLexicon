package com.myapp.lexicon.helpers

import com.myapp.lexicon.common.APP_TIME_ZONE
import org.junit.Assert
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


class ExtensionsKtTest {

    private val timeZoneId = "Europe/Moscow"


    @Test
    fun getCRC32CheckSum() {

        val actualCheckSum =
            "bjppaehkgdeldlcnmhnmiaeo.AO-J1OyRVjp1kZ3A2yGhxnfeDKo14VHnix1KJnN-xq5Ee_wFGtzp9YDkSCveWgRaUJ088qhvezU97VSL4_kq_d1u-Nwax1s_MQ/".getCRC32CheckSum()
        Assert.assertEquals(2100595828L, actualCheckSum)
    }

    @Test
    fun getCRC32CheckSum_empty_string() {

        val actualCheckSum = "".getCRC32CheckSum()
        Assert.assertEquals(0L, actualCheckSum)
    }

    @Test
    fun toStringDate() {
        val timeMillis = 1688378419048L
        val actualDate = timeMillis.toStringDate(Locale("ru", "RU"))
        Assert.assertEquals("2023-07-03", actualDate)
    }

    @Test
    fun toLongDate_Success() {
        val inputStrDate = "2023-07-03"
        val actualResult = inputStrDate.toLongDate()
        val actualStrDate = actualResult.toStringDate()
        Assert.assertEquals(inputStrDate, actualStrDate)
    }

    @Test
    fun toLongDate_Wrong_input() {
        val dataStr = "Xxxxxx"
        val actualResult = dataStr.toLongDate()
        Assert.assertTrue(actualResult < 0)
    }

    @Test
    fun toLongDate_Compare_two_dates() {
        val dataStr = "2023-07-03"
        val actualResult = dataStr.toLongDate()
        val currentTimeMillis = System.currentTimeMillis()
        Assert.assertTrue(actualResult < currentTimeMillis)
    }

    @Test
    fun isTodayDate() {
        var inputTime = "2024-06-09 23:59:59"
        var actualResult = inputTime.isTodayDate()
        Assert.assertTrue(!actualResult)

        inputTime = (System.currentTimeMillis() + TimeUnit.HOURS.toMillis(3)).toStringTime()
        actualResult = inputTime.isTodayDate()
        Assert.assertTrue(actualResult)

        inputTime = ""
        actualResult = inputTime.isTodayDate()
        Assert.assertTrue(!actualResult)
    }

    @Test
    fun isYesterday() {
        val currentTime = "2024-06-10 00:00:01".toLongTime()
        var inputTime = "2024-06-09 23:59:59"
        var actualResult = inputTime.isYesterday(currentTime)
        Assert.assertTrue(actualResult)

        inputTime = (System.currentTimeMillis() + TimeUnit.HOURS.toMillis(3)).toStringTime()
        actualResult = inputTime.isYesterday(currentTime)
        Assert.assertTrue(!actualResult)

        inputTime = ""
        actualResult = inputTime.isTodayDate(currentTime)
        Assert.assertTrue(!actualResult)
    }

    @Test
    fun dayOfMonthFromStrTime() {
        val inputTime = "2024-06-09 23:59:59"
        val actualDay = inputTime.dayOfMonthFromStrTime()
        Assert.assertEquals(9, actualDay)
    }

    @Test
    fun isDateOfLastMonth() {

        var inputDate = "2024-08-17 09:09:56"
        var actualResult = inputDate.isDateOfLastMonth(currentMonth = 9, timeZoneId = timeZoneId)
        Assert.assertTrue(actualResult)

        inputDate = "2024-08-17 09:09:56"
        actualResult = inputDate.isDateOfLastMonth(currentMonth = 8, timeZoneId = timeZoneId)
        Assert.assertTrue(!actualResult)

        inputDate = "sgfdf;dslp"
        actualResult = inputDate.isDateOfLastMonth(currentMonth = 9, timeZoneId = timeZoneId)
        Assert.assertTrue(actualResult)
    }

    @Test
    fun isTodayFirstDayOfMonth() {
        var inputDate = "2024-08-01 09:09:56".toLongTime()
        var actualResult = isTodayFirstDayOfMonth(timeInMillis = inputDate)
        Assert.assertTrue(actualResult)

        inputDate = "2024-09-01 09:09:56".toLongTime()
        actualResult = isTodayFirstDayOfMonth(timeInMillis = inputDate)
        Assert.assertTrue(actualResult)

        inputDate = "2024-09-10 09:09:56".toLongTime()
        actualResult = isTodayFirstDayOfMonth(timeInMillis = inputDate)
        Assert.assertTrue(!actualResult)
    }

    @Test
    fun timeInMillisMoscowTimeZone_s() {
        val timeInMillis = timeInMillisMoscowTimeZone

        val actualTime = timeInMillis.toStringTime()

        val instant = LocalDateTime.ofInstant(Instant.now(), ZoneId.of(APP_TIME_ZONE))

        val sysTime = System.currentTimeMillis().toStringTime()

        val time = Date().time.toStringTime()

        Assert.assertTrue(true)
    }



}