package com.myapp.lexicon.service;


import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.myapp.lexicon.R;
import com.myapp.lexicon.database.DataBaseEntry;
import com.myapp.lexicon.main.SplashScreenActivity;
import com.myapp.lexicon.settings.AppData;
import com.myapp.lexicon.settings.AppSettings;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import kotlin.Pair;

import static com.myapp.lexicon.service.ServiceActivity.map;
import static com.myapp.lexicon.service.ServiceActivity.speech;


public class ModalFragment extends Fragment
{
    private AppSettings appSettings;
    private TextView enTextView;
    private TextView ruTextView;
    private CheckBox checkBoxRu;

    public ModalFragment()
    {
        // Required empty public constructor
    }


    static ModalFragment newInstance(@Nullable String json)
    {
        ModalFragment fragment = new ModalFragment();
        if (json != null)
        {
            Bundle bundle = new Bundle();
            bundle.putString(AppData.ARG_JSON, json);
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        appSettings = new AppSettings(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View fragmentView = inflater.inflate(R.layout.s_repeat_modal_fragment, container, false);

        enTextView = fragmentView.findViewById(R.id.en_text_view);
        ruTextView = fragmentView.findViewById(R.id.ru_text_view);

        TextView nameDictTV = fragmentView.findViewById(R.id.name_dict_tv);
        TextView wordsNumberTV = fragmentView.findViewById(R.id.words_number_tv_modal_sv);

        ServiceActivity activity = (ServiceActivity)requireActivity();
        Bundle arguments = getArguments();
        if (arguments != null)
        {
            String json = arguments.getString(AppData.ARG_JSON);
            try
            {
                Pair<Map<String, Integer>, List<DataBaseEntry>> pair = new Gson().fromJson(json, AppData.jsonType); //TODO изменить парсинг json
                nameDictTV.setText(pair.getSecond().get(0).getDictName());
                enTextView.setText(pair.getSecond().get(0).getEnglish());
                ruTextView.setText(pair.getSecond().get(0).getTranslate());

                if (pair.getFirst().size() > 0)
                {
                    String concatText = (pair.getSecond().get(0).getRowId() + "")
                            .concat(" / ")
                            .concat(pair.getFirst().get("totalWords").toString())
                            .concat("  " + getString(R.string.text_studied) + " " + pair.getFirst().get("studiedWords").toString());
                    wordsNumberTV.setText(concatText);
                }
            } catch (JsonSyntaxException e)
            {
                e.printStackTrace();
                Toast.makeText(activity, "JSON parsing error", Toast.LENGTH_LONG).show();
                activity.finish();
            }

            Button btnStop = fragmentView.findViewById(R.id.btn_stop_service);
            btnStop.setOnClickListener( view -> {
                ((ServiceActivity)requireActivity()).stopAppService();
            });

            ImageButton btnClose = fragmentView.findViewById(R.id.btn_close);
            btnClose.setOnClickListener(view -> {
                requireActivity().finish();
            });

            Button btnOpenApp = fragmentView.findViewById(R.id.btn_open_app);
            btnOpenApp.setOnClickListener(view -> {
                requireActivity().startActivity(new Intent(getContext(), SplashScreenActivity.class));
                requireActivity().finish();
            });

            ImageButton btnSound = fragmentView.findViewById(R.id.btn_sound_modal);
            btnSound_OnClick(btnSound);

            checkBoxRu = fragmentView.findViewById(R.id.check_box_ru_speak_modal);
            checkBoxRu.setChecked(appSettings.isRuSpeechInModal());
            checkBoxRu_OnCheckedChange(checkBoxRu);

            ImageView orderPlayIcon = fragmentView.findViewById(R.id.order_play_icon_iv_modal);
            if (appSettings.getOrderPlay() == 0)
            {
                orderPlayIcon.setImageResource(R.drawable.ic_repeat_white);
            }
            if (appSettings.getOrderPlay() == 1)
            {
                orderPlayIcon.setImageResource(R.drawable.ic_shuffle_white);
            }
        }

        return fragmentView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    private void checkBoxRu_OnCheckedChange(CheckBox checkBoxRu)
    {
        checkBoxRu.setOnCheckedChangeListener((compoundButton, isChecked) -> appSettings.setRuSpeechInModal(isChecked));
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    private void btnSound_OnClick(ImageButton button)
    {
        button.setOnClickListener(view -> {
            if (speech == null || speech.isSpeaking())
            {
                return;
            }
            String enText = enTextView.getText().toString();
            final String ruText = ruTextView.getText().toString();
            if (!enText.equals(""))
            {
                speech.speak(enText, TextToSpeech.QUEUE_ADD, map);
            }
            speech.setOnUtteranceProgressListener(new UtteranceProgressListener()
            {
                @Override
                public void onStart(String s)
                {

                }

                @Override
                public void onDone(String s)
                {
                    if (checkBoxRu.isChecked() && !ruText.equals("") && s.equals(Locale.US.getDisplayLanguage()))
                    {
                        int res = speech.isLanguageAvailable(Locale.getDefault());
                        if (res == TextToSpeech.LANG_COUNTRY_AVAILABLE)
                        {
                            speech.setLanguage(Locale.getDefault());
                            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Locale.getDefault().getDisplayLanguage());
                            speech.speak(ruText, TextToSpeech.QUEUE_ADD, map);
                        }
                    }
                    if (s.equals(Locale.getDefault().getDisplayLanguage()))
                    {
                        speech.stop();
                        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Locale.US.getDisplayLanguage());
                        speech.setLanguage(Locale.US);
                    }
                }

                @Override
                public void onError(String s)
                {

                }
            });
        });
    }
}
