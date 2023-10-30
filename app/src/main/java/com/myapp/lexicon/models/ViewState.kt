package com.myapp.lexicon.models

import android.graphics.drawable.Drawable

data class ViewState(
    val text: String,
    val isEnabled: Boolean,
    val visibility: Int,
    val isFocused: Boolean,
    val background: Drawable
)