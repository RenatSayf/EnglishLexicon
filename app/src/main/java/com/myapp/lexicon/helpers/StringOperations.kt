package com.myapp.lexicon.helpers

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.myapp.lexicon.R
import com.myapp.lexicon.database.Word
import java.util.ArrayList

/**
 * Performs various operations on strings
 */
class StringOperations private constructor()
{
    fun getLangOfText(context: Context, text: String): Array<String?>
    {
        val lang = arrayOfNulls<String>(2)
        try
        {
            for (i in text.indices)
            {
                val charFirst = text.codePointAt(0)
                if (charFirst in 33..64 || text.codePointAt(i) in 91..96 || text.codePointAt(i) in 123..126)
                {
                    continue
                }
                when
                {
                    text.codePointAt(i) in 1025..1105 ->
                    {
                        lang[0] = context.getString(R.string.translate_direct_ru_en)
                        lang[1] = context.getString(R.string.translate_lang_ru)
                    }
                    text.codePointAt(i) in 65..122 ->
                    {
                        lang[0] = context.getString(R.string.translate_direct_en_ru)
                        lang[1] = context.getString(R.string.translate_lang_en)
                    }
                    else ->
                    {
                        lang[0] = null
                        lang[1] = null
                    }
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return lang
    }

    fun jsonToWord(json: String) : Array<Word>
    {
        return try
        {
            val jsonType = TypeToken.get(Array<Word>::class.java).type
            Gson().fromJson(json, jsonType)
        }
        catch (e: JsonSyntaxException)
        {
            e.printStackTrace()
            arrayOf()
        }
    }

    fun spaceToUnderscore(text: String?): String
    {
        return if (text != null)
        {
            val name = text.trim { it <= ' ' }
            name.replace(' ', '_')
        }
        else
        {
            ""
        }
    }

    fun underscoreToSpace(text: String?): String
    {
        return if (text != null)
        {
            val name = text.trim { it <= ' ' }
            name.replace('_', ' ')
        }
        else
        {
            ""
        }
    }

    companion object
    {
        private var ourInstance: StringOperations = StringOperations()
        @JvmStatic
        val instance: StringOperations
            get()
            {
                return ourInstance
            }
    }
}