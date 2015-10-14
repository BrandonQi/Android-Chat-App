package edu.stevens.cs522.chat.services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.activities.activityChat;

public class ChatSenderService extends Service {
    private static final String TAG = ChatSenderService.class.getSimpleName();
    Messenger mMessenger;
    private DatagramSocket mSenderSocket;

    @Override
    public void onCreate() {
        super.onCreate();

        /* create a background thread to process message from message channel */
        HandlerThread messengerThread = new HandlerThread(TAG,
                Process.THREAD_PRIORITY_BACKGROUND);
        messengerThread.start();
        MessageHandler handler = new MessageHandler(messengerThread.getLooper());
        mMessenger = new Messenger(handler);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()...");

        int sourcePort = Integer.parseInt(getResources().getString(R.string.sending_port));
        try {
            mSenderSocket = new DatagramSocket(sourcePort);
        } catch (SocketException e) {
            Log.e(TAG, "Cannot open socket: " + e);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()...");

        if (mSenderSocket != null) {
            mSenderSocket.close();
            mSenderSocket = null;
        }
    }

    public class MessageHandler extends Handler {
        public MessageHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            Bundle bundle = message.getData();
            InetAddress destAddress = (InetAddress) bundle
                    .getSerializable(activityChat.KEY_CHANNEL_ADDR);
            int destPort = bundle.getInt(activityChat.KEY_CHANNEL_PORT);
            byte[] msgData = bundle.getByteArray(activityChat.KEY_CHANNEL_MESSAGE_DATA);
            ResultReceiver resultReceiver = bundle
                    .getParcelable(activityChat.KEY_CHANNEL_RESULT_RECEIVER);

            DatagramPacket sendPacket = new DatagramPacket(msgData, msgData.length,
                    destAddress, destPort);
            try {
                mSenderSocket.send(sendPacket);
                Log.i(TAG, "Sent packet: " + new String(msgData));
                resultReceiver.send(activityChat.RESULT_CODE_MESSAGE_SENT, null);
            } catch (IOException e) {
                Log.e(TAG, "DatagramSocket.send() failed: " + e);
            }
        }
    }
}
