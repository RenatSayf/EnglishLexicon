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
    private boolean is_pause = false;
    private ListViewAdapter listViewAdapter;
    private String langCode;
    private int serviceMode = 0;
    private int doneRepeat = 1;

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
        return is_pause;
    }

    public void setPause(boolean pause)
    {
        is_pause = pause;
    }

    public ListViewAdapter getListViewAdapter()
    {
        return listViewAdapter;
    }

    public void setListViewAdapter(ListViewAdapter listViewAdapter)
    {
        this.listViewAdapter = listViewAdapter;
    }

    public void setTranslateLangCode(String langCode)
    {
        this.langCode = langCode;
    }

    public String getTranslateLangCode()
    {
        return langCode;
    }

    public void saveAllSettings(Context context)
    {
        AppSettings appSettings = new AppSettings(context);
        appSettings.setPause(is_pause);
        appSettings.setDictNumber(ndict);
        appSettings.setWordNumber(nword);
        appSettings.setTranslateLang(langCode);
    }

    public void initAllSettings(Context context)
    {
        AppSettings appSettings = new AppSettings(context);
        is_pause = appSettings.isPause();
        ndict = appSettings.getDictNumber();
        nword = appSettings.getWordNumber();
        langCode = appSettings.getTranslateLang();
    }

    public boolean isAdMob()
    {
        return true;
    }

    public boolean isOnline(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = null;
        if (cm != null)
        {
            netInfo = cm.getActiveNetworkInfo();
        }
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public String getTestDeviceID()
    {
        return "7162b61eda7337bb";
    }

    public boolean testDeviceEnabled()
    {
        return false;
    }

    public int getServiceMode()
    {
        return serviceMode;
    }

    public void setServiceMode(int serviceMode)
    {
        this.serviceMode = serviceMode;
    }

    public int getDoneRepeat()
    {
        return doneRepeat;
    }

    public void setDoneRepeat(int doneRepeat)
    {
        this.doneRepeat = doneRepeat;
    }
}
