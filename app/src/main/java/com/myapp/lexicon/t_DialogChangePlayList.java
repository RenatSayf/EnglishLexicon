package com.myapp.lexicon;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by Ренат on 03.10.2016.
 */

public class t_DialogChangePlayList extends android.support.v4.app.DialogFragment
{
    public final String KEY_LIST_DICT = "listdict";
    public IChangePlayList changePlayList;

    public interface IChangePlayList
    {
        void dialogResult();
    }

    public void setDialogResult(IChangePlayList changePlayList)
    {
        this.changePlayList = changePlayList;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        ArrayList<String> listDict = getArguments().getStringArrayList(KEY_LIST_DICT);
        if (listDict == null)
        {
            return super.onCreateDialog(savedInstanceState);
        }
        boolean[]choice = new boolean[listDict.size()];
        final String[] items = new String[listDict.size()];
        for (int i = 0; i < listDict.size(); i++)
        {
            items[i] = listDict.get(i);
            choice[i] = true;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle("Исключить словари из списка воспроизведения?")
                .setMultiChoiceItems(items, choice, new DialogInterface.OnMultiChoiceClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked)
                    {
                        if (!isChecked)
                        {
                            items[which] = null;
                        }
                    }
                })
                .setPositiveButton("Да", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        for (int i = 0; i < items.length; i++)
                        {
                            a_MainActivity.removeItemPlayList(items[i]);
                        }
                    }
                })
                .setNegativeButton("Нет", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                });

        return builder.create();
    }
}
