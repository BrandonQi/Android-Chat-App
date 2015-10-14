package edu.stevens.cs522.chat.web;

import android.net.Uri;
import android.os.Parcel;
import android.util.JsonWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.UUID;

public class Synchronize extends Request {
    public static final Creator<Synchronize> CREATOR = new Creator<Synchronize>() {
        public Synchronize createFromParcel(Parcel source) {
            return new Synchronize(source);
        }

        public Synchronize[] newArray(int size) {
            return new Synchronize[size];
        }
    };
    public long mSequenceNum;

    public Synchronize(String serverUrl, UUID registrationId,long clientId) {
        super(serverUrl, registrationId, clientId);
    }

    public Synchronize(Parcel parcel) {
        super(parcel);
        mSequenceNum = parcel.readLong();
    }
    @Override
    public Uri getRequestUri() {
        return Uri.parse(mServerUrl
                + "/" + mClientId
                + "?regid=" + mRegistrationId.toString()
                + "&seqnum=" + mSequenceNum);
    }

    public void outputRequestEntity(HttpURLConnection connection) throws IOException {
        connection.setRequestProperty("Content-Type", "application/json");
        JsonWriter writer = new JsonWriter(new BufferedWriter(
                new OutputStreamWriter(connection.getOutputStream(), "UTF-8")));
        writer.flush();
        writer.close();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeLong(mSequenceNum);
    }
}
