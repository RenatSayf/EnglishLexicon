package com.myapp.lexicon.models

import org.junit.Assert
import org.junit.Test

class WordTest {

    @Test
    fun testToString() {
        val word = Word(
            _id = 1,
            dictName = "Xxxx",
            english = "test",
            translate = "тест",
            countRepeat = 1
        )
        val string = word.toString()
        Assert.assertEquals("id=1|dict=Xxxx|en=test|tr=тест|count=1", string)
    }

    @Test
    fun toWord() {
        val string = "id=1|dict=Xxxx|en=test|tr=тест|count=1"
        val word = string.toWord()
        Assert.assertEquals(1, word._id)
    }

    @Test
    fun toWordsString() {
        val word1 = Word(
            _id = 1,
            dictName = "Xxxx",
            english = "test",
            translate = "тест",
            countRepeat = 1
        )
        val word2 = Word(
            _id = 2,
            dictName = "Yyyy",
            english = "test",
            translate = "тест",
            countRepeat = 1
        )
        val wordList = listOf(
            word1,
            word2
        )
        val wordsString = wordList.toWordsString()
        Assert.assertEquals("id=1|dict=Xxxx|en=test|tr=тест|count=1+id=2|dict=Yyyy|en=test|tr=тест|count=1", wordsString)
    }

    @Test
    fun toWordList() {

        val inputString = "id=1|dict=Xxxx|en=test|tr=тест|count=1+id=2|dict=Yyyy|en=test|tr=тест|count=1"
        val wordList = inputString.toWordList()
        Assert.assertEquals(1, wordList[0]._id)
        Assert.assertEquals(2, wordList[1]._id)
    }

}