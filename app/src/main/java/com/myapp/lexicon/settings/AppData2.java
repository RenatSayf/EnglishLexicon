package com.myapp.lexicon.settings;

import android.content.Context;

/**
 * Created by Renat on 08.03.2017.
 */

public class AppData2
{
    private static final AppData2 ourInstance = new AppData2();
    private Context context;
    private int ndict;
    private int nword = 1;
    private boolean isPause = false;

    public static AppData2 getInstance()
    {
        return ourInstance;
    }

    private AppData2()
    {

    }

    public void setContext(Context context)
    {
        this.context = context;
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

    public void saveAllSettings()
    {
        AppSettings appSettings = new AppSettings(context);
        appSettings.setPause(isPause);
        appSettings.setDictNumber(ndict);
        appSettings.setWordNumber(nword);
    }

    public void initAllSettings()
    {
        AppSettings appSettings = new AppSettings(context);
        isPause = appSettings.isPause();
        ndict = appSettings.getDictNumber();
        nword = appSettings.getWordNumber();
    }
}
