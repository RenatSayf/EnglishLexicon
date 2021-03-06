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
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;


/**
 * Created by Ренат.
 */

public class DialogTestComplete extends DialogFragment
{
    String KEY_RESULT = "result";
    String KEY_ERRORS = "errors";
    public static final String TAG = "DialogTestComplete.TAG";
    public static final String TOTAL_NUM = "TOTAL_NUM";
    public static final String CORRECTLY_NUM = "CORRECTLY_NUM";
    private static IDialogComplete_Result iDialogCompleteResult;

    public DialogTestComplete()
    {

    }

    public interface IDialogComplete_Result
    {
        void dialogCompleteResult(int res);
    }

    void setListener(IDialogComplete_Result result)
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
                    .setCancelable(false)
                    .setView(dialogView);

            double total = (double) getArguments().getInt(TOTAL_NUM, 0);
            double correctly = (double) getArguments().getInt(CORRECTLY_NUM, 0);
            double res = correctly / total * 100;

            String result = "";

            if (res >= 100)
            {
                result = getString(R.string.text_excellent);
                builder.setIcon(ResourcesCompat.getDrawable(requireContext().getResources(), R.drawable.icon_smiling_face, null));
            }
            if (res >= 90 && res < 100)
            {
                result = getString(R.string.text_good);
                builder.setIcon(ResourcesCompat.getDrawable(requireContext().getResources(), R.drawable.icon_calm_face, null));
            }
            if (res < 90)
            {
                result = getString(R.string.text_bad);
                builder.setIcon(ResourcesCompat.getDrawable(requireContext().getResources(), R.drawable.icon_sad_face, null));
            }

            TextView textViewResult = dialogView.findViewById(R.id.txt_view_result);
            textViewResult.setText(result);

            String errors = (int)correctly + " из " + (int)total;
            TextView textViewErrors = dialogView.findViewById(R.id.txt_view_errors);
            textViewErrors.setText(errors);

            Button buttonNext = dialogView.findViewById(R.id.btn_next);
            buttonNext.setOnClickListener( v ->
            {
                if (iDialogCompleteResult != null)
                {
                    iDialogCompleteResult.dialogCompleteResult(1);
                }
                dismiss();
            });

            Button buttonRepeat = dialogView.findViewById(R.id.btn_repeat);
            buttonRepeat.setOnClickListener( v ->
            {
                if (iDialogCompleteResult != null)
                {
                    iDialogCompleteResult.dialogCompleteResult(0);
                }
                dismiss();
            });

            Button buttonComplete = dialogView.findViewById(R.id.btn_complete);
            buttonComplete.setOnClickListener( v ->
            {
                if (iDialogCompleteResult != null)
                {
                    iDialogCompleteResult.dialogCompleteResult(-1);
                }
                dismiss();
            });

            return builder.create();
        } else
        {
            return super.onCreateDialog(null);
        }
    }


}
