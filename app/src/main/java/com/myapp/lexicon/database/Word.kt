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
        TODO("Not yet implemented")
    }

    override fun writeToParcel(p0: Parcel?, p1: Int)
    {
        TODO("Not yet implemented")
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

