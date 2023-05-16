package com.myapp.lexicon.wordstests;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myapp.lexicon.R;
import com.myapp.lexicon.databinding.DialogTestCompleteBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;




public class DialogTestComplete extends DialogFragment
{
    public static final String TAG = "DialogTestComplete.TAG";
    private static Listener listener;
    private DialogTestCompleteBinding binding;
    private static DialogTestComplete instance = null;
    private static int total;
    private static int correctly;


    public DialogTestComplete()
    {}

    public static DialogTestComplete getInstance(int correctly, int total, Listener listener) {

        DialogTestComplete.correctly = correctly;
        DialogTestComplete.total = total;
        DialogTestComplete.listener = listener;
        if (DialogTestComplete.instance == null) {
            DialogTestComplete.instance = new DialogTestComplete();
        }
        return instance;
    }

    public interface Listener
    {
        void onTestCompleteClick();
        void onTestRepeatClick();
        void onNextTestClick();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        setStyle(STYLE_NO_TITLE, R.style.AppAlertDialog);
        setCancelable(false);

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_popup_dialog);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        binding = DialogTestCompleteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        double res = 0;
        if (total > 0)
        {
            res = (double) correctly / (double) total * 100;
        }

        String result = "";

        if (res >= 100)
        {
            result = getString(R.string.text_excellent);
            binding.tvEmojiIcon.setText(R.string.slightly_smiling_face);
            binding.tvEmojiIcon2.setText(R.string.thumbs_up);
            binding.tvEmojiIcon2.setVisibility(View.VISIBLE);
        }
        if (res >= 90 && res < 100)
        {
            result = getString(R.string.text_good);
            binding.tvEmojiIcon.setText(R.string.slightly_smiling_face);
            binding.tvEmojiIcon2.setVisibility(View.GONE);
        }
        if (res < 90)
        {
            result = getString(R.string.text_bad);
            binding.tvEmojiIcon.setText(R.string.confused_face);
            binding.tvEmojiIcon2.setVisibility(View.GONE);
        }

        binding.tvResultStatus.setText(result);

        String errors = correctly + " из " + total;
        binding.tvResultValue.setText(errors);

        binding.btnNext.setOnClickListener( v ->
        {
            if (listener != null)
            {
                listener.onNextTestClick();
            }
            dismiss();
        });

        binding.btnRepeat.setOnClickListener( v ->
        {
            if (listener != null)
            {
                listener.onTestRepeatClick();
            }
            dismiss();
        });

        binding.btnComplete.setOnClickListener( v ->
        {
            if (listener != null)
            {
                listener.onTestCompleteClick();
            }
            dismiss();
        });
    }
}
