package com.myapp.lexicon.helpers

import org.junit.Assert
import org.junit.Test

class LuhnAlgorithmTest {

    @Test
    fun isLuhnChecksumValid_valid_number() {
        val inputNumber = "4276 1605 0176 0764"
        val actualResult = LuhnAlgorithm.isLuhnChecksumValid(inputNumber)
        Assert.assertEquals(true, actualResult)
    }

    @Test
    fun isLuhnChecksumValid_not_valid_number() {
        val inputNumber = "2564 8963 5478 6354"
        val actualResult = LuhnAlgorithm.isLuhnChecksumValid(inputNumber)
        Assert.assertEquals(false, actualResult)
    }

    @Test
    fun isLuhnChecksumValid_all_zero() {
        val inputNumber = "0000 0000 0000 0000"
        val actualResult = LuhnAlgorithm.isLuhnChecksumValid(inputNumber)
        Assert.assertEquals(false, actualResult)
    }

    @Test
    fun isLuhnChecksumValid_incorrect_number() {
        val inputNumber = "554545455 54544"
        val actualResult = LuhnAlgorithm.isLuhnChecksumValid(inputNumber)
        Assert.assertEquals(false, actualResult)
    }
}