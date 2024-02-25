package com.myapp.lexicon.video.extensions

import android.app.SearchManager
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter


fun SearchView.createAdapter(): SimpleCursorAdapter {
    return SimpleCursorAdapter(
        this.context,
        android.R.layout.simple_list_item_1,
        null,
        arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1),
        intArrayOf(android.R.id.text1),
        CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
    )
}