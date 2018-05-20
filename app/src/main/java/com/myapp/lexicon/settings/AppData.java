package com.myapp.lexicon.settings;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.myapp.lexicon.database.DataBaseEntry;
import com.myapp.lexicon.database.DataBaseQueries;
import com.myapp.lexicon.database.GetEntriesFromDbAsync;
import com.myapp.lexicon.database.GetStudiedWordsCount;
import com.myapp.lexicon.wordeditor.ListViewAdapter;

import java.util.ArrayList;


/**
 * Storing intermediate data of the application
 */

public class AppData
{
    private static AppData instance = null;

    public ArrayList<String> getPlayList()
    {
        return playList;
    }

    private ArrayList<String> playList;
    private int ndict;
    private int nword = 1;
    private boolean is_pause = false;
    private ListViewAdapter listViewAdapter;
    private String langCode;
    private int serviceMode = 0;
    private int doneRepeat = 1;

    private IGetWordListerner iGetWordListerner;
    private IDictNumChangeListener iDictNumChangeListener;

    public static AppData getInstance()
    {
        if (instance == null)
        {
            instance = new AppData();
        }

        return instance;
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
        if (iDictNumChangeListener != null)
        {
            iDictNumChangeListener.dictNumberOnChanged(this.ndict);
        }
    }

    public interface IDictNumChangeListener
    {
        void dictNumberOnChanged(int ndict);
    }

    public void setDictNumberChangeListener(Context context)
    {
        iDictNumChangeListener = (IDictNumChangeListener) context;
    }

    public int getNword()
    {
        return nword;
    }

    public void setNword(int nword)
    {
        this.nword = nword;
    }

    public interface IGetWordListerner
    {
        void getWordComplete(ArrayList<DataBaseEntry> entries, Integer[] dictSize);
    }

    public void getNextNword(final Activity activity, IGetWordListerner listerner)
    {
        if (playList.size() > 0)
        {
            iGetWordListerner = listerner;
            if (this.ndict > playList.size() - 1 || this.ndict < 0)
            {
                this.ndict = 0;
            }
            GetStudiedWordsCount getStudiedWordsCount = new GetStudiedWordsCount(activity, playList.get(ndict), new GetStudiedWordsCount.GetCountListener()
            {
                @Override
                public void onTaskComplete(Integer[] resArray)
                {
                    final Integer[] countEntries = resArray;
                    if (nword > countEntries[1])
                    {
                        nword = 1;
                        ndict++;
                        if (ndict > playList.size() - 1 || ndict < 0)
                        {
                            ndict = 0;
                        }
                    }
                    GetEntriesFromDbAsync getEntriesFromDbAsync = new GetEntriesFromDbAsync(activity, playList.get(ndict), nword, 1, true, new GetEntriesFromDbAsync.GetEntriesListener()
                    {
                        @Override
                        public void getEntriesListener(ArrayList<DataBaseEntry> entries)
                        {
                            if (iGetWordListerner != null)
                            {
                                iGetWordListerner.getWordComplete(entries, countEntries);
                            }
                        }
                    });
                    if (getEntriesFromDbAsync.getStatus() != AsyncTask.Status.RUNNING)
                    {
                        getEntriesFromDbAsync.execute();
                    }
                }
            });
            if (getStudiedWordsCount.getStatus() != AsyncTask.Status.RUNNING)
            {
                getStudiedWordsCount.execute();
            }
        }
    }

    public void getPreviousNword(final Activity activity, IGetWordListerner listerner)
    {
        if (playList.size() > 0)
        {
            iGetWordListerner = listerner;
            if (this.ndict > playList.size() - 1 || this.ndict < 0)
            {
                this.ndict = playList.size() - 1;
            }
            GetStudiedWordsCount getStudiedWordsCount = new GetStudiedWordsCount(activity, playList.get(ndict), new GetStudiedWordsCount.GetCountListener()
            {
                @Override
                public void onTaskComplete(Integer[] resArray)
                {
                    final Integer[] countEntries = resArray;
                    if (nword < 1)
                    {
                        ndict--;
                        if (ndict > playList.size() - 1 || ndict < 0)
                        {
                            ndict = playList.size() - 1;
                            nword = new DataBaseQueries(activity).getCountEntriesSync(playList.get(ndict));
                        }
                    }
                    GetEntriesFromDbAsync getEntriesFromDbAsync = new GetEntriesFromDbAsync(activity, playList.get(ndict), nword, -1, true, new GetEntriesFromDbAsync.GetEntriesListener()
                    {
                        @Override
                        public void getEntriesListener(ArrayList<DataBaseEntry> entries)
                        {
                            if (iGetWordListerner != null)
                            {
                                iGetWordListerner.getWordComplete(entries, countEntries);
                            }
                        }
                    });
                    if (getEntriesFromDbAsync.getStatus() != AsyncTask.Status.RUNNING)
                    {
                        getEntriesFromDbAsync.execute();
                    }
                }
            });
            if (getStudiedWordsCount.getStatus() != null)
            {
                getStudiedWordsCount.execute();
            }
        }
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
        //appSettings.savePlayList(playList);
        appSettings.setPause(is_pause);
        appSettings.setDictNumber(ndict);
        appSettings.setWordNumber(nword);
        appSettings.setTranslateLang(langCode);
    }

    public void initAllSettings(Context context)
    {
        AppSettings appSettings = new AppSettings(context);
        playList = appSettings.getPlayList();
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
