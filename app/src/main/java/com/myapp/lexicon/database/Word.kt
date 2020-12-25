package com.myapp.lexicon.database

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Words")
data class Word(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val _id: Int,

    @ColumnInfo(name = "dict_name")
    val dictName: String,

    @ColumnInfo(name = "english")
    val english: String,

    @ColumnInfo(name = "translate")
    val translate: String,

    @ColumnInfo(name = "count_repeat")
    val countRepeat: Int
) : Parcelable
{
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readInt())
    {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int)
    {
        parcel.writeInt(_id)
        parcel.writeString(dictName)
        parcel.writeString(english)
        parcel.writeString(translate)
        parcel.writeInt(countRepeat)
    }

    override fun describeContents(): Int
    {
        return this.hashCode()
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
}
