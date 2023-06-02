package com.myapp.lexicon.service;


import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.myapp.lexicon.R;
import com.myapp.lexicon.helpers.ExtensionsKt;
import com.myapp.lexicon.helpers.RandomNumberGenerator;
import com.myapp.lexicon.helpers.StringOperations;
import com.myapp.lexicon.interfaces.IModalFragment;
import com.myapp.lexicon.main.MainViewModel;
import com.myapp.lexicon.main.SpeechViewModel;
import com.myapp.lexicon.models.Word;
import com.myapp.lexicon.settings.SettingsExtKt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.disposables.CompositeDisposable;


@AndroidEntryPoint
public class TestModalFragment extends DialogFragment
{
    public static final String TAG = TestModalFragment.class.getCanonicalName() + ".TAG";
    public static IModalFragment iCallback;

    private TextView enTextView;
    private Button ruBtn1, ruBtn2;
    private List<Word> compareList;
    private boolean wordIsStudied = false;
    private static List<Integer> _counters = new ArrayList<>();
    private Word[] words = new Word[0];

    private MainViewModel viewModel;
    private SpeechViewModel speechVM;
    private final CompositeDisposable composite = new CompositeDisposable();

    public TestModalFragment()
    {
        // Required empty public constructor
    }

    static TestModalFragment newInstance(@Nullable String json, List<Integer> counters, IModalFragment callback)
    {
        TestModalFragment fragment = new TestModalFragment();
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
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        speechVM = new  ViewModelProvider(this).get(SpeechViewModel.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        View dialogView = getLayoutInflater().inflate(R.layout.s_test_modal_fragment, new LinearLayout(requireContext()), false);

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(dialogView).create();

        boolean adsIsEnabled = SettingsExtKt.getAdsIsEnabled(this);
        if (adsIsEnabled) {
            //TODO("Not implemented")
        }
        enTextView = dialogView.findViewById(R.id.en_text_view);
        ruBtn1 = dialogView.findViewById(R.id.ru_btn_1);
        ruBtn1.setText("");
        ruBtn1_OnClick(ruBtn1);
        ruBtn2 = dialogView.findViewById(R.id.ru_btn_2);
        ruBtn2.setText("");
        ruBtn2_OnClick(ruBtn2);

        Bundle arguments = getArguments();
        if (arguments != null)
        {
            String json = arguments.getString(ServiceActivity.ARG_JSON);
            try
            {
                if (json != null)
                {
                    words = StringOperations.getInstance().jsonToWord(json);
                    if (words.length > 0)
                    {
                        enTextView.setText(words[0].getEnglish());
                        TextView nameDictTV = dialogView.findViewById(R.id.name_dict_tv_test_modal);
                        nameDictTV.setText(words[0].getDictName());

                        viewModel.getRandomWord(words[0]).observe(this, word -> {
                            ArrayList<Word> listWords = new ArrayList<>();
                            listWords.add(words[0]);
                            listWords.add(word);
                            RandomNumberGenerator numberGenerator = new RandomNumberGenerator(2, (int) new Date().getTime());
                            int i = numberGenerator.generate();
                            int j = numberGenerator.generate();
                            ruBtn1.setText(listWords.get(i).getTranslate());
                            ruBtn2.setText(listWords.get(j).getTranslate());
                            compareList = listWords;
                        });
                    }
                }
                TextView wordsNumberTV = dialogView.findViewById(R.id.words_number_tv_test_modal);
                if (_counters.size() >= 3)
                {
                    String concatText = (_counters.get(0) + "")
                            .concat(" / ")
                            .concat(_counters.get(1) + "")
                            .concat("  " + getString(R.string.text_studied) + " " + _counters.get(2));
                    wordsNumberTV.setText(concatText);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }

        ImageButton speakButton = dialogView.findViewById(R.id.btn_sound_modal);
        speakButton.setOnClickListener(view -> {
            String enText = enTextView.getText().toString();
            if (!enText.equals(""))
            {
                speechVM.doSpeech(enText, Locale.US);
            }
        });

        ImageButton btnClose = dialogView.findViewById(R.id.modal_btn_close);
        btnClose.setOnClickListener(view -> requireActivity().finish());

        Button btnOpenApp = dialogView.findViewById(R.id.btn_open_app);
        btnOpenApp.setOnClickListener(view1 -> iCallback.openApp());

        Button btnStopService = dialogView.findViewById(R.id.btn_stop_service);
        //noinspection CodeBlock2Expr
        btnStopService.setOnClickListener( view -> {
            SettingsExtKt.disablePassiveWordsRepeat(
                    this,
                    () -> {
                        String message = getString(R.string.text_app_is_closed)
                                .concat(" ")
                                .concat(getString(R.string.app_name))
                                .concat(" ")
                                .concat(getString(R.string.text_app_is_closed_end));
                        ExtensionsKt.showToast(this, message, Toast.LENGTH_LONG);
                        requireActivity().finish();
                        return null;
                    },
                    err -> {
                        ExtensionsKt.showToast(this, err, Toast.LENGTH_LONG);
                        requireActivity().finish();
                        return null;
                    }
            );
        });

        checkStudied_OnCheckedChange(dialogView.findViewById(R.id.check_box_studied));

        viewModel.getCountRepeat().observe(this, id -> {
            if (id > 0) Toast.makeText(getActivity(), R.string.text_word_is_not_show, Toast.LENGTH_LONG).show();
        });

        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners_background);
        return dialog;
    }

    private void ruBtn1_OnClick(View view)
    {
        view.setOnClickListener( v -> {
            Button button = (Button) v;
            String translate = button.getText().toString().toLowerCase();
            String english = enTextView.getText().toString().toLowerCase();
            boolean result = compareWords(compareList, english, translate);
            if (result)
            {
                rightAnswerAnim(button);
            }
            else
            {
                noRightAnswerAnim(button);
            }
        });
    }

    private void ruBtn2_OnClick(View view)
    {
        view.setOnClickListener( v -> {
            Button button = (Button) v;
            String translate = button.getText().toString().toLowerCase();
            String english = enTextView.getText().toString().toLowerCase();
            boolean result = compareWords(compareList, english, translate);
            if (result)
            {
                rightAnswerAnim(button);
            }
            else
            {
                noRightAnswerAnim(button);
            }
        });
    }

    private boolean compareWords(List<Word> compareList, String english, String translate)
    {
        boolean result = false;
        if (compareList != null && compareList.size() > 0)
        {
            for (int i = 0; i < compareList.size(); i++)
            {
                String enText = compareList.get(i).getEnglish().toLowerCase();
                String ruText = compareList.get(i).getTranslate().toLowerCase();
                if (enText.equals(english.toLowerCase()) && ruText.equals(translate.toLowerCase()))
                {
                    result = true;
                    break;
                }
            }
        }
        else
        {
            result = true;
        }
        return result;
    }

    private void rightAnswerAnim(final Button button)
    {
        Animation animRight = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_right);
        animRight.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                viewModel.goForward(Arrays.asList(words));
                button.setBackgroundResource(R.drawable.btn_for_test_modal_green);
                button.setTextColor(requireContext().getResources().getColor(R.color.colorWhite, null));

                if (wordIsStudied)
                {
                    if (words.length > 0)
                    {
                        int wordId = words[0].get_id();
                        viewModel.setCountRepeat(0, wordId, wordId);
                    }
                }
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                button.setBackgroundResource(R.drawable.btn_for_test_modal_transp);
                button.setTextColor(requireContext().getResources().getColor(R.color.colorLightGreen, null));
                requireActivity().finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });
        button.startAnimation(animRight);
    }

    private void noRightAnswerAnim(final Button button)
    {
        Animation animNotRight = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_not_right);
        animNotRight.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                if (getActivity() != null)
                {
                    button.setBackgroundResource(R.drawable.btn_for_test_modal_red);
                    button.setTextColor(getActivity().getResources().getColor(R.color.colorWhite, null));
                    wordIsStudied = false;
                }
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                if (getActivity() != null)
                {
                    button.setBackgroundResource(R.drawable.btn_for_test_modal_transp);
                    button.setTextColor(getActivity().getResources().getColor(R.color.colorLightGreen, null));
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {

            }
        });
        button.startAnimation(animNotRight);
    }

    @SuppressWarnings("Convert2Lambda")
    private void checkStudied_OnCheckedChange(CheckBox checkBox)
    {
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked)
            {
                wordIsStudied = isChecked;
            }
        });
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        composite.dispose();
        composite.clear();
    }
}
