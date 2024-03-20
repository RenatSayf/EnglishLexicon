@file:Suppress("ObjectLiteralToLambda")

package com.myapp.lexicon.video.extensions

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.SearchManager
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.provider.BaseColumns
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView


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


private var playerInstance: YouTubePlayerView? = null
fun Context.youTubePlayerView(): YouTubePlayerView {
    if (playerInstance == null) {
        playerInstance = YouTubePlayerView(this)
    }
    return playerInstance!!.apply {
        enableAutomaticInitialization = false

    }
}

fun LinearLayoutCompat.changeHeightAnimatedly(
    value: Int = this.height,
    onEnd: (isVisible: Boolean) -> Unit = {}
) {
    val layoutParams = this@changeHeightAnimatedly.layoutParams as ConstraintLayout.LayoutParams
    val paramToAnimate = layoutParams.height
    ValueAnimator.ofInt((paramToAnimate), value).apply {
        addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(animator: ValueAnimator) {
                layoutParams.height = animator.animatedValue as Int
                this@changeHeightAnimatedly.layoutParams = layoutParams
            }
        })
        addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {

            }

            override fun onAnimationEnd(p0: Animator) {
                val animValue = (p0 as ValueAnimator).animatedValue as Int
                //"*********** animValue = $animValue ****************".logIfDebug()
                if (animValue == this@changeHeightAnimatedly.height) {
                    onEnd.invoke(true)
                }
                else {
                    onEnd.invoke(false)
                }
            }

            override fun onAnimationCancel(p0: Animator) {}

            override fun onAnimationRepeat(p0: Animator) {}

        })
    }.setDuration(200).start()
}