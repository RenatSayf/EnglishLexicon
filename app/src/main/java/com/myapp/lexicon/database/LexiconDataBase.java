package com.myapp.lexicon.database;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

public class LexiconDataBase extends ViewModel
{
    private MutableLiveData<List<String>> dictionaries;
    public LiveData<List<String>> getDictList()
    {
        if (dictionaries == null)
        {
            dictionaries = new MutableLiveData<List<String>>();
            loadDictList();
        }
        return dictionaries;
    }

    private void loadDictList()
    {
        return;
    }
}
