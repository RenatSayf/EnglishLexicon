package com.myapp.lexicon.wordeditor;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

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
    int amountWords = 0;
    boolean[] searchIsVisible;
    String queryString;
    ArrayList<String> dictNames;

    private WordEditorFields(@SuppressWarnings("unused") Parcel in)
    {

    }

    WordEditorFields()
    {
        searchIsVisible = new boolean[1];
        searchIsVisible[0] = true;
        dictNames = new ArrayList<>();
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
        parcel.writeString(queryString);
        parcel.writeInt(oldCountRepeat);
        parcel.writeInt(amountWords);
        parcel.writeBooleanArray(searchIsVisible);
        parcel.writeStringList(dictNames);
    }
}
