package edu.stevens.cs522.chat.entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class Message implements Parcelable {
    /* persistent table schema */
    public static final String COL_ID = "_id";
    public static final String COL_MESSAGE = "message";
    public static final String COL_SENDER = "sender";
    public static final String COL_PEERS_FK = "peers_fk";
    /*
     * Parcelable
     */
    public static final Creator<Message> CREATOR = new Creator<Message>() {
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
    private static final String TAG = Message.class.getSimpleName();
    public long id;
    public String message;
    public String sender;

    public Message(String message, String sender) {
        this.message = message;
        this.sender = sender;
    }

    public Message(Parcel parcel) {
        id = parcel.readLong();
        message = parcel.readString();
        sender = parcel.readString();
    }

    public Message(Cursor cursor) {
        message = getMessage(cursor);
        sender = getSender(cursor);
    }

    public static String getMessage(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(COL_MESSAGE));
    }

    public static void putMessage(ContentValues cv, String message) {
        cv.put(COL_MESSAGE, message);
    }

    public static String getSender(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(COL_SENDER));
    }

    public static void putSender(ContentValues cv, String sender) {
        cv.put(COL_SENDER, sender);
    }

    public static void putPeersFK(ContentValues cv, long peersFK) {
        cv.put(COL_PEERS_FK, peersFK);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dst, int flags) {
        dst.writeLong(id);
        dst.writeString(message);
        dst.writeString(sender);
    }

    public void writeToProvider(ContentValues cv, long peersFK) {
        putMessage(cv, message);
        putSender(cv, sender);
        putPeersFK(cv, peersFK);
    }
}
