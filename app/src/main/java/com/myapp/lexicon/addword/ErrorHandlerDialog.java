package com.myapp.lexicon.addword;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.myapp.lexicon.R;
import com.myapp.lexicon.database.DataBaseQueries;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;


public class ErrorHandlerDialog extends DialogFragment
{
    static final String KEY_IS_SAD_FACE = "sad_face";
    static final String KEY_ERROR_MESSAGE = "error_msg";
    static final String KEY_OPTION_MESSAGE = "option_msg";
    static final String DIALOG_TAG = "error_handler_dialog";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        if (getActivity() != null && getArguments() != null)
        {
            boolean isSad = getArguments().getBoolean(KEY_IS_SAD_FACE);
            String error = getArguments().getString(KEY_ERROR_MESSAGE);
            String option = getArguments().getString(KEY_OPTION_MESSAGE);

            final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.b_api_key_dialog, new LinearLayout(getContext()), false);
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

            TextView textViewError = dialogView.findViewById(R.id.text_view_error_msg);
            textViewError.setText(error);

            TextView textViewOption = dialogView.findViewById(R.id.text_view_option);
            textViewOption.setText(option);

            final EditText editText = dialogView.findViewById(R.id.edit_text_api_key);

            Button buttonPaste = dialogView.findViewById(R.id.btn_paste);
            buttonPaste.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    FragmentActivity activity = getActivity();
                    if (activity != null)
                    {
                        @SuppressWarnings("AccessStaticViaInstance") ClipboardManager manager = (ClipboardManager) activity.getSystemService(getActivity().CLIPBOARD_SERVICE);
                        if (manager != null)
                        {
                            ClipData clip = manager.getPrimaryClip();
                            if (clip != null && clip.getItemCount() > 0)
                            {
                                editText.setText(clip.getItemAt(0).getText());
                            }
                        }
                    }
                }
            });

            Button buttonSave = dialogView.findViewById(R.id.btn_save);
            buttonSave.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    DataBaseQueries dataBaseQueries = new DataBaseQueries(getActivity());
                    if (!editText.getText().toString().equals("") && editText.getText().toString().length() > 70)
                    {
                        long res = dataBaseQueries.addUpdateApiKey(editText.getText().toString());
                        if (res != -1)
                        {
                            Toast.makeText(getActivity(), R.string.text_key_is_added, Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(getActivity(), R.string.text_write_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                    dismiss();
                }
            });

            Button buttonGet = dialogView.findViewById(R.id.btn_get);
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

            Button buttonCancel = dialogView.findViewById(R.id.btn_cancel);
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
        return super.onCreateDialog(null);
    }


}
