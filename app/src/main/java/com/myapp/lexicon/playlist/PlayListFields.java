package com.myapp.lexicon.playlist;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class PlayListFields implements Parcelable
{
    ArrayList<String> newPlayList;

    private PlayListFields(Parcel in)
    {
    }

    PlayListFields()
    {
        newPlayList = new ArrayList<>();
    }

    public static final Creator<PlayListFields> CREATOR = new Creator<PlayListFields>()
    {
        @Override
        public PlayListFields createFromParcel(Parcel in)
        {
            return new PlayListFields(in);
        }

        @Override
        public PlayListFields[] newArray(int size)
        {
            return new PlayListFields[size];
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
        parcel.writeStringList(newPlayList);
    }
}
