package com.myapp.lexicon.settings;

import android.content.Context;

import com.myapp.lexicon.wordeditor.ListViewAdapter;

/**
 * Storing intermediate data of the application
 */

public class AppData2
{
    private static final AppData2 ourInstance = new AppData2();
    private int ndict;
    private int nword = 1;
    private boolean isPause = false;
    private ListViewAdapter listViewAdapter;

    public static AppData2 getInstance()
    {
        return ourInstance;
    }

    private AppData2()
    {

    }

    public int getNdict()
    {
        return ndict;
    }

    public void setNdict(int ndict)
    {
        this.ndict = ndict;
    }

    public int getNword()
    {
        return nword;
    }

    public void setNword(int nword)
    {
        this.nword = nword;
    }

    public boolean isPause()
    {
        return isPause;
    }

    public void setPause(boolean pause)
    {
        isPause = pause;
    }

    public ListViewAdapter getListViewAdapter()
    {
        return listViewAdapter;
    }

    public void setListViewAdapter(ListViewAdapter listViewAdapter)
    {
        this.listViewAdapter = listViewAdapter;
    }

    public void saveAllSettings(Context context)
    {
        AppSettings appSettings = new AppSettings(context);
        appSettings.setPause(isPause);
        appSettings.setDictNumber(ndict);
        appSettings.setWordNumber(nword);
    }

    public void initAllSettings(Context context)
    {
        AppSettings appSettings = new AppSettings(context);
        isPause = appSettings.isPause();
        ndict = appSettings.getDictNumber();
        nword = appSettings.getWordNumber();
    }
}
