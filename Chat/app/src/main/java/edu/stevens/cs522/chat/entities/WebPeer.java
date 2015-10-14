package edu.stevens.cs522.chat.entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class WebPeer implements Parcelable {
    public static final String COL_ID = "_id";
    public static final String COL_NAME = "name";
    public static final Creator<WebPeer> CREATOR = new Creator<WebPeer>() {
        public WebPeer createFromParcel(Parcel source) {
            return new WebPeer(source);
        }

        public WebPeer[] newArray(int size) {
            return new WebPeer[size];
        }
    };
    public long id;
    public String name;
    public WebPeer(long senderId, String name) {
        this.id = senderId;
        this.name = name;
    }

    public WebPeer(Parcel parcel) {
        id = parcel.readLong();
        name = parcel.readString();
    }

    public WebPeer(Cursor cursor) {
        id = getSenderId(cursor);
        name = getName(cursor);
    }

    public static long getSenderId(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
    }
    public static void putSenderId(ContentValues cv, long senderId) {
        cv.put(COL_ID, senderId);
    }

    public static String getName(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
    }

    public static void putName(ContentValues cv, String name) {
        cv.put(COL_NAME, name);
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(id);
        parcel.writeString(name);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToProvider(ContentValues cv) {
        putSenderId(cv, id);
        putName(cv, name);
    }
}
