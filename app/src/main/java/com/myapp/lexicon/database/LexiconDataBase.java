package com.myapp.lexicon.database;

import android.content.Context;
import android.database.Cursor;

import com.myapp.lexicon.R;
import com.myapp.lexicon.helpers.StringOperations;
import com.myapp.lexicon.settings.AppSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class LexiconDataBase extends ViewModel
{
    private MutableLiveData<List<String>> dictionaries;
    private DatabaseHelper databaseHelper;

    public LiveData<List<String>> setDictList(Context context)
    {
        if (dictionaries == null)
        {
            dictionaries = new MutableLiveData<>();
            Disposable subscribe = loadDictListAsync(context).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(listMutableLiveData -> dictionaries.postValue(listMutableLiveData), Throwable::printStackTrace);
        }
        return dictionaries;
    }

    private Observable<ArrayList<String>> loadDictListAsync(Context context)
    {
        return Observable.create(emitter -> {
            try
            {
                ArrayList<String> dictList = loadDictList(context);
                emitter.onNext(dictList);
            } catch (Exception e)
            {
                emitter.onError(e);
            } finally
            {
                emitter.onComplete();
            }
        });
    }

    private ArrayList<String> loadDictList(Context context)
    {
        String nameNotDict;
        Cursor cursor = null;
        ArrayList<String> list = new ArrayList<>();
        try
        {
            databaseHelper = new DatabaseHelper(context);
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                cursor = databaseHelper.database.rawQuery("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name", null);
            }

            if (cursor != null && cursor.getCount() > 0)
            {
                if (cursor.moveToFirst())
                {
                    while ( !cursor.isAfterLast() )
                    {
                        nameNotDict = cursor.getString( cursor.getColumnIndex("name"));
                        if (!nameNotDict.equals(DatabaseHelper.TABLE_METADATA) && !nameNotDict.equals(DatabaseHelper.TABLE_SEQUENCE) && !nameNotDict.equals(DatabaseHelper.TABLE_API_KEY))
                        {
                            String table_name = cursor.getString(cursor.getColumnIndex("name"));
                            table_name = StringOperations.getInstance().underscoreToSpace(table_name);
                            list.add(table_name);
                        }
                        cursor.moveToNext();
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            databaseHelper.close();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
            databaseHelper.close();
        }
        MutableLiveData<List<String>> dicts = new MutableLiveData<>();
        AppSettings appSettings = new AppSettings(context);
        if (appSettings.getPlayList() != null)
        {
            int playListSize = appSettings.getPlayList().size();
            int dictNumber = appSettings.getDictNumber();
            if (playListSize > 0 && dictNumber < playListSize)
            {
                String dictName = appSettings.getPlayList().get(dictNumber);
                int dictIndex = list.indexOf(dictName);
                if (dictIndex >= 0 && dictIndex < list.size())
                {
                    Collections.swap(list, dictIndex, 0);
                }
            }
        }
        list.add(context.getString(R.string.text_new_dict));
        //dicts.setValue(list);
        return list;
    }
}
