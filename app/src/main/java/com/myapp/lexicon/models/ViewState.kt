package com.myapp.lexicon.models

import android.graphics.drawable.Drawable
import android.view.View

data class ViewState(
    val text: String = "",
    val isEnabled: Boolean = false,
    val visibility: Int = View.GONE,
    val isFocused: Boolean = false,
    val background: Drawable? = null
)