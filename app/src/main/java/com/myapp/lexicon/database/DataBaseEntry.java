package com.myapp.lexicon.database;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class DataBaseEntry implements Parcelable, Serializable
{
    private String english;
    private String translate;
    private String countRepeat;
    private int rowId;

    public DataBaseEntry(String english, String translate)
    {
        this.english = english;
        this.translate = translate;
    }

    public DataBaseEntry(String english, String translate, String count_repeat)
    {
        this.english = english;
        this.translate = translate;
        this.countRepeat = count_repeat;
    }

    public DataBaseEntry(int rowId, String english, String translate, String count_repeat)
    {
        this.rowId = rowId;
        this.english = english;
        this.translate = translate;
        this.countRepeat = count_repeat;
    }

    protected DataBaseEntry(Parcel in)
    {
        rowId = in.readInt();
        english = in.readString();
        translate = in.readString();
        countRepeat = in.readString();
    }

    public static final Creator<DataBaseEntry> CREATOR = new Creator<DataBaseEntry>()
    {
        @Override
        public DataBaseEntry createFromParcel(Parcel in)
        {
            return new DataBaseEntry(in);
        }

        @Override
        public DataBaseEntry[] newArray(int size)
        {
            return new DataBaseEntry[size];
        }
    };

    public String getEnglish()
    {
        return english;
    }

    public void setEnglish(String english)
    {
        this.english = english;
    }

    public String getTranslate()
    {
        return translate;
    }

    public void setTranslate(String translate)
    {
        this.translate = translate;
    }

    public String getCountRepeat()
    {
        return countRepeat;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i)
    {
        parcel.writeInt(rowId);
        parcel.writeString(english);
        parcel.writeString(translate);
        parcel.writeString(countRepeat);
    }

    public int getRowId()
    {
        return rowId;
    }

}
