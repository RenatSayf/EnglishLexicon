package com.myapp.lexicon;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


/**
 * Created by Ренат on 28.09.2016.
 */

public class t_DialogTestComplete extends android.support.v4.app.DialogFragment
{
    private static t_DialogTestComplete instance = new t_DialogTestComplete();
    public String KEY_RESULT = "result";
    public String KEY_ERRORS = "errors";
    public static IDialogComplete_Result iDialogCompleteResult;

    private TextView textViewResult;
    private TextView textViewErrors;
    private ImageButton buttonNext;
    private ImageButton buttonRepeat;
    private ImageButton buttonComplete;

    public t_DialogTestComplete()
    {

    }

    public static t_DialogTestComplete getInstance()
    {
        return instance;
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
                .setTitle("Завершено")
                .setView(dialogView);

        textViewResult = (TextView) dialogView.findViewById(R.id.txt_view_result);
        textViewResult.setText(getArguments().getString(KEY_RESULT, ""));

        textViewErrors = (TextView) dialogView.findViewById(R.id.txt_view_errors);
        textViewErrors.setText(getArguments().getString(KEY_ERRORS, ""));

        buttonNext = (ImageButton) dialogView.findViewById(R.id.btn_next);
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

        buttonRepeat = (ImageButton) dialogView.findViewById(R.id.btn_repeat);
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

        buttonComplete = (ImageButton) dialogView.findViewById(R.id.btn_complete);
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
