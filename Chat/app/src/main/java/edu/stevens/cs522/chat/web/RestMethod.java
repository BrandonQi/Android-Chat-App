package edu.stevens.cs522.chat.web;

import android.util.JsonReader;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

public class RestMethod {
    public static Response perform(Register request) throws IOException {
        URL url = new URL(request.getRequestUri().toString());

        URLConnection urlConnection = url.openConnection();

        if (!(urlConnection instanceof HttpURLConnection)) {
            throw new IOException("Not an HTTP connection!");
        }

        HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
        initConnectionProperties(httpURLConnection, request);

        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setDoInput(true);
        httpURLConnection.connect();

        throwErrors(httpURLConnection);
        JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(
                httpURLConnection.getInputStream())));
        Response response = request.getResponse(httpURLConnection, reader);
        reader.close();
        httpURLConnection.disconnect();

        return response;
    }

    public static Response perform(Synchronize request, IStreamingOutput output)
            throws IOException {
        URL url = new URL(request.getRequestUri().toString());

        URLConnection urlConnection = url.openConnection();

        if (!(urlConnection instanceof HttpURLConnection)) {
            throw new IOException("Not an HTTP connection!");
        }

        HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
        initConnectionProperties(httpURLConnection, request);

        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setDoInput(true);

        if (output.outputRequestEntity(httpURLConnection) == false) {
            httpURLConnection.disconnect();
            return null;
        }

        throwErrors(httpURLConnection);
        return request.getResponse(httpURLConnection, null);
    }

    private static void initConnectionProperties(HttpURLConnection connection, Request request) {
        connection.setUseCaches(false);
        connection.setRequestProperty("CONNECTION", "Keep-Alive");

        Map<String, String> headers = request.getRequestHeaders();
        for (Map.Entry<String, String> header : headers.entrySet()) {
            connection.addRequestProperty(header.getKey(), header.getValue());
        }
    }

    private static void throwErrors(HttpURLConnection connection) throws IOException {
        final int responseCode = connection.getResponseCode();
        if (responseCode < 200 || responseCode > 300) {
            String exceptionMessage = "Error Response " + responseCode
                    + " " + connection.getResponseMessage()
                    + " for " + connection.getURL();

            throw new IOException(exceptionMessage);
        }
    }
}
