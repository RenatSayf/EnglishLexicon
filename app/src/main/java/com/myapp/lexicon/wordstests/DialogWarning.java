package com.myapp.lexicon.wordstests;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.myapp.lexicon.R;

/**
 * Created by Renat .
 */

public class DialogWarning extends DialogFragment
{
    public final String TAG = "lexicon_warning_dialog";
    public final String KEY_TEXT_OK_BUTTON = "key_text_ok_btn";
    public final String KEY_TEXT_NO_BUTTON = "key_text_no_btn";
    public final String KEY_MESSAGE = "key_message";

    private IDialogResult listener;

    public DialogWarning()
    {

    }

    public void setListener(IDialogResult listener)
    {
        this.listener = listener;
    }

    public interface IDialogResult
    {
        void dialogListener(boolean result);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        String textBtnOk = getString(R.string.button_text_yes);
        String textBtnNo = getString(R.string.button_text_no);
        String message = getString(R.string.you_have_uncompleted_test);
        if (getArguments() != null)
        {
            textBtnOk = getArguments().getString(KEY_TEXT_OK_BUTTON, textBtnOk);
            textBtnNo = getArguments().getString(KEY_TEXT_NO_BUTTON, textBtnNo);
            message = getArguments().getString(KEY_MESSAGE, message);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Незавершенный тест:")
                .setMessage(message)
                .setPositiveButton(textBtnOk, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        if (listener != null)
                        {
                            listener.dialogListener(true);
                        }
                        dismiss();
                    }
                })
                .setNegativeButton(textBtnNo, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        if (listener != null)
                        {
                            listener.dialogListener(false);
                        }
                        dismiss();
                    }
                });

        return builder.create();
    }
}
