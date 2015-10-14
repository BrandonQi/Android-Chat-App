package edu.stevens.cs522.chat.managers;

import android.database.Cursor;

public interface ILoaderQueryListener {
    public void handleResults(Cursor cursor);

    public void closeResults();
}
