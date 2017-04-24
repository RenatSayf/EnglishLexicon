package com.myapp.lexicon.wordstests;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.myapp.lexicon.R;
import com.myapp.lexicon.settings.AppSettings;

import java.util.ArrayList;

/**
 * Created by Ренат.
 */

public class DialogChangePlayList extends android.support.v4.app.DialogFragment
{
    public final String TAG = "dialog_change_pl_lexicon";
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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle(R.string.header_dialog_change_pl)
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
                        for (String item : items)
                        {
                            new AppSettings(getContext()).removeItemFromPlayList(item);
                        }
                        dismiss();
                    }
                })
                .setNegativeButton("Нет", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dismiss();
                    }
                });

        return builder.create();
    }
}
