package edu.stevens.cs522.chat.managers;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class AsyncContentResolver extends AsyncQueryHandler {
    public AsyncContentResolver(ContentResolver contentResolver) {
        super(contentResolver);
    }

    public void insertAsync(Uri uri,
                            ContentValues contentValues,
                            IContinue<Uri> callback) {
        startInsert(0, callback, uri, contentValues);
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
        if (cookie != null) {
            ((IContinue<Uri>) cookie).kontinue(uri);
        }
    }

    public void updateAsync(Uri uri, ContentValues cv, IContinue<Integer> callback) {
        startUpdate(0, callback, uri, cv, null, null);
    }

    @Override
    protected void onUpdateComplete(int token, Object cookie, int result) {
        if (cookie != null) {
            ((IContinue<Integer>) cookie).kontinue(result);
        }
    }

    public void queryAsync(Uri uri, IContinue<Cursor> callback, String[] selectionArgs) {
        startQuery(0, callback, uri, null, null, selectionArgs, null);
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (cookie != null) {
            ((IContinue<Cursor>) cookie).kontinue(cursor);
        }
    }
}
