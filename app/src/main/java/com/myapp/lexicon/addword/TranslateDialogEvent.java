package com.myapp.lexicon.addword;

import com.myapp.lexicon.database.DataBaseEntry;

public class TranslateDialogEvent
{
    public final DataBaseEntry entry;

    public TranslateDialogEvent(DataBaseEntry entry)
    {
        this.entry = entry;
    }
}
