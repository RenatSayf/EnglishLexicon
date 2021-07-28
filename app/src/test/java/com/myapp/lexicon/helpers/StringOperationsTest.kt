package com.myapp.lexicon.helpers

import com.myapp.lexicon.models.Word
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

class StringOperationsTest
{
    private lateinit var stringOperation: StringOperations
    private var json: String = "[{\"_id\":13,\"countRepeat\":1,\"dictName\":\"Наречия\",\"english\":\"only\",\"translate\":\"только\"},{\"_id\":14,\"countRepeat\":1,\"dictName\":\"Наречия\",\"english\":\"still\",\"translate\":\"до сих пор, по-прежнему\"}]"
    private var json1: String = ""

    private var expectedArrayWord: Array<Word>? = null
    private var actualArrayWord: Array<Word>? = null

    @Before
    fun setUp()
    {
        stringOperation = StringOperations.instance
        val word1 = Word(13, "Наречия", "only", "только", 1)
        val word2 = Word(14, "Наречия", "still", "до сих пор, по-прежнему", 1)
        expectedArrayWord = arrayOf(word1, word2)
        //expectedArrayWord = arrayOf()
    }

    @Test
    fun jsonToWord()
    {
        actualArrayWord = stringOperation.jsonToWord(json1)
        val actual = actualArrayWord is Array<Word>
        val expected = expectedArrayWord is Array<Word>
        assertEquals("**********************", expected, actual)
    }
}