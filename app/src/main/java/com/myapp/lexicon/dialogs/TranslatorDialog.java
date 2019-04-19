package com.myapp.lexicon.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.myapp.lexicon.R;
import com.myapp.lexicon.connectivity.TranslateApi;

public class TranslatorDialog extends AppCompatDialogFragment
{
    public static final String TAG = "translator_dialog";
    public static final String EN_WORD_TAG = "en_word";
    private static final String RU_WORD_TAG = "ru_word";

    private static TranslatorDialog dialog = null;

    public TranslatorDialog()
    {

    }

    public static TranslatorDialog getInstance(String enWord)
    {
        if (dialog == null)
        {
            dialog = new TranslatorDialog();
        }
        Bundle bundle = new Bundle();
        bundle.putString(EN_WORD_TAG, enWord);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getActivity() != null)
        {
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            String url = new TranslateApi(getActivity()).getStringUrl(getArguments().getString(EN_WORD_TAG));
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    if (getArguments() != null)
                    {
                        getArguments().putString(RU_WORD_TAG, null);
                    }
                    return;
                }
            },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error)
                        {
                            return;
                        }
                    });
            queue.add(stringRequest);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        if (getActivity() != null)
        {
            View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_translator, new LinearLayout(getContext()), false);
            EditText editTextEn = dialogView.findViewById(R.id.en_word_et);
            if (getArguments() != null)
            {
                editTextEn.setText(getArguments().getString(EN_WORD_TAG));
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())//.setTitle("Перевод")
                    .setPositiveButton("Добавить", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {

                        }
                    })
                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {

                        }
                    })
                    .setView(dialogView);
            return builder.create();
        } else
        {
            return super.onCreateDialog(savedInstanceState);
        }

    }


}
