package com.myapp.lexicon.helpers

sealed class UiState
{
    data class TextViewCreated(
        val scaleX: Float,
        val scaleY: Float) : UiState()

    data class TextViewAfterAnim(
        val scaleX: Float,
        val scaleY: Float) : UiState()

    object AnimStarted : UiState()
    class AnimEnded() : UiState()
    class AnimCanceled() : UiState()

    data class Initial(val state: Int = 1) : UiState()
    data class Active(val state: Int = 2) : UiState()
    data class NotActive(val state: Int = 0) : UiState()
}
