package edu.stevens.cs522.chat.managers;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
public class LoaderQueryBuilder<T> implements LoaderManager.LoaderCallbacks<Cursor> {
    private Context mContext;
    private Uri mUri;
    private int mLoaderId;
    private ILoaderQueryListener mListener;

    private LoaderQueryBuilder(Context context, Uri uri, int loaderId,
                               ILoaderQueryListener listener) {
        mContext = context;
        mUri = uri;
        mLoaderId = loaderId;
        mListener = listener;
    }

    public static <T> void executeQuery(Activity activity,
                                        Uri uri,
                                        int loaderId,
                                        ILoaderQueryListener listener) {
        LoaderQueryBuilder<T> queryBuilder
                = new LoaderQueryBuilder<T>(activity, uri, loaderId, listener);
        activity.getLoaderManager().initLoader(loaderId, null, queryBuilder);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        if (id == mLoaderId) {
            return new CursorLoader(mContext, mUri, null, null, null, null);
        }
        return null;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == mLoaderId) {
            mListener.handleResults(cursor);
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == mLoaderId) {
            mListener.closeResults();
        }
    }
}
