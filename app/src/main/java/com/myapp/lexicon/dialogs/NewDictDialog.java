package com.myapp.lexicon.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myapp.lexicon.R;
import com.myapp.lexicon.databinding.ADialogAddDictBinding;
import com.myapp.lexicon.helpers.ExtensionsKt;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class NewDictDialog extends DialogFragment
{
    private ADialogAddDictBinding binding;
    public static final String TAG = NewDictDialog.class.getSimpleName() + ".TAG";
    private static NewDictDialog newDictDialog = null;
    private static Listener listener;

    public static NewDictDialog newInstance(Listener listener)
    {
        NewDictDialog.listener = listener;
        if (newDictDialog == null)
        {
            newDictDialog = new NewDictDialog();
        }
        return newDictDialog;
    }

    public interface Listener
    {
        void onPositiveClick(@NonNull String dictName);
        void onNegativeClick();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        setStyle(STYLE_NO_TITLE, R.style.AppAlertDialog);
        setCancelable(false);
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null)
        {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_popup_dialog);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        binding = ADialogAddDictBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        binding.etDictName.requestFocus();
        binding.etDictName.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                String str = s.toString();
                int wrongIndex = ExtensionsKt.checkOnlyLetterAndFirstNotDigit(str);
                if (wrongIndex >= 0) {
                    try
                    {
                        String text = Objects.requireNonNull(binding.etDictName.getText()).toString();
                        String wrongStr = text.substring(wrongIndex);
                        String fixedStr = str.replace(wrongStr, "");
                        binding.etDictName.setText(fixedStr);
                        binding.etDictName.setSelection(fixedStr.length());
                    } catch (NullPointerException e)
                    {
                        ExtensionsKt.printStackTraceIfDebug(e);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s){}
        });

        binding.btnCancel.setOnClickListener(v -> {
            dismiss();
            listener.onNegativeClick();
        });

        binding.btnOk.setOnClickListener( v -> {
            String dictName = binding.etDictName.getText() != null ? binding.etDictName.getText().toString() : "";
            dismiss();
            if (dictName.isEmpty()) {
                dismiss();
                listener.onNegativeClick();
                return;
            }
            listener.onPositiveClick(dictName);
        });
    }
}
