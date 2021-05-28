package com.myapp.lexicon.helpers

sealed class UiState
{
    data class TextViewCreated(
        val scaleX: Float,
        val scaleY: Float) : UiState()

    data class TextViewAfterAnim(
        val scaleX: Float,
        val scaleY: Float) : UiState()

    class AnimStarted() : UiState()
    class AnimEnded() : UiState()
    class AnimCanceled() : UiState()
}
