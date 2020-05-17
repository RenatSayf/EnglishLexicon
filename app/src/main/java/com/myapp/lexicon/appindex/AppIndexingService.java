package com.myapp.lexicon.appindex;

import android.app.IntentService;
import android.content.Intent;

import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.Indexable;
import com.google.firebase.appindexing.builders.Indexables;
import com.myapp.lexicon.R;

import java.util.ArrayList;

import androidx.annotation.Nullable;

/**
 * Created by Renat.
 */

public class AppIndexingService extends IntentService
{
    public AppIndexingService()
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
                .setDescription("Запоминай английские слова легко")
                .setUrl(getString(R.string.app_link))
                .build();

        indexableNotes.add(noteToIndex);

        noteToIndex = Indexables.noteDigitalDocumentBuilder()
                .setName("Английский разговорник")
                .setText("Английский разговорник")
                .setDescription("отображает на экране английские слова или целые фразы с переводом и озвучивает их")
                .setUrl(getString(R.string.app_link))
                .build();

        indexableNotes.add(noteToIndex);

        noteToIndex = Indexables.noteDigitalDocumentBuilder()
                .setName("английский язык для начинающих")
                .setText("английский язык для начинающих")
                .setDescription("отображает на экране английские слова или целые фразы с переводом и озвучивает их")
                .setUrl(getString(R.string.app_link))
                .build();

        indexableNotes.add(noteToIndex);

        noteToIndex = Indexables.noteDigitalDocumentBuilder()
                .setName("изучай английский язык")
                .setText("изучай английский язык")
                .setDescription("отображает на экране английские слова или целые фразы с переводом и озвучивает их")
                .setUrl(getString(R.string.app_link))
                .build();

        indexableNotes.add(noteToIndex);


        noteToIndex = Indexables.noteDigitalDocumentBuilder()
                .setName("англо-русский переводчик")
                .setText("англо-русский переводчик")
                .setDescription("англо-русский, русско-английский переводчик")
                .setUrl(getString(R.string.app_link))
                .build();

        indexableNotes.add(noteToIndex);

        noteToIndex = Indexables.noteDigitalDocumentBuilder()
                .setName("английский язык обучение аудио для начинающих")
                .setText("английский язык обучение аудио для начинающих")
                .setDescription("Английские слова и фразы запоминай легко и быстро")
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
