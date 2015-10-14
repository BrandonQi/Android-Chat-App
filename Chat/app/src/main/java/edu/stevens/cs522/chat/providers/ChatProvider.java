package edu.stevens.cs522.chat.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.util.Log;

import edu.stevens.cs522.chat.contracts.ChatContract;
import edu.stevens.cs522.chat.databases.MessageDbAdapter;
import edu.stevens.cs522.chat.entities.Peer;

public class ChatProvider extends ContentProvider {
    private static final String TAG = ChatProvider.class.getSimpleName();

    private static final int ALL_ITEMS_PEERS = 1;
    private static final int SINGLE_ITEM_PEERS = 2;
    private static final int ALL_ITEMS_MESSAGES = 3;
    private static final int SINGLE_ITEM_MESSAGES = 4;
    private static final int ALL_ITEMS_WEB_PEERS = 5;
    private static final int SINGLE_ITEM_WEB_PEERS = 6;
    private static final int ALL_ITEMS_WEB_MESSAGES = 7;
    private static final int SINGLE_ITEM_WEB_MESSAGES = 8;

    private static final UriMatcher URI_MATCHER;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(ChatContract.AUTHORITY, ChatContract.PATH_PEERS,
                ALL_ITEMS_PEERS);
        URI_MATCHER.addURI(ChatContract.AUTHORITY, ChatContract.PATH_PEERS + "/#",
                SINGLE_ITEM_PEERS);
        URI_MATCHER.addURI(ChatContract.AUTHORITY, ChatContract.PATH_MESSAGES,
                ALL_ITEMS_MESSAGES);
        URI_MATCHER.addURI(ChatContract.AUTHORITY, ChatContract.PATH_MESSAGES + "/#",
                SINGLE_ITEM_MESSAGES);
        URI_MATCHER.addURI(ChatContract.AUTHORITY, ChatContract.PATH_WEB_PEERS,
                ALL_ITEMS_WEB_PEERS);
        URI_MATCHER.addURI(ChatContract.AUTHORITY, ChatContract.PATH_WEB_PEERS + "/#",
                SINGLE_ITEM_WEB_PEERS);
        URI_MATCHER.addURI(ChatContract.AUTHORITY, ChatContract.PATH_WEB_MESSAGES,
                ALL_ITEMS_WEB_MESSAGES);
        URI_MATCHER.addURI(ChatContract.AUTHORITY, ChatContract.PATH_WEB_MESSAGES + "/#",
                SINGLE_ITEM_WEB_MESSAGES);
    }

    MessageDbAdapter mDb;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mDb = new MessageDbAdapter(context);
        mDb.open();
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case ALL_ITEMS_PEERS:
                return ChatContract.CONTENT_TYPE_ALL_ITEMS_PEERS;
            case SINGLE_ITEM_PEERS:
                return ChatContract.CONTENT_TYPE_SINGLE_ITEM_PEERS;
            case ALL_ITEMS_MESSAGES:
                return ChatContract.CONTENT_TYPE_ALL_ITEMS_MESSAGES;
            case SINGLE_ITEM_MESSAGES:
                return ChatContract.CONTENT_TYPE_SINGLE_ITEM_MESSAGES;

            case ALL_ITEMS_WEB_PEERS:
                return ChatContract.CONTENT_TYPE_ALL_ITEMS_WEB_PEERS;
            case SINGLE_ITEM_WEB_PEERS:
                return ChatContract.CONTENT_TYPE_SINGLE_ITEM_WEB_PEERS;
            case ALL_ITEMS_WEB_MESSAGES:
                return ChatContract.CONTENT_TYPE_ALL_ITEMS_WEB_MESSAGES;
            case SINGLE_ITEM_WEB_MESSAGES:
                return ChatContract.CONTENT_TYPE_SINGLE_ITEM_WEB_MESSAGES;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues cv) {
        long rowId = -1;
        switch (URI_MATCHER.match(uri)) {
            case ALL_ITEMS_PEERS:
                rowId = mDb.insertPeer(cv);
                break;
            case ALL_ITEMS_MESSAGES:
                rowId = mDb.insertMessage(cv);
                break;
            case ALL_ITEMS_WEB_PEERS:
                rowId = mDb.insertWebPeer(cv);
                break;
            case ALL_ITEMS_WEB_MESSAGES:
                rowId = mDb.insertWebMessage(cv);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        if (rowId > 0) {
            Uri itemUri = ContentUris.withAppendedId(uri, rowId);
            getContext().getContentResolver().notifyChange(itemUri, null);
            return itemUri;
        }
        throw new SQLException("Insertion failed. URI: " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        switch (URI_MATCHER.match(uri)) {
            case ALL_ITEMS_WEB_MESSAGES:
                return mDb.deleteObsoleteWebMessages();
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues cv, String where, String[] whereArgs) {
        if (URI_MATCHER.match(uri) == SINGLE_ITEM_PEERS) {
            long id = ContentUris.parseId(uri);
            return mDb.updatePeer(cv, Peer.COL_ID + "=?",
                    new String[]{Long.toString(id)});
        }
        throw new IllegalArgumentException("Unsupported URI: " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sort) {
        Cursor cursor;
        long id;
        switch (URI_MATCHER.match(uri)) {
            case ALL_ITEMS_PEERS:
                if (selectionArgs == null) {
                    Log.d(TAG, "query() uri=" + uri);
                    cursor = mDb.fetchPeers();
                } else {
                    Log.d(TAG, "query() uri=" + uri
                            + ", selectionArgs[0]=" + selectionArgs[0]);
                    cursor = mDb.fetchPeer(selectionArgs[0]);
                }
                break;
            case SINGLE_ITEM_PEERS:
                id = ContentUris.parseId(uri);
                cursor = mDb.fetchPeer(id);
                break;
            case ALL_ITEMS_MESSAGES:
                cursor = mDb.fetchMessages();
                break;
            case SINGLE_ITEM_MESSAGES:
                id = ContentUris.parseId(uri);
                cursor = mDb.fetchMessages(id);
                break;
            case ALL_ITEMS_WEB_PEERS:
                cursor = mDb.fetchWebPeers();
                break;
            case SINGLE_ITEM_WEB_PEERS:
                id = ContentUris.parseId(uri);
                cursor = mDb.fetchWebPeer(id);
                break;
            case ALL_ITEMS_WEB_MESSAGES:
                if (sort == null) {
                    cursor = mDb.fetchWebMessages();
                } else {
                    cursor = mDb.fetchMaxSequenceWebMessage();
                }
                break;
            case SINGLE_ITEM_WEB_MESSAGES:
                id = ContentUris.parseId(uri);
                if (id == 0) {
                    cursor = mDb.fetchUnsynchronizedWebMessages();
                } else {
                    cursor = mDb.fetchWebMessages(id);
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

}
