package com.myapp.lexicon.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.myapp.lexicon.R;
import com.myapp.lexicon.addword.AddWordActivity;
import com.myapp.lexicon.addword.TranslateDialogEvent;
import com.myapp.lexicon.connectivity.NetRepositoryImpl;
import com.myapp.lexicon.connectivity.TranslateApi;
import com.myapp.lexicon.database.CallableAction;
import com.myapp.lexicon.database.DataBaseEntry;
import com.myapp.lexicon.database.DataBaseQueries;
import com.myapp.lexicon.database.LexiconDataBase;
import com.myapp.lexicon.main.Speaker;
import com.myapp.lexicon.main.SplashScreenActivity;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TranslatorDialog extends AppCompatDialogFragment implements View.OnClickListener
{
    public static final String TAG = "translator_dialog";
    private static final String EN_WORD_TAG = "en_word";

    private EditText editTextEn;
    private EditText editTextRu;
    private ProgressBar progressBar;
    private Spinner dictListSpinner;
    private ArrayAdapter<String> adapter;
    private Speaker speaker;

    private static NoticeDialogListener mListener;

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
        LexiconDataBase dataBase = new ViewModelProvider(this).get(LexiconDataBase.class);
        dataBase.setDictList(getActivity()).observe(this, new Observer<List<String>>()
        {
            @Override
            public void onChanged(@Nullable List<String> dicts)
            {
                if (getActivity() != null && dicts != null)
                {
                    adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, dicts);
                    dictListSpinner.setAdapter(adapter);
                }
            }
        });
        dataBase.setDictList(getActivity());

        speaker = new Speaker(getActivity(), new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                speaker.speechInit(status, getActivity(), speaker);
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
            editTextEn = dialogView.findViewById(R.id.en_word_et);
            editTextRu = dialogView.findViewById(R.id.ru_word_et);
            progressBar = dialogView.findViewById(R.id.prog_bar_dialog_trans);
            dictListSpinner = dialogView.findViewById(R.id.spinner_trans_dialog);
            Button btnAdd = dialogView.findViewById(R.id.btn_add_trans_dialog);
            Button btnCancel = dialogView.findViewById(R.id.btn_cancel_trans_dialog);
            btnCancel.setOnClickListener(this);
            ImageButton enSpeechButton = dialogView.findViewById(R.id.en_speech_imgbtn);
            enSpeechButton.setOnClickListener(this);
            ImageButton ruSpeechButton = dialogView.findViewById(R.id.ru_speech_imgbtn);
            ruSpeechButton.setOnClickListener(this);
            btnAddOnClick(btnAdd);
            ImageButton btnUpdateTranslate = dialogView.findViewById(R.id.btn_update_translate);
            btnUpdateTranslateOnClick(btnUpdateTranslate);
            Button btnGoToYandex = dialogView.findViewById(R.id.btn_go_to_yandex);
            btnGoToYandexOnClick(btnGoToYandex);
            Button btnOpenApp = dialogView.findViewById(R.id.btn_open_app);
            btnOpenAppOnClick(btnOpenApp);

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

    private void btnOpenAppOnClick(Button button)
    {
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (getActivity() != null)
                {
                    getActivity().finish();
                    Intent intent = new Intent(getActivity(), SplashScreenActivity.class);
                    getActivity().startActivity(intent);
                }
            }
        });
    }

    private void btnGoToYandexOnClick(Button button)
    {
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (getActivity() != null)
                {
                    String transDirect = getActivity().getString(R.string.translate_direct_en_ru);
                    String text = editTextEn.getText().toString();
                    String url = getString(R.string.translate_yandex_ru).concat("/?lang=").concat(transDirect).concat("&text=").concat(text);
                    Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browser);
                }
            }
        });
    }

    private void btnUpdateTranslateOnClick(ImageButton button)
    {
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String url = new TranslateApi(getActivity()).getStringUrl(editTextEn.getText().toString());
                getTranslate(url);
            }
        });
    }

    private void btnAddOnClick(Button btnAdd)
    {
        btnAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final String dict = dictListSpinner.getSelectedItem().toString();
                String enText = editTextEn.getText().toString();
                String ruText = editTextRu.getText().toString();
                if (getActivity() != null && dict.equals(getActivity().getString(R.string.text_new_dict)))
                {

                    Intent intent = new Intent(getContext(), AddWordActivity.class);
                    EventBus.getDefault().postSticky(new TranslateDialogEvent(new DataBaseEntry(enText, ruText)));
                    startActivity(intent);
                    mListener.onDialogAddClick(TranslatorDialog.this);
                }
                else if (getActivity() != null && !dict.equals(getActivity().getString(R.string.text_new_dict)) && !dict.equals(""))
                {
                    Observable<Long> longObservable = Observable.fromCallable(new CallableAction(new DataBaseQueries(getContext()), dict, new DataBaseEntry(enText, ruText)));
                    longObservable.subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new io.reactivex.Observer<Long>()
                            {
                                private Disposable disposable;
                                private Long res;
                                @Override
                                public void onSubscribe(Disposable d)
                                {
                                    disposable = d;
                                }

                                @Override
                                public void onNext(Long aLong)
                                {
                                    res = aLong;
                                }

                                @Override
                                public void onError(Throwable e)
                                {
                                    disposable.dispose();
                                }

                                @Override
                                public void onComplete()
                                {
                                    disposable.dispose();
                                    if (res != -1)
                                    {
                                        Toast toast = Toast.makeText(getActivity(), getActivity().getString(R.string.in_dictionary) + dict + getActivity().getString(R.string.new_word_is_added), Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.CENTER,0,0);
                                        toast.show();
                                    }
                                    mListener.onDialogAddClick(TranslatorDialog.this);
                                }
                            });
                }

            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (getArguments() != null && getActivity() != null)
        {
            String url = new TranslateApi(getActivity()).getStringUrl(getArguments().getString(EN_WORD_TAG));
            getTranslate(url);

            String buildUrl = new TranslateApi(getActivity()).buildUrl(getArguments().getString(EN_WORD_TAG));
            Disposable disposable = null;
            disposable = new NetRepositoryImpl().getTranslateDoc(buildUrl)
                    .map(document -> {
                        return document.body().toString();
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(document -> {

                    }, throwable -> {
                        return;
                    });
        }

    }

    private void getTranslate(String url)
    {
        if (getActivity() != null && getArguments() != null)
        {
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, null, response -> {
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
                    editTextRu.setText(ruText.toString().toLowerCase());
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            },
                    error -> progressBar.setVisibility(View.GONE));
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
            dismiss();
        }
        if (id == R.id.en_speech_imgbtn)
        {
            String text = editTextEn.getText().toString();
            doSpeech(text, Locale.US);
        }
        if (id == R.id.ru_speech_imgbtn)
        {
            String text = editTextRu.getText().toString();
            doSpeech(text, Locale.getDefault());
        }
    }

    private void doSpeech(String text, Locale locale)
    {
        HashMap<String, String> utterance_Id = new HashMap<>();

        try
        {
            speaker.setLanguage(locale);
        } catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        utterance_Id.clear();
        utterance_Id.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "translate_dialog");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            speaker.speak(text, TextToSpeech.QUEUE_ADD, null, utterance_Id.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
        } else
        {
            speaker.speak(text, TextToSpeech.QUEUE_ADD, utterance_Id);
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog)
    {
        super.onCancel(dialog);
        if (getActivity() != null)
        {
            getActivity().finish();
        }
    }

    @Override
    public void onDestroy()
    {
        speaker.shutdown();
        super.onDestroy();
    }
}
