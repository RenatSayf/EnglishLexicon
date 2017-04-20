package com.myapp.lexicon.wordeditor;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Renat.
 */

class WordEditorFields implements Parcelable
{
    long rowID;
    String oldTextEn;
    String oldTextRu;
    String oldCurrentDict;
    int oldCountRepeat;
    boolean[] searchIsVisible;

    private WordEditorFields(Parcel in)
    {

    }

    WordEditorFields()
    {
        searchIsVisible = new boolean[1];
    }

    public static final Creator<WordEditorFields> CREATOR = new Creator<WordEditorFields>()
    {
        @Override
        public WordEditorFields createFromParcel(Parcel in)
        {
            return new WordEditorFields(in);
        }

        @Override
        public WordEditorFields[] newArray(int size)
        {
            return new WordEditorFields[size];
        }
    };

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i)
    {
        parcel.writeLong(rowID);
        parcel.writeString(oldTextEn);
        parcel.writeString(oldTextRu);
        parcel.writeString(oldCurrentDict);
        parcel.writeInt(oldCountRepeat);
        parcel.writeBooleanArray(searchIsVisible);
    }
}
