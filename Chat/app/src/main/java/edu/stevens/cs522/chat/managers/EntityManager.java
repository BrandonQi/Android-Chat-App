package edu.stevens.cs522.chat.managers;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import edu.stevens.cs522.chat.contracts.ChatContract;
import edu.stevens.cs522.chat.entities.Message;
import edu.stevens.cs522.chat.entities.Peer;

public class EntityManager<T> {
    private final Context mContext;
    private final IEntityCreator<T> mCreator;
    private final int mLoaderId;

    private AsyncContentResolver mAsyncResolver;
    private ContentResolver mContentResolver;

    public EntityManager(Context context, IEntityCreator<T> creator, int loaderId) {
        mContext = context;
        mCreator = creator;
        mLoaderId = loaderId;
    }

    private AsyncContentResolver getAsyncResolver() {
        if (mAsyncResolver == null) {
            mAsyncResolver = new AsyncContentResolver(mContext.getContentResolver());
        }
        return mAsyncResolver;
    }

    private ContentResolver getContentResolver() {
        if (mContentResolver == null) {
            mContentResolver = mContext.getContentResolver();
        }
        return mContentResolver;
    }

    public void persistAsync(final Peer peer, final Message message) {
        ContentValues cvPeer = new ContentValues();
        peer.writeToProvider(cvPeer);
        getAsyncResolver().insertAsync(ChatContract.CONTENT_URI_PEERS, cvPeer,
                new IContinue<Uri>() {
                    public void kontinue(Uri value) {
                        peer.id = ContentUris.parseId(value);
                        ContentValues cvMessage = new ContentValues();
                        message.writeToProvider(cvMessage, peer.id);
                        getAsyncResolver().insertAsync(ChatContract.CONTENT_URI_MESSAGES,
                                cvMessage, null);
                    }
                });
    }

    public void updateAsync(final long rowId, final Peer peer, final Message message) {
        ContentValues cvPeer = new ContentValues();
        peer.writeToProvider(cvPeer);
        getAsyncResolver().updateAsync(
                ContentUris.withAppendedId(ChatContract.CONTENT_URI_PEERS, rowId),
                cvPeer,
                new IContinue<Integer>() {
                    public void kontinue(Integer value) {
                        ContentValues cvMessage = new ContentValues();
                        message.writeToProvider(cvMessage, rowId);
                        getAsyncResolver().insertAsync(ChatContract.CONTENT_URI_MESSAGES,
                                cvMessage, null);
                    }
                });
    }

    public void executeAsyncQuery(Uri uri, String[] selectionArgs,
                                  IAsyncQueryListener<T> listener) {
        AsyncQueryBuilder.executeQuery(mContext, uri, selectionArgs, mCreator, listener);
    }

    public void executeLoaderQuery(Uri uri, ILoaderQueryListener listener) {
        LoaderQueryBuilder.executeQuery((Activity) mContext, uri, mLoaderId, listener);
    }

    public List<T> executeSyncQuery(Uri uri, String[] projection, String selection,
                                    String[] selectionArgs, String sortOrder) {
        Cursor cursor = mContext.getContentResolver().query(uri, projection,
                selection, selectionArgs, sortOrder);
        List<T> instances = new ArrayList<T>();
        if (cursor.moveToFirst()) {
            do {
                T instance = mCreator.create(cursor);
                instances.add(instance);
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return instances;
    }
}
