package com.myapp.lexicon;

import android.app.Application;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

    public static int buttonPlayVisible = View.VISIBLE;
    public static int buttonStopVisible = View.GONE;
    public static int buttonPauseVisible = View.GONE;
    public static int buttonPreviousVisible = View.GONE;
    public static int buttonNextVisible = View.GONE;

    private static int _Ndict;
    private static int _Nword = 1;
    public static int get_Ndict()
    {
        return _Ndict;
    }

    public static void set_Ndict(int Ndict)
    {
        _Ndict = Ndict;
    }

    public static int get_Nword()
    {
        return _Nword;
    }

    public static void set_Nword(int Nword)
    {
        _Nword = Nword;
    }

    private static boolean _isPause = false;
    public static boolean get_isPause()
    {
        return _isPause;
    }

    public static void set_isPause(boolean isPause)
    {
        _isPause = isPause;
    }

    public static int progBarMainActVisible = View.GONE;

    private static boolean engOnly = false;
    public static boolean isEngOnly()
    {
        return engOnly;
    }

    public static void setEngOnly(boolean engOnly)
    {
        AppData.engOnly = engOnly;
    }

    public static Button[] arrayBtnLeft = new Button[t_MatchFragment.ROWS];
    public static Button[] arrayBtnRight = new Button[t_MatchFragment.ROWS];



}
