package com.myapp.lexicon.models

import com.myapp.lexicon.database.DataBaseEntry
import java.util.*

data class WordObject(var totalWords: Int)
{
    var minRowId: Int = 0
    var maxRowId: Int = 0
    var studiedWords: Int = 0
    //var totalWords: Int = 0
    var entries: LinkedList<DataBaseEntry>? = null
}