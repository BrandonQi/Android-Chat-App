/*********************************************************************

 Client for sending chat messages to the server.

 Copyright (c) 2012 Stevens Institute of Technology

 **********************************************************************/
package edu.stevens.cs522.chat.activities;

import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.Date;
import java.util.UUID;

import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.contracts.ChatContract;
import edu.stevens.cs522.chat.entities.WebMessage;
import edu.stevens.cs522.chat.entities.WebPeer;
import edu.stevens.cs522.chat.managers.EntityManager;
import edu.stevens.cs522.chat.managers.IEntityCreator;
import edu.stevens.cs522.chat.managers.ILoaderQueryListener;
import edu.stevens.cs522.chat.services.RequestService;
import edu.stevens.cs522.chat.web.ServiceHelper;

public class activityChat extends ListActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    String string;
    ServiceHelper serviceHelper;
    String string1;
    long aLong;
    UUID uuid;
    boolean registered;
    PendingIntent pendingIntent;

    public static final int RESULT_CODE_MESSAGE_RECEIVED = 1000;
    public static final int RESULT_CODE_MESSAGE_SENT = 1001;
    public static final int RESULT_CODE_REGISTERED = 1002;

    final static String TAG = activityChat.class.getSimpleName();

    public static final String KEY_RESULT_RECEIVER = TAG + ".result_receiver";
    public static final String KEY_CHANNEL_ADDR = TAG + ".address";
    public static final String KEY_CHANNEL_PORT = TAG + ".port";
    public static final String KEY_CHANNEL_MESSAGE_DATA = TAG + ".message_data";
    public static final String KEY_CHANNEL_RESULT_RECEIVER = TAG + ".channel_result_receiver";

    static final String KEY_USERNAME = activityLogin.KEY_USERNAME;
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_chat);

        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(threadPolicy);

        Intent intent = getIntent();
        string = intent.getExtras().getString(KEY_USERNAME,getResources().getString(R.string.default_username));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String s = getResources().getString(R.string.preference_key_username);

        sharedPreferences.edit().putString(s, string).apply();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        final SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_2,null,new String[]{WebPeer.COL_NAME, WebMessage.COL_MESSAGE},new int[]{android.R.id.text1, android.R.id.text2},0);
        getListView().setAdapter(cursorAdapter);

        final EntityManager<WebMessage> entityManager = new EntityManager<WebMessage>(this, new IEntityCreator<WebMessage>() {
            public WebMessage create(Cursor c) {return new WebMessage(c);}
        }, ChatContract.LOADER_ID_ALL_WEB_MESSAGES);

        entityManager.executeLoaderQuery(ChatContract.CONTENT_URI_WEB_MESSAGES, new ILoaderQueryListener() {
            public void handleResults(Cursor cursor) {cursorAdapter.swapCursor(cursor);}
            public void closeResults() {cursorAdapter.swapCursor(null);}
        });

        string1 = getResources().getString(R.string.default_server_url);
        aLong = Long.parseLong(getResources().getString(R.string.default_client_id));
        uuid = UUID.fromString(getResources().getString(R.string.default_registration_id));
        serviceHelper = new ServiceHelper(this);
        registered = false;
    }
    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(broadcastReceiver);
    }
    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(getResources().getString(R.string.action_syncing));
        registerReceiver(broadcastReceiver, filter);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public void onClickSendWeb(View view) {
        if (registered) {
            EditText text = (EditText) findViewById(R.id.message_text);
            serviceHelper.postMessage(new Date(), aLong, text.getText().toString());
            text.setText("");
            pendingIntent = PendingIntent.getBroadcast(this, 0,new Intent(getResources().getString(R.string.action_syncing)), 0);
            AlarmManager alarmMan = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmMan.setRepeating(AlarmManager.RTC,new Date().getTime(),1 * 1000,pendingIntent);
        } else {
            Toast.makeText(this, "Register first.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.menu_chat, m);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        super.onOptionsItemSelected(menuItem);
        switch (menuItem.getItemId()) {
            case R.id.mi_register:{
                uuid = UUID.randomUUID();
                ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode == RESULT_CODE_REGISTERED) {
                            aLong = resultData.getLong(RequestService.KEY_CLIENT_ID);
                            if (aLong == 0) {
                                Toast.makeText(getBaseContext(), "Registration failed.", Toast.LENGTH_LONG).show();
                            } else {
                                Notification notification = new Notification.Builder(activityChat.this).setContentTitle("Register succeed.").setContentText("Register succeed: username=" + string + " clientId=" + aLong).setSmallIcon(R.drawable.ic_launcher).build();
                                NotificationManager NotificationMan = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                                NotificationMan.notify(0, notification);
                                SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(activityChat.this).edit();
                                String keyClientId = getResources().getString(R.string.preference_key_client_id);
                                editor.putString(keyClientId, Long.toString(aLong));
                                String keyRegistrationId = getResources().getString(R.string.preference_key_registration_id);
                                editor.putString(keyRegistrationId, uuid.toString());
                                editor.apply();
                                registered = true;
                            }
                        }
                    }
                };
                serviceHelper.register(string1, uuid, string, resultReceiver);
                return true;
            }
            case R.id.mi_view_peers: {
                startActivity(new Intent(this, activityWebPeers.class));
                return true;
            }
            case R.id.mi_settings: {
                Intent settingsIntent = new Intent(this, activityPreference.class);
                startActivity(settingsIntent);
                return true;
            }
        }
        return false;
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager.getActiveNetworkInfo() == null||!connectivityManager.getActiveNetworkInfo().isConnected()) {
                return;
            }
            ResultReceiver broadcastReceiver1 = new ResultReceiver(new Handler()) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    if (resultCode == 0) {
                        AlarmManager alarmMan = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                        alarmMan.cancel(pendingIntent);
                    }
                }
            };
            serviceHelper.synchronize(string1, uuid, aLong,broadcastReceiver1);
        }
    };

    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        registered = false;
        if (key.compareTo(getResources().getString(R.string.preference_key_username)) == 0)
        {
            string = preferences.getString(key,getResources().getString(R.string.default_username));
        }
        else if (key.compareTo(getResources().getString(R.string.preference_key_server_url))== 0)
        {
            string1 = preferences.getString(key,getResources().getString(R.string.default_server_url));
        }
        else if (key.compareTo(getResources().getString(R.string.preference_key_client_id))== 0)
        {
            aLong = Long.parseLong(preferences.getString(key,getResources().getString(R.string.default_client_id)));
        }
        else if (key.compareTo(getResources().getString(R.string.preference_key_registration_id))== 0)
        {
            uuid = UUID.fromString(preferences.getString(key,getResources().getString(R.string.default_registration_id)));
        }
    }
}
