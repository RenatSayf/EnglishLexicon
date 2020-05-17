package com.myapp.lexicon.wordstests;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.myapp.lexicon.R;
import com.myapp.lexicon.settings.AppSettings;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

/**
 * Created by Ренат.
 */

public class DialogChangePlayList extends DialogFragment
{
    public final String TAG = "dialog_change_pl_lexicon";
    final String KEY_LIST_DICT = "list_dict";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        ArrayList<String> listDict = null;
        if (getArguments() != null)
        {
            listDict = getArguments().getStringArrayList(KEY_LIST_DICT);
        }
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
                            if (getActivity() != null)
                            {
                                new AppSettings(getActivity()).removeItemFromPlayList(item);
                            }
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
