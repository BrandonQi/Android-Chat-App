package edu.stevens.cs522.chat.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import edu.stevens.cs522.chat.entities.Message;
import edu.stevens.cs522.chat.entities.Peer;
import edu.stevens.cs522.chat.entities.WebMessage;
import edu.stevens.cs522.chat.entities.WebPeer;

public class MessageDbAdapter {
    private static final String TAG = MessageDbAdapter.class.getSimpleName();

    public static final int VERSION = 1;
    Context context;
    DatabaseHelper databaseHelper;
    SQLiteDatabase sqLiteDatabase;

    public static final String DB_NAME = "chat.history.db";
    public static final String TABLE_PEERS = "peers";
    private static final String CREATE_PEERS = "CREATE TABLE "
            + TABLE_PEERS + " ("
            + Peer.COL_ID + " INTEGER PRIMARY KEY, "
            + Peer.COL_NAME + " TEXT NOT NULL, "
            + Peer.COL_ADDRESS + " BLOB NOT NULL, "
            + Peer.COL_PORT + " INTEGER NOT NULL" + ");";
    public static final String TABLE_MESSAGES = "messages";
    private static final String CREATE_MESSAGES = "CREATE TABLE "
            + TABLE_MESSAGES + " ("
            + Message.COL_ID + " INTEGER PRIMARY KEY, "
            + Message.COL_MESSAGE + " TEXT NOT NULL, "
            + Message.COL_SENDER + " TEXT NOT NULL, "
            + Message.COL_PEERS_FK + " INTEGER NOT NULL, "
            + "FOREIGN KEY (" + Message.COL_PEERS_FK + ") REFERENCES " + TABLE_PEERS
            + "(" + Peer.COL_ID + ") ON DELETE CASCADE" + ");";
    private static final String CREATE_MESSAGE_INDEX = "CREATE INDEX IF NOT EXISTS "
            + TABLE_MESSAGES + TABLE_PEERS + " ON "
            + TABLE_MESSAGES + "(" + Message.COL_PEERS_FK + ");";
    public static final String TABLE_WEB_PEERS = "web_peers";
    private static final String CREATE_WEB_PEERS = "CREATE TABLE "
            + TABLE_WEB_PEERS + " ("
            + Peer.COL_ID + " INTEGER PRIMARY KEY, "
            + Peer.COL_NAME + " TEXT NOT NULL" + ");";
    public static final String TABLE_WEB_MESSAGES = "web_messages";
    private static final String CREATE_WEB_MESSAGES = "CREATE TABLE "
            + TABLE_WEB_MESSAGES + " ("
            + WebMessage.COL_ID + " INTEGER PRIMARY KEY, "
            + WebMessage.COL_TIMESTAMP + " INTEGER NOT NULL, "
            + WebMessage.COL_SEQUENCE_NO + " INTEGER NOT NULL, "
            + WebMessage.COL_SENDER_ID + " INTEGER NOT NULL, "
            + WebMessage.COL_MESSAGE + " TEXT NOT NULL" + " );";
    private static final String CREATE_WEB_MESSAGE_INDEX = "CREATE INDEX IF NOT EXISTS "
            + TABLE_WEB_MESSAGES + TABLE_WEB_PEERS + " ON "
            + TABLE_WEB_MESSAGES + "(" + WebMessage.COL_SENDER_ID + ");";
    private static final String SELECT_ALL_WEB_MESSAGES = "SELECT "
            + TABLE_WEB_MESSAGES + "." + WebMessage.COL_ID + ", "
            + WebMessage.COL_SEQUENCE_NO + ", "
            + WebMessage.COL_MESSAGE + ", "
            + WebPeer.COL_NAME
            + " FROM " + TABLE_WEB_MESSAGES + " JOIN " + TABLE_WEB_PEERS + " ON "
            + TABLE_WEB_MESSAGES + "." + WebMessage.COL_SENDER_ID + "="
            + TABLE_WEB_PEERS + "." + WebPeer.COL_ID
            + " ORDER BY " + WebMessage.COL_SEQUENCE_NO;

    public MessageDbAdapter(Context context) {
        this.context = context;
        databaseHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        sqLiteDatabase = databaseHelper.getWritableDatabase();
        sqLiteDatabase.execSQL("PRAGMA foreign_keys=ON;");
    }

    public void close() {
        databaseHelper.close();
    }

    public long insertPeer(ContentValues cv) {
        return sqLiteDatabase.insert(TABLE_PEERS, null, cv);
    }

    public long insertMessage(ContentValues cv) {
        return sqLiteDatabase.insert(TABLE_MESSAGES, null, cv);
    }

