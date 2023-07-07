package com.myapp.lexicon.helpers

object LuhnAlgorithm {

    fun isLuhnChecksumValid(number: String): Boolean {
        var sum = 0
        var isDoubled = false
        for (i in number.length - 1 downTo 0) {
            val digit = number[i].code - '0'.code
            if (digit < 0 || digit > 9) {
                // Ignore non-digits
                continue
            }
            var addend: Int
            if (isDoubled) {
                addend = digit * 2
                if (addend > 9) {
                    addend -= 9
                }
            } else {
                addend = digit
            }
            sum += addend
            isDoubled = !isDoubled
        }
        return if (sum > 0) {
            sum % 10 == 0
        }
        else false
    }
}