package com.myapp.lexicon.dialogs;

import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.View;
import android.widget.Button;
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
import com.myapp.lexicon.database.LexiconDataBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class TranslatorDialog extends AppCompatDialogFragment implements View.OnClickListener
{
    public static final String TAG = "translator_dialog";
    public static final String EN_WORD_TAG = "en_word";

    private EditText editTextRu;
    private ProgressBar progressBar;

    static NoticeDialogListener mListener;

    public interface NoticeDialogListener
    {
        void onDialogAddClick(AppCompatDialogFragment dialog);
        void onDialogCancelClick(AppCompatDialogFragment dialog);
    }

    public TranslatorDialog()
    {

    }

    public static TranslatorDialog getInstance(String enWord, NoticeDialogListener listener)
    {
        mListener = listener;
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
        LexiconDataBase dataBase = ViewModelProviders.of(this).get(LexiconDataBase.class);
        dataBase.getDictList().observe(this, new Observer<List<String>>()
        {
            @Override
            public void onChanged(@Nullable List<String> dicts)
            {
                return;
            }
        });
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
            Button btnAdd = dialogView.findViewById(R.id.btn_add_trans_dialog);
            Button btnCancel = dialogView.findViewById(R.id.btn_cancel_trans_dialog);
            btnCancel.setOnClickListener(this);
            btnAddOnClick(btnAdd);
            if (getArguments() != null)
            {
                editTextEn.setText(getArguments().getString(EN_WORD_TAG));
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())//.setTitle("Перевод")
                    .setView(dialogView);
            return builder.create();
        } else
        {
            return super.onCreateDialog(savedInstanceState);
        }
    }

    private void btnAddOnClick(Button btnAdd)
    {
        btnAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mListener.onDialogAddClick(TranslatorDialog.this);
            }
        });
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
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
                            ruText.append(array.get(i));
                            if (i < array.length() - 1)
                            {
                                ruText.append(", ");
                            }
                        }
                        editTextRu.setText(ruText.toString());
                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }


            },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error)
                        {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
            queue.add(jsonRequest);
        }

    }

    @Override
    public void onClick(View view)
    {
        int id = view.getId();
        if (id == R.id.btn_cancel_trans_dialog)
        {
            mListener.onDialogCancelClick(TranslatorDialog.this);
        }
    }
}
