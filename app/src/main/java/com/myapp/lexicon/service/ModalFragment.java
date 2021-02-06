package com.myapp.lexicon.service;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonSyntaxException;
import com.myapp.lexicon.R;
import com.myapp.lexicon.database.Word;
import com.myapp.lexicon.helpers.StringOperations;
import com.myapp.lexicon.interfaces.IModalFragment;
import com.myapp.lexicon.main.SpeechViewModel;
import com.myapp.lexicon.settings.AppSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class ModalFragment extends DialogFragment
{
    public static final String TAG = ModalFragment.class.getCanonicalName() + ".TAG";
    public static IModalFragment iCallback;

    private AppSettings appSettings;
    private TextView enTextView;
    private TextView ruTextView;
    private static List<Integer> _counters = new ArrayList<>();
    private SpeechViewModel speechVM;

    public ModalFragment()
    {
        // Required empty public constructor
    }


    static ModalFragment newInstance(@Nullable String json, List<Integer> counters, IModalFragment callback)
    {
        ModalFragment fragment = new ModalFragment();
        _counters = counters;
        iCallback = callback;
        if (json != null && counters.size() > 0)
        {
            Bundle bundle = new Bundle();
            bundle.putString(ServiceActivity.ARG_JSON, json);
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        speechVM = new ViewModelProvider(this).get(SpeechViewModel.class);

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
            String json = arguments.getString(ServiceActivity.ARG_JSON);
            try
            {
                if (json != null)
                {
                    Word[] words = StringOperations.getInstance().jsonToWord(json);
                    if (words.length > 0)
                    {
                        nameDictTV.setText(words[0].getDictName());
                        enTextView.setText(words[0].getEnglish());
                        ruTextView.setText(words[0].getTranslate());
                    }
                }

                if (_counters.size() > 0)
                {
                    String concatText = (_counters.get(2) + "")
                            .concat(" / ")
                            .concat(_counters.get(1) + "")
                            .concat("  " + getString(R.string.text_studied) + " " + _counters.get(0));
                    wordsNumberTV.setText(concatText);
                }
            } catch (JsonSyntaxException e)
            {
                e.printStackTrace();
                Toast.makeText(activity, "JSON parsing error", Toast.LENGTH_LONG).show();
                activity.finish();
            }

            Button btnStop = fragmentView.findViewById(R.id.btn_stop_service);
            btnStop.setOnClickListener( view -> ((ServiceActivity)requireActivity()).stopAppService());

            ImageButton btnClose = fragmentView.findViewById(R.id.btn_close);
            btnClose.setOnClickListener(view -> requireActivity().finish());

            Button btnOpenApp = fragmentView.findViewById(R.id.btn_open_app);
            btnOpenApp.setOnClickListener(view -> iCallback.openApp());

            ImageButton btnSound = fragmentView.findViewById(R.id.btn_sound_modal);
            btnSound_OnClick(btnSound);

            CheckBox checkBoxRu = fragmentView.findViewById(R.id.check_box_ru_speak_modal);
            speechVM.isRuSpeech().observe(getViewLifecycleOwner(), checkBoxRu::setChecked);
            checkBoxRu_OnCheckedChange(checkBoxRu);

            speechVM.getSpeechDoneId().observe(getViewLifecycleOwner(), id -> {
                if (id.equals("En") && checkBoxRu.isChecked())
                {
                    speechVM.doSpeech(ruTextView.getText().toString(), new Locale(getString(R.string.lang_code_translate)));
                }
            });

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
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        speechVM.getSpeechDoneId().observe(getViewLifecycleOwner(), id -> {
            if (id.equals("En"))
            {
                Boolean isRuSpeech = speechVM.isRuSpeech().getValue();
                if (isRuSpeech != null && isRuSpeech)
                {
                    String ruText = ruTextView.getText().toString();
                    speechVM.doSpeech(ruText, new Locale(getString(R.string.lang_code_translate)));
                }
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    private void checkBoxRu_OnCheckedChange(CheckBox checkBoxRu)
    {
        checkBoxRu.setOnCheckedChangeListener((compoundButton, isChecked) -> speechVM.setRuSpeech(isChecked));
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

            Boolean isEnSpeech = speechVM.isEnSpeech().getValue();
            String enText = enTextView.getText().toString();
            if (isEnSpeech != null && isEnSpeech)
            {
                speechVM.doSpeech(enText, Locale.US);
            }
        });
    }
}
