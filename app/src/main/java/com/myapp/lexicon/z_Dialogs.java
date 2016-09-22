package com.myapp.lexicon;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Spinner;

import java.util.ArrayList;

/**
 * Created by Ренат on 22.09.2016.
 */
public class z_Dialogs extends Activity
{
    private static z_Dialogs ourInstance = new z_Dialogs();
    public IDialogCompleteResult iDialogCompleteResult;

    public static z_Dialogs getInstance()
    {
        return ourInstance;
    }

    private z_Dialogs()
    {
    }

    public interface IDialogCompleteResult
    {
        void dialogCompleteResult(int res);
    }

    public void dialogComplete(Activity activity, final Spinner spinner)
    {
        final Dialog dialogComplete;
        final View dialogView = activity.getLayoutInflater().inflate(R.layout.t_dialog_complete_test, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle("Завершено")
                .setCancelable(false)
                .setView(dialogView);
        dialogComplete = builder.create();
        dialogComplete.show();
        ImageButton buttonNext = (ImageButton) dialogView.findViewById(R.id.btn_next);
        buttonNext.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int count = spinner.getAdapter().getCount();
                int position = spinner.getSelectedItemPosition();
                if (position < count)
                {
                    position++;
                    spinner.setSelection(position);
                }
                if (position == count)
                {
                    position = 0;
                    spinner.setSelection(position);
                }
                dialogComplete.dismiss();
            }
        });
        ImageButton buttonRepeat = (ImageButton) dialogView.findViewById(R.id.btn_repeat);
        buttonRepeat.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                dialogComplete.dismiss();
            }
        });
        ImageButton buttonComplete = (ImageButton) dialogView.findViewById(R.id.btn_complete);
        buttonComplete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                dialogComplete.dismiss();
            }
        });
    }

}
