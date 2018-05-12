package com.myapp.lexicon.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.myapp.lexicon.R;

import java.util.ArrayList;

public class InclusionDialog extends DialogFragment
{
    public static final String TAG = "inclusion_dialog";
    private static final String KEY_DICT_NAMES = "key_dict_names_inclusion_dialog";
    private static InclusionDialog instance = new InclusionDialog();
    public  static IInclusionDialog iInclusionDialog;

    public interface IInclusionDialog
    {
        void inclusionDialogResult(int result);
    }

    public void setResultListener(IInclusionDialog listener)
    {
        iInclusionDialog = listener;
    }

    public static InclusionDialog getInstance(ArrayList<String> dictNames)
    {
        if (instance == null)
        {
            instance = new InclusionDialog();
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(KEY_DICT_NAMES, dictNames);
        instance.setArguments(bundle);
        return instance;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        FragmentActivity activity = getActivity();
        if (activity != null && getArguments() != null)
        {
            View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_inclusion, new LinearLayout(getContext()), false);
            View title = getActivity().getLayoutInflater().inflate(R.layout.dialog_title_inclusion, new LinearLayout(getContext()), false);
            TextView dictNameTV = dialogView.findViewById(R.id.dict_name_dialog_inclusion);
            ArrayList<String> dictNames = getArguments().getStringArrayList(KEY_DICT_NAMES);
            String text = "";
            if (dictNames != null)
            {
                for (int i = 0; i < dictNames.size(); i++)
                {
                    text = text.concat(dictNames.get(i));
                    if (i < dictNames.size() - 1)
                    {
                        text = text + ", ";
                    }
                }
            }
            dictNameTV.setText(text);
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setCustomTitle(title)
                    .setView(dialogView)
                    .setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            if (iInclusionDialog != null)
                            {
                                iInclusionDialog.inclusionDialogResult(-1);
                            }
                        }
                    })
                    .setNeutralButton("Продожить без", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            if (iInclusionDialog != null)
                            {
                                iInclusionDialog.inclusionDialogResult(0);
                            }
                        }
                    })
                    .setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            if (iInclusionDialog != null)
                            {
                                iInclusionDialog.inclusionDialogResult(1);
                            }
                        }
                    })
                    .setCancelable(false);
            return builder.create();
        } else
        {
            return super.onCreateDialog(savedInstanceState);
        }
    }
}
