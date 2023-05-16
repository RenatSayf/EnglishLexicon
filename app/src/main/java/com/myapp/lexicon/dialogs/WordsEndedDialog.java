package com.myapp.lexicon.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.myapp.lexicon.R;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;



public class WordsEndedDialog extends DialogFragment
{
    public static final String TAG = "words_ended_dialog";
    private static final String DICT_NAME = "dict_name_words_ended_dialog";
    private static WordsEndedDialog instance = null;
    private static Listener listener;

    public static WordsEndedDialog getInstance(String dictName, Listener listener)
    {
        WordsEndedDialog.listener = listener;
        if (instance == null)
        {
            instance = new WordsEndedDialog();
        }
        Bundle bundle = new Bundle();
        bundle.putString(DICT_NAME, dictName);
        instance.setArguments(bundle);
        return instance;
    }

    public interface Listener
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
            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_popup_dialog_white);
            return dialog;
        }
        return super.onCreateDialog(null);
    }

    private void btnOk_OnClick(Button button)
    {
        button.setOnClickListener( view -> {
            if (listener != null)
            {
                listener.wordEndedDialogResult(0);
            }
            dismiss();
        });
    }

    private void btnCancel_OnClick(Button button)
    {
        button.setOnClickListener( view -> {
            if (listener != null)
            {
                listener.wordEndedDialogResult(1);
            }
            dismiss();
        });
    }

}
