package edu.stevens.cs522.chat.entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Peer implements Parcelable {
    public static final String COL_ID = "_id";
    public static final String COL_NAME = "name";
    public static final String COL_ADDRESS = "address";
    public static final String COL_PORT = "port";
    public static final Creator<Peer> CREATOR = new Creator<Peer>() {
        public Peer createFromParcel(Parcel source) {
            return new Peer(source);
        }

        public Peer[] newArray(int size) {
            return new Peer[size];
        }
    };
    private static final String TAG = Message.class.getSimpleName();
    public long id;
    public String name;
    public InetAddress address;
    public int port;
    public Peer(String name, InetAddress address, int port) {
        this.name = name;
        this.address = address;
        this.port = port;
    }

    public Peer(Parcel parcel) {
        id = parcel.readLong();
        name = parcel.readString();

        int addressLength = parcel.readInt();
        byte[] addressBytes = new byte[addressLength];
        try {
            address = InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            Log.w(TAG, e);
        }

        port = parcel.readInt();
    }

    public Peer(Cursor cursor) {
        id = getId(cursor);
        name = getName(cursor);
        address = getAddress(cursor);
        port = getPort(cursor);
    }

    public static long getId(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
    }

    public static String getName(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
    }

    public static void putName(ContentValues cv, String name) {
        cv.put(COL_NAME, name);
    }

    public static InetAddress getAddress(Cursor cursor) {
        byte[] bytes = cursor.getBlob(cursor.getColumnIndexOrThrow(COL_ADDRESS));

        try {
            return InetAddress.getByAddress(bytes);
        } catch (UnknownHostException e) {
            Log.w(TAG, e);
            return null;
        }
    }

    public static void putAddress(ContentValues cv, InetAddress address) {
        cv.put(COL_ADDRESS, address.getAddress());
    }

    public static int getPort(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(COL_PORT));
    }

    public static void putPort(ContentValues cv, int port) {
        cv.put(COL_PORT, port);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dst, int flags) {
        dst.writeLong(id);
        dst.writeString(name);

        int addressLength = address.getAddress().length;
        dst.writeInt(addressLength);
        dst.writeByteArray(address.getAddress());

        dst.writeInt(port);
    }

    public void writeToProvider(ContentValues cv) {
        putName(cv, name);
        putAddress(cv, address);
        putPort(cv, port);
    }

}
