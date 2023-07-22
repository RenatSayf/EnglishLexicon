package com.myapp.lexicon.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(tableName = "Words")
data class Word(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var _id: Int,

    @ColumnInfo(name = "dict_name")
    var dictName: String,

    @ColumnInfo(name = "english")
    var english: String,

    @ColumnInfo(name = "translate")
    var translate: String,

    @ColumnInfo(name = "count_repeat")
    var countRepeat: Int
) : Parcelable
{
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readInt()
    )

    override fun describeContents(): Int
    {
        return 0
    }

    override fun writeToParcel(p0: Parcel, p1: Int)
    {
        p0.writeInt(_id)
        p0.writeString(dictName)
        p0.writeString(english)
        p0.writeString(translate)
        p0.writeInt(countRepeat)
    }

    companion object CREATOR : Parcelable.Creator<Word>
    {
        override fun createFromParcel(parcel: Parcel): Word
        {
            return Word(parcel)
        }

        override fun newArray(size: Int): Array<Word?>
        {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "id=${this._id}|dict=${this.dictName}|en=${this.english}|tr=${this.translate}|count=${this.countRepeat}"
    }

}

fun String.toWord(): Word {

    val list = this.split("|")
    var word = Word(-1, "", "", "", 0)
    list.forEach { item ->
        val subList = item.split("=")
        when {
            subList.contains("id") -> {
                word._id = subList[1].toInt()
            }
            subList.contains("dict") -> {
                word.dictName = subList[1]
            }
            subList.contains("en") -> {
                word.english = subList[1]
            }
            subList.contains("tr") -> {
                word.translate = subList[1]
            }
            subList.contains("count") -> {
                word.countRepeat = subList[1].toInt()
            }
        }
    }
    if (word._id < 0) {
        val jsonObject = JSONObject(this)
        val id = jsonObject.getInt("_id")
        val dict = jsonObject.getString("dictName")
        val english = jsonObject.getString("english")
        val translate = jsonObject.getString("translate")
        val countRepeat = jsonObject.getInt("countRepeat")
        word = Word(id, dict, english, translate, countRepeat)
    }
    return word
}

fun List<Word>.toWordsString(): String {
    var string = ""
    this.forEach { word ->
        string += "$word+"
    }
    return string.trim { it.toString() == "+" }
}

fun String.toWordList(): List<Word> {
    val stringList = this.split("+")
    val wordList = stringList.map {
        it.toWord()
    }
    return wordList
}