package com.myapp.lexicon.wordstests;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.myapp.lexicon.R;


/**
 * Created by Ренат.
 */

public class DialogTestComplete extends android.support.v4.app.DialogFragment
{
    public String KEY_RESULT = "result";
    public String KEY_ERRORS = "errors";
    public static IDialogComplete_Result iDialogCompleteResult;

    public DialogTestComplete()
    {

    }

    public interface IDialogComplete_Result
    {
        void dialogCompleteResult(int res);
    }

    public void setIDialogCompleteResult(IDialogComplete_Result result)
    {
        iDialogCompleteResult = result;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {

        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.t_dialog_complete_test, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.text_test_is_finish))
                .setView(dialogView);

        if (getArguments().get(KEY_RESULT) == getString(R.string.text_excellent))
        {
            builder.setIcon(getResources().getDrawable(R.drawable.icon_smiling_face));
        }
        if (getArguments().get(KEY_RESULT) == getString(R.string.text_good))
        {
            builder.setIcon(getResources().getDrawable(R.drawable.icon_calm_face));
        }
        if (getArguments().get(KEY_RESULT) == getString(R.string.text_bad))
        {
            builder.setIcon(getResources().getDrawable(R.drawable.icon_sad_face));
        }

        TextView textViewResult = (TextView) dialogView.findViewById(R.id.txt_view_result);
        textViewResult.setText(getArguments().getString(KEY_RESULT, ""));

        TextView textViewErrors = (TextView) dialogView.findViewById(R.id.txt_view_errors);
        textViewErrors.setText(getArguments().getString(KEY_ERRORS, ""));

        Button buttonNext = (Button) dialogView.findViewById(R.id.btn_next);
        buttonNext.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (iDialogCompleteResult != null)
                {
                    iDialogCompleteResult.dialogCompleteResult(1);
                }
                dismiss();
            }
        });

        Button buttonRepeat = (Button) dialogView.findViewById(R.id.btn_repeat);
        buttonRepeat.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (iDialogCompleteResult != null)
                {
                    iDialogCompleteResult.dialogCompleteResult(0);
                }
                dismiss();
            }
        });

        Button buttonComplete = (Button) dialogView.findViewById(R.id.btn_complete);
        buttonComplete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (iDialogCompleteResult != null)
                {
                    iDialogCompleteResult.dialogCompleteResult(-1);
                }
                dismiss();
            }
        });

        return builder.create();
    }


}
