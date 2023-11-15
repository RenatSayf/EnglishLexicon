package com.myapp.lexicon.common

enum class OrderBy(val value: String) {
    ASC("lower(english) ASC"),
    DESC("lower(english) DESC"),
    RANDOM("random()")
}