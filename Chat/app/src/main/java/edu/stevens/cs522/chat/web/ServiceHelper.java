package edu.stevens.cs522.chat.web;

import android.app.Activity;
import android.content.Intent;
import android.os.ResultReceiver;

import java.util.Date;
import java.util.UUID;

import edu.stevens.cs522.chat.entities.WebMessage;
import edu.stevens.cs522.chat.services.RequestService;

public class ServiceHelper {
    private Activity activity;

    public static final int REQUEST_REGISTER = 1000;
    public static final int REQUEST_MESSAGE = 2000;
    public static final int REQUEST_SYNCHRONIZE = 30000;

    private static final String PREFIX = ServiceHelper.class.getSimpleName();

    public static final String KEY_REQUEST = PREFIX + ".key_request_type";
    public static final String KEY_REGISTER = PREFIX + ".key_register";
    public static final String KEY_MESSAGE = PREFIX + ".key_message_request";
    public static final String KEY_SYNCHRONIZE = PREFIX + ".key_synchronize";
    public static final String KEY_RESULT_RECEIVER = PREFIX + ".key_result_receiver";

    public ServiceHelper(Activity activity) {
        this.activity = activity;
    }

    public void register(String serverUrl, UUID registrationId, String username,ResultReceiver resultReceiver) {
        Register request = new Register(serverUrl, registrationId, username);
        Intent intent = new Intent(activity, RequestService.class);
        intent.putExtra(KEY_REQUEST, REQUEST_REGISTER);
        intent.putExtra(KEY_REGISTER, request);
        intent.putExtra(KEY_RESULT_RECEIVER, resultReceiver);
        activity.startService(intent);
    }

    public void postMessage(Date timestamp, long senderId, String text) {
        WebMessage webMessage = new WebMessage(timestamp, 0, senderId, text);
        Intent intent = new Intent(activity, RequestService.class);
        intent.putExtra(KEY_REQUEST, REQUEST_MESSAGE);
        intent.putExtra(KEY_MESSAGE, webMessage);
        activity.startService(intent);
    }

    public void synchronize(String serverUrl, UUID registrationId, long clientId,ResultReceiver receiver) {
        Synchronize request = new Synchronize(serverUrl, registrationId, clientId);
        Intent intent = new Intent(activity, RequestService.class);
        intent.putExtra(KEY_REQUEST, REQUEST_SYNCHRONIZE);
        intent.putExtra(KEY_SYNCHRONIZE, request);
        intent.putExtra(KEY_RESULT_RECEIVER, receiver);
        activity.startService(intent);
    }
}
