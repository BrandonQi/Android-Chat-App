package edu.stevens.cs522.chat.web;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class Request implements Parcelable {
    private static final String TAG = Request.class.getSimpleName();

    public String mServerUrl;
    public long mClientId;
    public UUID mRegistrationId;
    public Map<String, String> mHeaders;

    public Request(String serverUrl, UUID registrationId, long clientId) {
        mServerUrl = serverUrl;
        mClientId = clientId;
        mRegistrationId = registrationId;
        mHeaders = new HashMap<String, String>();
        mHeaders.put("X-latitude", "40.7439905");
        mHeaders.put("X-longitude", "-74.0323626");
    }

    public Request(Parcel parcel) {
        mServerUrl = parcel.readString();
        mClientId = parcel.readLong();
        mRegistrationId = UUID.fromString(parcel.readString());

        Bundle bundle = parcel.readBundle();
        mHeaders = new HashMap<String, String>();
        mHeaders.put("X-latitude", bundle.getString("X-latitude"));
        mHeaders.put("X-longitude", bundle.getString("X-longitude"));
    }

    public Map<String, String> getRequestHeaders() {
        return mHeaders;
    }

    public abstract Uri getRequestUri();

    public Response getResponse(HttpURLConnection connection, JsonReader reader) {
        Response response = null;
        try {
            if (connection.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
                response = new Response.RegisterResponse(connection, reader);
                mClientId = ((Response.RegisterResponse) response).mClientId;
            }

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                response = new Response.SynchronizeResponse(connection);
            }
        } catch (IOException e) {
            Log.e(TAG, "HttpURLConnection.getResponseCode() failed: " + e);
        }
        return response;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mServerUrl);
        parcel.writeLong(mClientId);
        parcel.writeString(mRegistrationId.toString());

        Bundle bundle = new Bundle();
        for (Map.Entry<String, String> header : mHeaders.entrySet()) {
            bundle.putString(header.getKey(), header.getValue());
        }
        parcel.writeBundle(bundle);
    }
}
