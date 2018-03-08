package com.myapp.lexicon.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.myapp.lexicon.R;
import com.myapp.lexicon.settings.AppSettings;

/**
 * Created by Renat
 */

public class ChoiceLangDialog extends DialogFragment
{
    public static final String TAG = "choice_lang_dialog";

    private AppSettings appSettings;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.a_choice_lang_dialog, null);

        appSettings = new AppSettings(getActivity());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Choose Your language")
                .setView(dialogView);

        return builder.create();
    }
}
