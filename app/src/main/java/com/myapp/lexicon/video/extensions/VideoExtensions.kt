package com.myapp.lexicon.video.extensions

import android.app.SearchManager
import android.database.Cursor
import android.database.MatrixCursor
import android.provider.BaseColumns
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

fun SearchView.updateAdapter(list: List<String>){
    val cursor = MatrixCursor(arrayOf(BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1))
    list.forEachIndexed { index, text ->
        cursor.addRow(arrayOf(index, text))
    }
    (this.suggestionsAdapter as SimpleCursorAdapter).changeCursor(cursor)
}

fun SearchView.getSelectedSuggestion(
    position: Int,
    onSelection: (selection: String) -> Unit
) {
    val cursor = this.suggestionsAdapter.getItem(position) as Cursor
    val columnIndex = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)
    if (columnIndex >= 0) {
        val selection = cursor.getString(columnIndex)
        this.setQuery(selection, false)
        onSelection.invoke(selection)
    }
}