package com.myapp.lexicon.database;

import java.util.concurrent.Callable;

public class CallableAction implements Callable<Long>
{
    private String tableName;
    private DataBaseEntry entry;
    private DataBaseQueries queries;
    public CallableAction(DataBaseQueries queries, String tableName, DataBaseEntry entry)
    {
        this.queries = queries;
        this.tableName = tableName;
        this.entry = entry;
    }

    @Override
    public Long call() throws Exception
    {
        return queries.insertWordInTableSync(tableName, entry);
    }
}
