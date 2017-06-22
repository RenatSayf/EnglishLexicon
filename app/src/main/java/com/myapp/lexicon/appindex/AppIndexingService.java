package com.myapp.lexicon.appindex;

import android.app.IntentService;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;

import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.Indexable;
import com.google.firebase.appindexing.builders.Indexables;
import com.myapp.lexicon.R;

import java.util.ArrayList;

/**
 * Created by Renat.
 */

public class AppIndexingService extends IntentService
{
    public AppIndexingService(String name)
    {
        super("LexiconIndexingService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent)
    {
        ArrayList<Indexable> indexableNotes = new ArrayList<>();

        Indexable noteToIndex = Indexables.noteDigitalDocumentBuilder()
                    .setName(getString(R.string.app_indexing_name))
                    .setText(getString(R.string.app_indexing_text))
                    .setUrl(getString(R.string.app_link))
                    .build();

            indexableNotes.add(noteToIndex);


        if (indexableNotes.size() > 0)
        {
            Indexable[] notesArr = new Indexable[indexableNotes.size()];
            notesArr = indexableNotes.toArray(notesArr);

            // batch insert indexable notes into index
            FirebaseAppIndex.getInstance().update(notesArr);
        }
    }
}
