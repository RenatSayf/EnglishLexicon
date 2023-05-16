package com.myapp.lexicon.wordstests;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.myapp.lexicon.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;



public class DialogWarning extends DialogFragment
{
    public static final String DIALOG_TAG = "DIALOG_TAG";
    public final String TAG = "lexicon_warning_dialog";
    final String KEY_TEXT_OK_BUTTON = "key_text_ok_btn";
    final String KEY_TEXT_NO_BUTTON = "key_text_no_btn";
    final String KEY_MESSAGE = "key_message";
    final String KEY_IS_NEUTRAL_BTN = "key_is_neutral_btn";

    private Listener listener;

    public DialogWarning()
    {

    }

    public void setListener(Listener listener)
    {
        this.listener = listener;
    }

    public interface Listener
    {
        void onPositiveClick();
        void onNegativeClick();
    }

    @SuppressWarnings("Convert2Lambda")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        String textBtnOk = getString(R.string.text_resume);
        String textBtnNo = getString(R.string.text_at_first);
        String message = getString(R.string.you_have_uncompleted_test);
        boolean isNeutralBtn = false;

        if (getArguments() != null)
        {
            textBtnOk = getArguments().getString(KEY_TEXT_OK_BUTTON, textBtnOk);
            textBtnNo = getArguments().getString(KEY_TEXT_NO_BUTTON, textBtnNo);
            message = getArguments().getString(KEY_MESSAGE, message);
            if (getArguments().containsKey(KEY_IS_NEUTRAL_BTN))
            {
                isNeutralBtn = getArguments().getBoolean(KEY_IS_NEUTRAL_BTN);
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_dialog_warning)
                .setMessage(message)
                .setPositiveButton(textBtnOk, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        if (listener != null)
                        {
                            listener.onPositiveClick();
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
                            listener.onNegativeClick();
                        }
                        dismiss();
                    }
                });

        if (isNeutralBtn)
        {
            builder.setNeutralButton(getString(R.string.button_text_cancel), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    dismiss();
                }
            });
        }

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_popup_dialog_white);
        return dialog;
    }
}
