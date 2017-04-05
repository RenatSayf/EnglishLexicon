package com.myapp.lexicon.addword;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.myapp.lexicon.R;

/**
 * Created by Renat.
 */

public class ErrorHandlerDialog extends DialogFragment
{
    public static final String KEY_TITLE = "title";
    public static final String KEY_ERROR_MESSAGE = "error_msg";
    public static final String KEY_OPTION_MESSAGE = "option_msg";
    public static final String DIALOG_TAG = "error_handler_dialog";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        String error = getArguments().getString(KEY_ERROR_MESSAGE);
        String option = getArguments().getString(KEY_OPTION_MESSAGE);

        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.b_api_key_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.title_yandex_translate))
                .setView(dialogView)
                .setIcon(getResources().getDrawable(R.drawable.icon_sad_face));

        TextView textViewError = (TextView) dialogView.findViewById(R.id.text_view_error_msg);
        textViewError.setText(error);

        TextView textViewOption = (TextView) dialogView.findViewById(R.id.text_view_option);
        textViewOption.setText(option);

        Button buttonPaste = (Button) dialogView.findViewById(R.id.btn_paste);
        buttonPaste.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

            }
        });

        Button buttonSave = (Button) dialogView.findViewById(R.id.btn_save);
        buttonSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dismiss();
            }
        });

        Button buttonGet = (Button) dialogView.findViewById(R.id.btn_get);
        buttonGet.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
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
