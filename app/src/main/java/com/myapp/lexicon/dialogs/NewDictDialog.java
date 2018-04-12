package com.myapp.lexicon.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.myapp.lexicon.R;
import com.myapp.lexicon.database.DataBaseQueries;

public class NewDictDialog extends android.support.v4.app.DialogFragment
{
    public static final String TAG = "new_dict_dialog";
    private DataBaseQueries dataBaseQueries;
    private static NewDictDialog newDictDialog = null;
    public static NewDictDialog newInstance()
    {
        if (newDictDialog == null)
        {
            newDictDialog = new NewDictDialog();
        }
        return newDictDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final View view = getActivity().getLayoutInflater().inflate(R.layout.a_dialog_add_dict, null);
        final EditText editText = view.findViewById(R.id.dialog_add_dict);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle(R.string.title_new_dictionary).setIcon(R.drawable.icon_book)
                .setPositiveButton(R.string.btn_text_add, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                        String dictName = editText.getText().toString();
                        if (!dictName.equals(""))
                        {
                            try
                            {
                                dataBaseQueries = new DataBaseQueries(getActivity());
                                dataBaseQueries.addTableToDbSync(dictName);
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                            Toast toast = Toast.makeText(getActivity(), getString(R.string.text_added_new_dict)+dictName, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    }
                })
                .setNegativeButton(R.string.btn_text_cancel, null)
                .setView(view);

        editText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                String str = s.toString();
                for (int i = 0; i < str.length(); i++)
                {
                    int char_first = str.codePointAt(0);
                    if ((char_first >= 33 && char_first <= 64) || (char_first >= 91 && char_first <= 96) ||
                            (char_first >= 123 && char_first <= 126))
                    {
                        String str_wrong = str.substring(i);
                        editText.setText(str.replace(str_wrong,""));
                    }
                    if ((str.codePointAt(i) >= 33 && str.codePointAt(i) <= 47) || (str.codePointAt(i) >= 58 && str.codePointAt(i) <= 64) ||
                            (str.codePointAt(i) >= 91 && str.codePointAt(i) <= 96) || (str.codePointAt(i) >= 123 && str.codePointAt(i) <= 126))
                    {
                        String str_wrong = str.substring(i);
                        editText.setText(str.replace(str_wrong,""));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s){}
        });

        return builder.create();
    }
}