    public long insertWebPeer(ContentValues cv) {
        return sqLiteDatabase.insert(TABLE_WEB_PEERS, null, cv);
    }

    public long insertWebMessage(ContentValues cv) {
        return sqLiteDatabase.insert(TABLE_WEB_MESSAGES, null, cv);
    }

    public int deleteObsoleteWebMessages() {
        return sqLiteDatabase.delete(TABLE_WEB_MESSAGES,
                WebMessage.COL_SEQUENCE_NO + "=?",
                new String[]{"0"});
    }

    public int updatePeer(ContentValues cv, String where, String[] whereArgs) {
        return sqLiteDatabase.update(TABLE_PEERS, cv, where, whereArgs);
    }

    public Cursor fetchPeers() {
        return sqLiteDatabase.query(TABLE_PEERS,
                new String[]{Peer.COL_ID, Peer.COL_NAME},
                null, null, null, null, null);
    }

    public Cursor fetchPeer(String name) {
        Cursor cursor = sqLiteDatabase.query(TABLE_PEERS,
                null,
                Peer.COL_NAME + "=?",
                new String[]{name},
                null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor fetchPeer(long rowId) {
        Cursor cursor = sqLiteDatabase.query(TABLE_PEERS,
                new String[]{Peer.COL_ID, Peer.COL_NAME, Peer.COL_ADDRESS, Peer.COL_PORT},
                Peer.COL_ID + "=?",
                new String[]{Long.toString(rowId)},
                null, null, null);
        if (cursor.getCount() == 0) {
            Log.e(TAG, "fetchPeer:" + rowId + " No record");
            return null;
        }
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor fetchMessages() {
        return sqLiteDatabase.query(TABLE_MESSAGES,
                null, null, null, null, null, null);
    }

    public Cursor fetchMessages(long foreignId) {
        return sqLiteDatabase.query(TABLE_MESSAGES,
                new String[]{Message.COL_ID, Message.COL_MESSAGE},
                Message.COL_PEERS_FK + "=?", new String[]{Long.toString(foreignId)},
                null, null, null);
    }

    public Cursor fetchWebPeers() {
        return sqLiteDatabase.query(TABLE_WEB_PEERS,
                null, null, null, null, null, null);
    }

    public Cursor fetchWebPeer(long id) {
        Cursor cursor = sqLiteDatabase.query(TABLE_WEB_PEERS,
                null,
                WebPeer.COL_ID + "=?", new String[]{Long.toString(id)},
                null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor fetchWebMessages() {
        return sqLiteDatabase.rawQuery(SELECT_ALL_WEB_MESSAGES, null);
    }

    public Cursor fetchWebMessages(long senderId) {
        return sqLiteDatabase.query(TABLE_WEB_MESSAGES,
                null,
                WebMessage.COL_SENDER_ID + "=?", new String[]{Long.toString(senderId)},
                null, null, WebMessage.COL_SEQUENCE_NO);
    }

    public Cursor fetchMaxSequenceWebMessage() {
        Cursor cursor = sqLiteDatabase.query(TABLE_WEB_MESSAGES, null, null, null, null, null,
                WebMessage.COL_SEQUENCE_NO + " DESC ", "1");
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor fetchUnsynchronizedWebMessages() {
        Cursor cursor = sqLiteDatabase.query(TABLE_WEB_MESSAGES, null,
                WebMessage.COL_SEQUENCE_NO + "=?", new String[]{"0"},
                null, null, null);
        cursor.moveToFirst();
        return cursor;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DB_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                Log.d(TAG, CREATE_PEERS);
                db.execSQL(CREATE_PEERS);

                Log.d(TAG, CREATE_MESSAGES);
                db.execSQL(CREATE_MESSAGES);

                Log.d(TAG, CREATE_MESSAGE_INDEX);
                db.execSQL(CREATE_MESSAGE_INDEX);

                Log.d(TAG, CREATE_WEB_PEERS);
                db.execSQL(CREATE_WEB_PEERS);

                Log.d(TAG, CREATE_WEB_MESSAGES);
                db.execSQL(CREATE_WEB_MESSAGES);

                Log.d(TAG, CREATE_WEB_MESSAGE_INDEX);
                db.execSQL(CREATE_WEB_MESSAGE_INDEX);
            } catch (SQLException e) {
                Log.e(TAG, e.toString());
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "onUpgrade: " + oldVersion + "->" + newVersion);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PEERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEB_PEERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEB_MESSAGES);
            onCreate(db);
        }
    }
}
