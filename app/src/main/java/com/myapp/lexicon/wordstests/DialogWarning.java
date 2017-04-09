package com.myapp.lexicon.wordstests;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Created by Renat .
 */

public class DialogWarning extends DialogFragment
{
    public static final String TAG = "lexicon_warning_dialog";
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Незавершенный тест:")
                .setMessage("У Вас есть незавершенный тест. Вы хотите продолжить проверку или начать тест с начала")
                .setPositiveButton("Да", new DialogInterface.OnClickListener()
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
                .setNegativeButton("С начала", new DialogInterface.OnClickListener()
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
