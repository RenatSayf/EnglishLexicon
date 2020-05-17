package com.myapp.lexicon.wordstests;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.myapp.lexicon.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;


/**
 * Created by Ренат.
 */

public class DialogTestComplete extends DialogFragment
{
    String KEY_RESULT = "result";
    String KEY_ERRORS = "errors";
    private static IDialogComplete_Result iDialogCompleteResult;

    public DialogTestComplete()
    {

    }

    public interface IDialogComplete_Result
    {
        void dialogCompleteResult(int res);
    }

    void setIDialogCompleteResult(IDialogComplete_Result result)
    {
        iDialogCompleteResult = result;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        if (getActivity() != null && getArguments() != null)
        {
            final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.t_dialog_complete_test, new LinearLayout(getContext()), false);
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

            TextView textViewResult = dialogView.findViewById(R.id.txt_view_result);
            textViewResult.setText(getArguments().getString(KEY_RESULT, ""));

            TextView textViewErrors = dialogView.findViewById(R.id.txt_view_errors);
            textViewErrors.setText(getArguments().getString(KEY_ERRORS, ""));

            Button buttonNext = dialogView.findViewById(R.id.btn_next);
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

            Button buttonRepeat = dialogView.findViewById(R.id.btn_repeat);
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

            Button buttonComplete = dialogView.findViewById(R.id.btn_complete);
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
        } else
        {
            return super.onCreateDialog(null);
        }
    }


}
