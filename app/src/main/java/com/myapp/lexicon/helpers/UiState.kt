package com.myapp.lexicon.helpers

sealed class UiState
{
    data class TextViewCreated(
        val scaleX: Float,
        val scaleY: Float) : UiState()

    data class TextViewAfterAnim(
        val scaleX: Float,
        val scaleY: Float) : UiState()
}
