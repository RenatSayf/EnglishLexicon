package com.myapp.lexicon.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.myapp.lexicon.R;
import com.myapp.lexicon.settings.AppSettings;

/**
 * Created by Renat
 */

public class ChoiceLangDialog extends DialogFragment
{
    public static final String TAG = "choice_lang_dialog";

    private AppSettings appSettings;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        appSettings = new AppSettings(getActivity());
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.a_choice_lang_dialog, new LinearLayout(getActivity()), false);
        RadioGroup langListRG = dialogView.findViewById(R.id.lang_list_RG);
        langListRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId)
            {
                switch (checkedId)
                {
                    case R.id.btn_ru_lang:
                        //Toast.makeText(getActivity(),"Выбран русский", Toast.LENGTH_SHORT).show();
                        appSettings.setTranslateLang(appSettings.getTransLangList().get(0));
                        break;
                    case R.id.btn_ua_lang:
                        //Toast.makeText(getActivity(),"Выбран украинский", Toast.LENGTH_SHORT).show();
                        appSettings.setTranslateLang(appSettings.getTransLangList().get(1));
                        break;
                    default:
                        appSettings.setTranslateLang(appSettings.getTransLangList().get(0));
                        break;
                }
            }
        });

        Button dialogBtnOk = dialogView.findViewById(R.id.dialog_btn_ok);
        dialogBtnOk.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dismiss();
                getActivity().finish();
                Intent intent = new Intent(getActivity(), SplashScreenActivity.class);
                getActivity().startActivity(intent);
            }
        });



        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Choose Your language")
                .setView(dialogView);

        return builder.create();
    }
}
