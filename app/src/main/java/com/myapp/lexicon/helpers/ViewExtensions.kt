package com.myapp.lexicon.helpers

import android.view.View


fun View.onVisibilityChanged(
    onVisible: () -> Unit = {},
    onInvisible: () -> Unit = {},
    onChanged: (View) -> Unit = {}
) {
    this.viewTreeObserver.addOnGlobalLayoutListener {
        when(this.visibility) {
            View.VISIBLE -> {
                onVisible.invoke()
            }
            View.GONE, View.INVISIBLE -> {
                onInvisible.invoke()
            }
        }
        onChanged.invoke(this)
    }
}