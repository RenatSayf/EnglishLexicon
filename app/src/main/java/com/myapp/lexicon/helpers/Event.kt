package com.myapp.lexicon.helpers

//todo Отправка события в активити/фрагмент: Step 1
open class Event<out T>(private val content: T)
{
    var hasBeenHandled = false
        private set(value) // Allow external read but not write
        {
            field = value
        }

    /**
     * Returns the content and prevents its use again.
     */
    fun getContent(): T?
    {
        return if (hasBeenHandled)
        {
            null
        }
        else
        {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}