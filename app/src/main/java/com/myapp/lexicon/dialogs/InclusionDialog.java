package com.myapp.lexicon.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;

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
        if (activity != null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);

            if (iInclusionDialog != null)
            {
                iInclusionDialog.inclusionDialogResult(0);
            }

            return builder.create();
        } else
        {
            return super.onCreateDialog(savedInstanceState);
        }
    }
}
