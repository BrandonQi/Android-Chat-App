package edu.stevens.cs522.chat.web;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.ResultReceiver;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;

import edu.stevens.cs522.chat.contracts.ChatContract;
import edu.stevens.cs522.chat.entities.WebMessage;
import edu.stevens.cs522.chat.entities.WebPeer;

public class RequestProcessor implements IStreamingOutput {
    private static final String TAG = RequestProcessor.class.getSimpleName();
    Context context;

    public RequestProcessor(Context context) {
        this.context = context;
    }

    public long perform(Register request) {
        try {
            Response.RegisterResponse response = (Response.RegisterResponse) RestMethod.perform(request);
            return response.mClientId;
        } catch (IOException e) {
            Log.e(TAG, "RestMethod.perform(Register) failed: " + e);
            return 0;
        }
    }

    public void perform(WebMessage message) {
        ContentValues cv = new ContentValues();
        message.writeToProvider(cv);
        context.getContentResolver().insert(ChatContract.CONTENT_URI_WEB_MESSAGES, cv);
    }

    public void perform(Synchronize request, ResultReceiver syncingReceiver) {
        Cursor cursorMaxSeq = context.getContentResolver().query(ChatContract.CONTENT_URI_WEB_MESSAGES, null, null, null, "MAX");
        WebMessage message = new WebMessage(cursorMaxSeq);
        cursorMaxSeq.close();
        request.mSequenceNum = message.sequenceNum;

        try {
            Response.SynchronizeResponse response = (Response.SynchronizeResponse) RestMethod.perform(request, this);
            if (response == null) {
                return;
            }

            context.getContentResolver().delete(ChatContract.CONTENT_URI_WEB_MESSAGES, null, null);

            JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(response.mHttpURLConnection.getInputStream())));
            reader.beginObject();

            Response.matchName("clients", reader);
            reader.beginArray();
            ArrayList<WebPeer> webPeers = new ArrayList<WebPeer>();
            while (reader.hasNext()) {
                reader.beginObject();
                Response.matchName("sender", reader);
                WebPeer webPeer = new WebPeer(webPeers.size() + 1, reader.nextString());
                webPeers.add(webPeer);
                Response.matchName("X-latitude", reader);
                reader.nextDouble();
                Response.matchName("X-longitude", reader);
                reader.nextDouble();
                reader.endObject();
            }
            reader.endArray();

            Cursor cursorWebPeers = context.getContentResolver().query(
                    ChatContract.CONTENT_URI_WEB_PEERS,
                    null, null, null, null);
            int localWebPeersCount = cursorWebPeers.getCount();
            cursorWebPeers.close();
            if (localWebPeersCount < webPeers.size()) {
                for (int i = localWebPeersCount; i < webPeers.size(); i++) {
                    ContentValues cv = new ContentValues();
                    webPeers.get(i).writeToProvider(cv);
                    context.getContentResolver().insert(
                            ChatContract.CONTENT_URI_WEB_PEERS, cv);
                }
            }

            Response.matchName("messages", reader);
            int messageCount = 0;
            reader.beginArray();
            while (reader.hasNext()) {
                messageCount++;
                reader.beginObject();
                Response.matchName("chatroom", reader);
                reader.nextString();
                Response.matchName("timestamp", reader);
                Date timestamp = new Date(reader.nextLong());
                Response.matchName("X-latitude", reader);
                reader.nextDouble();
                Response.matchName("X-longitude", reader);
                reader.nextDouble();
                Response.matchName("seqnum", reader);
                long sequenceNum = reader.nextLong();
                Response.matchName("sender", reader);
                String sender = reader.nextString();
                long senderId = 0;
                for (WebPeer webPeer : webPeers) {
                    if (webPeer.name.compareTo(sender) == 0) {
                        senderId = webPeer.id;
                    }
                }
                if (senderId == 0) {
                    throw new IOException("illegal message. unknown sender: " + sender);
                }
                Response.matchName("text", reader);
                String text = reader.nextString();

                WebMessage webMessage = new WebMessage(timestamp, sequenceNum, senderId, text);
                ContentValues cv = new ContentValues();
                webMessage.writeToProvider(cv);
                context.getContentResolver().insert(ChatContract.CONTENT_URI_WEB_MESSAGES, cv);

                reader.endObject();
            }
            reader.endArray();
            Log.d(TAG, "Syncing messages: " + messageCount);

            reader.endObject();
            reader.close();
            response.mHttpURLConnection.disconnect();
            syncingReceiver.send(0, null);
        } catch (IOException e) {
            Log.e(TAG, "Synchronization failed: " + e);
        }
    }

    public boolean outputRequestEntity(HttpURLConnection connection)
            throws IOException {
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setChunkedStreamingMode(0);

        final JsonWriter writer = new JsonWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8")));

        Cursor cursorWebMessages = context.getContentResolver().query(ContentUris.withAppendedId(ChatContract.CONTENT_URI_WEB_MESSAGES, 0), null, null, null, null);
        if (cursorWebMessages.getCount() == 0) {
            return false;
        }

        try {
            writer.beginArray();
            if (cursorWebMessages.moveToFirst()) {
                do {
                    WebMessage webMessage = new WebMessage(cursorWebMessages);
                    writer.beginObject();
                    writer.name("chatroom");
                    writer.value("_default");
                    writer.name("timestamp");
                    writer.value(webMessage.timestamp.getTime());
                    writer.name("text");
                    writer.value(webMessage.message);
                    writer.endObject();
                }
                while (cursorWebMessages.moveToNext());
            }
            writer.endArray();
        } catch (IOException e) {
            Log.e(TAG, "Output synchronization Request Entity failed: " + e);
        }

        writer.flush();
        writer.close();
        return true;
    }
}
