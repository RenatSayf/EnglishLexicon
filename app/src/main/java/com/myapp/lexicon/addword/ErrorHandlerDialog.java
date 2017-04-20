package com.myapp.lexicon.addword;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.myapp.lexicon.R;
import com.myapp.lexicon.database.DataBaseQueries;

/**
 * Created by Renat.
 */

public class ErrorHandlerDialog extends DialogFragment
{
    public static final String KEY_IS_SAD_FACE = "sad_face";
    public static final String KEY_ERROR_MESSAGE = "error_msg";
    public static final String KEY_OPTION_MESSAGE = "option_msg";
    public static final String DIALOG_TAG = "error_handler_dialog";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        boolean isSad = getArguments().getBoolean(KEY_IS_SAD_FACE);
        String error = getArguments().getString(KEY_ERROR_MESSAGE);
        String option = getArguments().getString(KEY_OPTION_MESSAGE);

        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.b_api_key_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.title_yandex_translate))
                .setView(dialogView);
        if (isSad)
        {
            builder.setIcon(getResources().getDrawable(R.drawable.icon_sad_face));
        }
        else
        {
            builder.setIcon(getResources().getDrawable(R.drawable.icon_calm_face));
        }

        TextView textViewError = (TextView) dialogView.findViewById(R.id.text_view_error_msg);
        textViewError.setText(error);

        TextView textViewOption = (TextView) dialogView.findViewById(R.id.text_view_option);
        textViewOption.setText(option);

        final EditText editText = (EditText) dialogView.findViewById(R.id.edit_text_api_key);

        Button buttonPaste = (Button) dialogView.findViewById(R.id.btn_paste);
        buttonPaste.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ClipboardManager manager = (ClipboardManager) getActivity().getSystemService(getActivity().CLIPBOARD_SERVICE);
                ClipData clip = manager.getPrimaryClip();
                if (clip.getItemCount() > 0)
                {
                    editText.setText(clip.getItemAt(0).getText());
                }
            }
        });

        Button buttonSave = (Button) dialogView.findViewById(R.id.btn_save);
        buttonSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                DataBaseQueries dataBaseQueries = new DataBaseQueries(getActivity());
                if (!editText.getText().toString().equals("") && editText.getText().toString().length() > 70)
                {
                    dataBaseQueries.addUpdateApiKey(editText.getText().toString());
                }
                dismiss();
            }
        });

        Button buttonGet = (Button) dialogView.findViewById(R.id.btn_get);
        buttonGet.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.tech_yandex_ru_translate)));
                startActivity(browser);
                dismiss();
            }
        });

        Button buttonCancel = (Button) dialogView.findViewById(R.id.btn_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dismiss();
            }
        });


        return builder.create();
    }


}
