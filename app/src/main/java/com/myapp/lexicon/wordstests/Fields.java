package com.myapp.lexicon.wordstests;

import android.os.Parcel;
import android.os.Parcelable;

import com.myapp.lexicon.database.DataBaseEntry;

import java.util.ArrayList;

/**
 * Created by Renat.
 */

class Fields implements Parcelable
{
    int additonalCount = 0;
    int wordIndex = 1;
    float buttonY;
    float buttonX;
    int indexEn = -1;
    int indexRu = -1;
    int counterRightAnswer = 0;
    int tempButtonId;
    int oldControlListSize = 0;
    int wordsCount;

    ArrayList<DataBaseEntry> listFromDB;

    private Fields(Parcel in)
    {
    }

    Fields()
    {
        listFromDB = new ArrayList<>();
    }

    public static final Creator<Fields> CREATOR = new Creator<Fields>()
    {
        @Override
        public Fields createFromParcel(Parcel in)
        {
            return new Fields(in);
        }

        @Override
        public Fields[] newArray(int size)
        {
            return new Fields[size];
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
        parcel.writeInt(additonalCount);
        parcel.writeInt(wordIndex);
        parcel.writeFloat(buttonX);
        parcel.writeFloat(buttonY);
        parcel.writeInt(indexEn);
        parcel.writeInt(indexRu);
        parcel.writeInt(counterRightAnswer);
        parcel.writeInt(tempButtonId);
        parcel.writeInt(oldControlListSize);
        parcel.writeInt(wordsCount);

        parcel.writeTypedList(listFromDB);
    }
}
