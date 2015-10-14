package edu.stevens.cs522.chat.services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.ResultReceiver;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.activities.activityChat;

public class ChatReceiverService extends Service {
    public final static String TAG = ChatReceiverService.class.getSimpleName();
    public final static String KEY_RESULT_ADDRESS = TAG + ".address";
    public final static String KEY_RESULT_PORT = TAG + ".port";
    public final static String KEY_RESULT_NAME = TAG + ".name";
    public final static String KEY_RESULT_MESSAGE = TAG + ".message";

    private DatagramSocket mReceiverSocket;
    private ResultReceiver mResultReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()...");

        if (intent != null) {
            mResultReceiver = intent
                    .getParcelableExtra(activityChat.KEY_RESULT_RECEIVER);
        }

        /* open the receiver socket */
        int port = Integer.parseInt(getResources()
                .getString(R.string.receiver_port_default));
        try {
            mReceiverSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            Log.e(TAG, "Cannot open socket: " + e);
            return START_STICKY;
        }

        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                Log.d(TAG, "Looper.prepare() ok");
                receiveMessage();
            }
        }).start();

        Log.d(TAG, "onStartCommand()... OK");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()...");

        if (mReceiverSocket != null) {
            mReceiverSocket.close();
            mReceiverSocket = null;
        }
    }

    private void receiveMessage() {
        final int DATA_LEN = 1024;
        DatagramPacket datagramPacket = new DatagramPacket(new byte[DATA_LEN], DATA_LEN);

        try {
            while (true) {
                Log.d(TAG, "Receiving...");
                mReceiverSocket.receive(datagramPacket);
                Log.d(TAG, "Received a packet");

                /* get peer address */
                InetAddress sourceIPAddress = datagramPacket.getAddress();
                int sourcePort = datagramPacket.getPort();
                Log.i(TAG, "Source IP Address (port): " + sourceIPAddress + " (" + sourcePort + ")");

                /* get name & message */
                String msg = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                String SEPARATOR = ": ";
                int separatorIndex = msg.indexOf(SEPARATOR);
                String name = msg.substring(0, separatorIndex);
                String message = msg.substring(separatorIndex + SEPARATOR.length());
                Log.d(TAG, "name=[" + name + "] message=[" + message + "]");

                /* inform the activity about the received message */
                Bundle resultData = new Bundle();
                resultData.putSerializable(KEY_RESULT_ADDRESS, sourceIPAddress);
                resultData.putInt(KEY_RESULT_PORT, sourcePort);
                resultData.putString(KEY_RESULT_NAME, name);
                resultData.putString(KEY_RESULT_MESSAGE, message);
                mResultReceiver.send(activityChat.RESULT_CODE_MESSAGE_RECEIVED,
                        resultData);

                /* broadcast the received message */
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(getResources()
                        .getString(R.string.action_message_received));
                getBaseContext().sendBroadcast(broadcastIntent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Problems receiving packet: " + e);
        }
    }
}
