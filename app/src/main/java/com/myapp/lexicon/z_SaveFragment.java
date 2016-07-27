package com.myapp.lexicon;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by Ренат on 20.04.2016.
 */
public class z_SaveFragment extends Fragment
{
    private z_speechSynthesAsync speechSynthesAsync;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
    public z_speechSynthesAsync getSpeechSynthesAsync()
    {
        return speechSynthesAsync;
    }

    public void setSpeechSynthesAsync(z_speechSynthesAsync speechSynthesAsync)
    {
        this.speechSynthesAsync = speechSynthesAsync;
    }


}
