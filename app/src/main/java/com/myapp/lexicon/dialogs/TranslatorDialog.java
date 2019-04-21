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
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.myapp.lexicon.R;
import com.myapp.lexicon.connectivity.TranslateApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TranslatorDialog extends AppCompatDialogFragment
{
    public static final String TAG = "translator_dialog";
    public static final String EN_WORD_TAG = "en_word";
    private static final String RU_WORD_TAG = "ru_word";

    private EditText editTextRu;
    private ProgressBar progressBar;

    public TranslatorDialog()
    {

    }

    public static TranslatorDialog getInstance(String enWord)
    {
        TranslatorDialog dialog = new TranslatorDialog();
        Bundle bundle = new Bundle();
        bundle.putString(EN_WORD_TAG, enWord);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        if (getActivity() != null)
        {
            View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_translator, new LinearLayout(getContext()), false);
            EditText editTextEn = dialogView.findViewById(R.id.en_word_et);
            editTextRu = dialogView.findViewById(R.id.ru_word_et);
            progressBar = dialogView.findViewById(R.id.prog_bar_dialog_trans);
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

    @Override
    public void onStart()
    {
        super.onStart();
        if (getArguments() != null && getActivity() != null)
        {
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            String url = new TranslateApi(getActivity()).getStringUrl(getArguments().getString(EN_WORD_TAG));
            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response)
                {
                    progressBar.setVisibility(View.GONE);
                    try
                    {
                        JSONArray array = response.getJSONArray("text");
                        StringBuilder ruText = new StringBuilder();
                        for (int i = 0; i < array.length(); i++)
                        {
                            ruText.append(array.get(i)).append(", ");
                        }

                        editTextRu.setText(ruText.toString().replaceAll("/(,\\s)$/g", ""));
                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                    return;
                }


            },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error)
                        {
                            progressBar.setVisibility(View.GONE);
                            return;
                        }
                    });
            queue.add(jsonRequest);
            progressBar.animate();
        }

    }
}
