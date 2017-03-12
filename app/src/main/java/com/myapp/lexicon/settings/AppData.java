package com.myapp.lexicon.settings;

import android.content.Context;

/**
 * Created by Ренат on 20.04.2016.
 */
public class AppData
{
    private static AppSettings appSettings;
    private Context context;
    private static int ndict;
    private static int nword = 1;
    private static boolean isPause = false;

    public AppData(Context context)
    {
        appSettings = new AppSettings(context);
    }

    public static int get_Ndict()
    {
        return ndict;
    }

    public static void set_Ndict(int Ndict)
    {
        ndict = Ndict;
    }

    public static int get_Nword()
    {
        return nword;
    }

    public static void set_Nword(int Nword)
    {
        nword = Nword;
    }

    public static boolean isPause()
    {
        return isPause;
    }

    public static void setPause(boolean param)
    {
        isPause = param;
    }



    public static void saveAllSettings()
    {
        appSettings.setPause(isPause);
        appSettings.setDictNumber(ndict);
        appSettings.setWordNumber(nword);
    }

    public static void initAllSettings()
    {
        isPause = appSettings.isPause();
        ndict = appSettings.getDictNumber();
        nword = appSettings.getWordNumber();
    }

}
