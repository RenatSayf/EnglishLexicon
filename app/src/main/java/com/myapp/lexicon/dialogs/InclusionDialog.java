package com.myapp.lexicon.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.myapp.lexicon.R;
import com.myapp.lexicon.helpers.LockOrientation;

import java.util.ArrayList;

public class InclusionDialog extends DialogFragment
{
    public static final String TAG = "inclusion_dialog";
    private static final String KEY_DICT_NAMES = "key_dict_names_inclusion_dialog";
    private static InclusionDialog instance = new InclusionDialog();
    public static IInclusionDialog iInclusionDialog;
    private LockOrientation lockOrientation;

    public interface IInclusionDialog
    {
        void inclusionDialogResult(int result);
    }

    public void setResultListener(IInclusionDialog listener)
    {
        iInclusionDialog = listener;
    }

    public static InclusionDialog getInstance(ArrayList<String> dictNames)
    {
        if (instance == null)
        {
            instance = new InclusionDialog();
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(KEY_DICT_NAMES, dictNames);
        instance.setArguments(bundle);
        return instance;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        FragmentActivity activity = getActivity();
        if (activity != null && getArguments() != null)
        {
            lockOrientation = new LockOrientation(getActivity());
            lockOrientation.lock();
            View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_inclusion, new LinearLayout(getContext()), false);
            View title = getActivity().getLayoutInflater().inflate(R.layout.dialog_title_inclusion, new LinearLayout(getContext()), false);
            TextView dictNameTV = dialogView.findViewById(R.id.dict_name_dialog_inclusion);
            ArrayList<String> dictNames = getArguments().getStringArrayList(KEY_DICT_NAMES);
            String text = "";
            if (dictNames != null)
            {
                for (int i = 0; i < dictNames.size(); i++)
                {
                    text = text.concat(dictNames.get(i));
                    if (i < dictNames.size() - 1)
                    {
                        text = text + ", ";
                    }
                }
            }
            dictNameTV.setText(text);
            Button btnOk = dialogView.findViewById(R.id.btn_ok_dialog_inclusion);
            btnOk_OnClick(btnOk);
            Button btnCancel = dialogView.findViewById(R.id.btn_cancel_dialog_inclusion);
            btnCancel_OnClick(btnCancel);
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setCustomTitle(title)
                    .setView(dialogView)
                    .setCancelable(false);
            return builder.create();
        } else
        {
            return super.onCreateDialog(savedInstanceState);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        lockOrientation.unLock();
    }

    private void btnCancel_OnClick(Button button)
    {
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (iInclusionDialog != null)
                {
                    iInclusionDialog.inclusionDialogResult(-1);
                }
                dismiss();
            }
        });
    }

    private void btnOk_OnClick(Button button)
    {
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (iInclusionDialog != null)
                {
                    iInclusionDialog.inclusionDialogResult(1);
                }
                dismiss();
            }
        });
    }
}
