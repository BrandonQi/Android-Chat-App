package edu.stevens.cs522.chat.managers;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class AsyncQueryBuilder<T> implements IContinue<Cursor> {
    private IEntityCreator<T> mCreator;
    private IAsyncQueryListener<T> mListener;

    private AsyncQueryBuilder(IEntityCreator<T> creator,
                              IAsyncQueryListener<T> listener) {
        mCreator = creator;
        mListener = listener;
    }

    public static <T> void executeQuery(Context context,
                                        Uri uri,
                                        String[] selectionArgs,
                                        IEntityCreator<T> creator,
                                        IAsyncQueryListener<T> listener) {
        AsyncQueryBuilder<T> queryBuilder
                = new AsyncQueryBuilder<T>(creator, listener);
        AsyncContentResolver resolver
                = new AsyncContentResolver(context.getContentResolver());
        resolver.queryAsync(uri, queryBuilder, selectionArgs);
    }

    public void kontinue(Cursor cursor) {
        List<T> instances = new ArrayList<T>();
        if (cursor.moveToFirst()) {
            do {
                T instance = mCreator.create(cursor);
                instances.add(instance);
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        mListener.handleResults(instances);
    }
}
