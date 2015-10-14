package edu.stevens.cs522.chat.entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class WebMessage implements Parcelable {
    public static final String COL_ID = "_id";
    public static final String COL_TIMESTAMP = "timestamp";
    public static final String COL_SEQUENCE_NO = "sequence_no";
    public static final String COL_SENDER_ID = "sender_id";
    public static final String COL_MESSAGE = "message";
    public static final Creator<WebMessage> CREATOR = new Creator<WebMessage>() {
        public WebMessage createFromParcel(Parcel source) {
            return new WebMessage(source);
        }

        public WebMessage[] newArray(int size) {
            return new WebMessage[size];
        }
    };
    public long id;
    public Date timestamp;
    public long sequenceNum;
    public long senderId;
    public String message;

    public WebMessage(Date timestamp, long sequenceNum, long senderId, String message) {
        this.timestamp = timestamp;
        this.sequenceNum = sequenceNum;
        this.senderId = senderId;
        this.message = message;
    }

    public WebMessage(Parcel parcel) {
        id = parcel.readLong();
        timestamp = new Date(parcel.readLong());
        sequenceNum = parcel.readLong();
        senderId = parcel.readLong();
        message = parcel.readString();
    }

    public WebMessage(Cursor cursor) {
        timestamp = getTimestamp(cursor);
        sequenceNum = getSequenceNo(cursor);
        senderId = getSenderId(cursor);
        message = getMessage(cursor);
    }

    public static Date getTimestamp(Cursor cursor) {
        return new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP)));
    }

    public static void putTimestamp(ContentValues cv, Date timestamp) {
        cv.put(COL_TIMESTAMP, timestamp.getTime());
    }

    public static long getSequenceNo(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(COL_SEQUENCE_NO));
    }

    public static void putSequenceNo(ContentValues cv, long sequenceNo) {
        cv.put(COL_SEQUENCE_NO, sequenceNo);
    }

    public static long getSenderId(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(COL_SENDER_ID));
    }

    public static void putSenderId(ContentValues cv, long senderId) {
        cv.put(COL_SENDER_ID, senderId);
    }

    public static String getMessage(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(COL_MESSAGE));
    }

    public static void putMessage(ContentValues cv, String message) {
        cv.put(COL_MESSAGE, message);
    }

    @Override
    public String toString() {
        return "{" + timestamp.getTime() + ", "
                + sequenceNum + ", "
                + senderId + ", "
                + message + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dst, int flags) {
        dst.writeLong(id);
        dst.writeLong(timestamp.getTime());
        dst.writeLong(sequenceNum);
        dst.writeLong(senderId);
        dst.writeString(message);
    }

    public void writeToProvider(ContentValues cv) {
        putTimestamp(cv, timestamp);
        putSequenceNo(cv, sequenceNum);
        putSenderId(cv, senderId);
        putMessage(cv, message);
    }
}
