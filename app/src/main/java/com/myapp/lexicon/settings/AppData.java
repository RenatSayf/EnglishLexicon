package com.myapp.lexicon.settings;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.myapp.lexicon.wordeditor.ListViewAdapter;

/**
 * Storing intermediate data of the application
 */

public class AppData
{
    private static final AppData ourInstance = new AppData();
    private int ndict;
    private int nword = 1;
    private boolean isPause = false;
    private ListViewAdapter listViewAdapter;

    public static AppData getInstance()
    {
        return ourInstance;
    }

    private AppData()
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

    public boolean isAdMob()
    {
        return true;
    }

    public boolean isOnline(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
