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




    private static String currentDict2;
    public static String getCurrentDict2()
    {
        return currentDict2;
    }

    public static void setCurrentDict2(String currentDict)
    {
        AppData.currentDict2 = currentDict;
    }

    private static int _Ndict2;
    private static int _Nword2 = 1;
    public static int get_Ndict2()
    {
        return _Ndict2;
    }

    public static void set_Ndict2(int Ndict)
    {
        _Ndict2 = Ndict;
    }

    public static int get_Nword2()
    {
        return _Nword2;
    }

    public static void set_Nword2(int Nword)
    {
        _Nword2 = Nword;
    }

    private static boolean _isPause2 = false;
    public static boolean get_isPause2()
    {
        return _isPause2;
    }

    public static void set_isPause2(boolean isPause)
    {
        _isPause2 = isPause;
    }

    private static boolean engOnly2 = false;
    public static boolean isEngOnly2()
    {
        return engOnly2;
    }

    public static void setEngOnly2(boolean engOnly)
    {
        AppData.engOnly2 = engOnly;
    }



}
