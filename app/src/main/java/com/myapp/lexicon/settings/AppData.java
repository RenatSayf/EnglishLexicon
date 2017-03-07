package com.myapp.lexicon.settings;

import android.app.Application;
import android.content.Context;
import android.widget.Button;

import com.myapp.lexicon.t_MatchFragment;

/**
 * Created by Ренат on 20.04.2016.
 */
public class AppData extends Application
{
    private static AppSettings appSettings;
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


    public static Button[] arrayBtnLeft = new Button[t_MatchFragment.ROWS];
    public static Button[] arrayBtnRight = new Button[t_MatchFragment.ROWS];

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
