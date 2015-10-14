package edu.stevens.cs522.chat.web;

import android.net.Uri;
import android.os.Parcel;
import android.util.JsonWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.UUID;

public class PostMessage extends Request {
    public String string;
    public Date date;

    public static final Creator<PostMessage> CREATOR = new Creator<PostMessage>() {
        public PostMessage createFromParcel(Parcel source) {
            return new PostMessage(source);
        }
        public PostMessage[] newArray(int size) {
            return new PostMessage[size];
        }
    };

    public PostMessage(String serverUrl, UUID registrationId,long clientId, String text) {
        super(serverUrl, registrationId, clientId);
        string = text;
        date = new Date();
    }

    public PostMessage(Parcel parcel) {
        super(parcel);
        string = parcel.readString();
        date = new Date(parcel.readLong());
    }

    @Override
    public Uri getRequestUri() {
        return Uri.parse(mServerUrl+ "/" + mClientId+ "?regid=" + mRegistrationId.toString());
    }

    void outputRequestEntity(HttpURLConnection connection) throws IOException {
        connection.setRequestProperty("Content-Type", "application/json");
        JsonWriter writer = new JsonWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8")));

        writer.beginObject();
        writer.name("chatroom");
        writer.value("_default");
        writer.name("timestamp");
        writer.value(date.getTime());
        writer.name("text");
        writer.value(string);
        writer.endObject();
        writer.flush();
        writer.close();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeString(string);
        parcel.writeLong(date.getTime());
    }
}
