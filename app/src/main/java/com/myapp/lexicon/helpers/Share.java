package com.myapp.lexicon.helpers;

import android.content.Context;
import android.content.Intent;

import com.myapp.lexicon.R;

public class Share
{
    public void doShare(Context context)
    {
        if (context != null)
        {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "http://play.google.com/store/apps/details?id=" + context.getPackageName();
            String shareSub = context.getString(R.string.text_excellent_app);
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub);
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            context.startActivity(Intent.createChooser(sharingIntent, context.getString(R.string.text_share_using)));
        }
    }
}
