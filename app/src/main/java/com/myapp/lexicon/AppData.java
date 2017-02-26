package com.myapp.lexicon;

import android.app.Application;
import android.widget.Button;

/**
 * Created by Ренат on 20.04.2016.
 */
public class AppData extends Application
{
    private static String enText;

    public static String getEnText()
    {
        return enText;
    }

    public static void setEnText(String enText)
    {
        AppData.enText = enText;
    }

    private static String ruText;
    public static String getRuText()
    {
        return ruText;
    }

    public static void setRuText(String ruText)
    {
        AppData.ruText = ruText;
    }

    private static String currentDict;
    public static String getCurrentDict()
    {
        return currentDict;
    }

    public static void setCurrentDict(String currentDict)
    {
        AppData.currentDict = currentDict;
    }

    private static int ndict;
    private static int nword = 1;
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

    private static boolean isPause = false;
    public static boolean get_isPause()
    {
        return isPause;
    }

    public static void set_isPause(boolean isPause)
    {
        AppData.isPause = isPause;
    }


    public static Button[] arrayBtnLeft = new Button[t_MatchFragment.ROWS];
    public static Button[] arrayBtnRight = new Button[t_MatchFragment.ROWS];


}
