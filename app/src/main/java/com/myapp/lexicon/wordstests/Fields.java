package com.myapp.lexicon.wordstests;

import android.os.Parcel;
import android.os.Parcelable;

import com.myapp.lexicon.database.DataBaseEntry;

import java.util.ArrayList;

/**
 * Created by Renat.
 */

public class Fields implements Parcelable
{
    public int additonalCount = 0;
    public int wordIndex = 1;
    public float buttonY;
    public float buttonX;
    public int indexEn = -10000;
    public int indexRu = -1;

    public ArrayList<DataBaseEntry> listFromDB;

    public Fields(Parcel in)
    {
    }

    public Fields()
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
        parcel.writeTypedList(listFromDB);
    }
}
