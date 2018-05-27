package com.myapp.lexicon.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.myapp.lexicon.R;

public class WordsEndedDialog extends android.support.v4.app.DialogFragment
{
    public static final String TAG = "words_ended_dialog";
    private static final String DICT_NAME = "dict_name_words_ended_dialog";
    private static WordsEndedDialog ourInstance = new WordsEndedDialog();
    private static IWordEndedDialogResult idialogResult;

    public static WordsEndedDialog getInstance(String dictName, IWordEndedDialogResult dialogResult)
    {
        idialogResult = dialogResult;
        if (ourInstance == null)
        {
            ourInstance = new WordsEndedDialog();
        }
        Bundle bundle = new Bundle();
        bundle.putString(DICT_NAME, dictName);
        ourInstance.setArguments(bundle);
        return ourInstance;
    }

    public interface IWordEndedDialogResult
    {
        void wordEndedDialogResult(int res);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = null;
        if (getActivity() != null)
        {
            View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_ended_words, new LinearLayout(getContext()), false);
            TextView dictNameTV = dialogView.findViewById(R.id.dict_name_dialog_ended);
            Button btnOk = dialogView.findViewById(R.id.btn_ok_ended_dialog);
            btnOk_OnClick(btnOk);
            Button btnCancel = dialogView.findViewById(R.id.btn_cancel_ended_dialog);
            btnCancel_OnClick(btnCancel);
            if (getArguments() != null)
            {
                dictNameTV.setText(getArguments().getString(DICT_NAME));
            }
            View title = getActivity().getLayoutInflater().inflate(R.layout.congratulate_title, new LinearLayout(getContext()), false);

            builder = new AlertDialog.Builder(getActivity())
                    .setCustomTitle(title)
                    .setCancelable(false)
                    .setView(dialogView);
        }

        if (builder != null)
        {
            return builder.create();
        }
        return super.onCreateDialog(null);
    }

    private void btnOk_OnClick(Button button)
    {
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (idialogResult != null)
                {
                    idialogResult.wordEndedDialogResult(0);
                }
                dismiss();
            }
        });
    }

    private void btnCancel_OnClick(Button button)
    {
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (idialogResult != null)
                {
                    idialogResult.wordEndedDialogResult(1);
                }
                dismiss();
            }
        });
    }

}
