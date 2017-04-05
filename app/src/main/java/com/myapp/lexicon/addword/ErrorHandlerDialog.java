package com.myapp.lexicon.addword;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.myapp.lexicon.R;

/**
 * Created by Renat on 04.04.2017.
 */

public class ErrorHandlerDialog extends DialogFragment
{

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.b_api_key_dialog, null);
        return super.onCreateDialog(savedInstanceState);
    }
}
