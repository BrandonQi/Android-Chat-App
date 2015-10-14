package edu.stevens.cs522.chat.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import edu.stevens.cs522.chat.activities.activityChat;
import edu.stevens.cs522.chat.entities.WebMessage;
import edu.stevens.cs522.chat.web.Register;
import edu.stevens.cs522.chat.web.RequestProcessor;
import edu.stevens.cs522.chat.web.ServiceHelper;
import edu.stevens.cs522.chat.web.Synchronize;

public class RequestService extends IntentService {
    public static final String KEY_CLIENT_ID = RequestService.class.getCanonicalName()
            + ".client_id";

    private static final String TAG = RequestService.class.getSimpleName();

    private RequestProcessor mProcessor = new RequestProcessor(this);

    public RequestService() {
        super(RequestService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int requestType = intent.getIntExtra(ServiceHelper.KEY_REQUEST, 0);
        Log.d(TAG, "onHandleIntent() requestType=" + requestType);
        switch (requestType) {
            case ServiceHelper.REQUEST_REGISTER:
                Register register = intent.getParcelableExtra(ServiceHelper.KEY_REGISTER);
                ResultReceiver resultReceiver = intent.getParcelableExtra(ServiceHelper.KEY_RESULT_RECEIVER);
                long clientId = mProcessor.perform(register);

                Bundle resultData = new Bundle();
                resultData.putLong(KEY_CLIENT_ID, clientId);
                resultReceiver.send(activityChat.RESULT_CODE_REGISTERED, resultData);
                break;

            case ServiceHelper.REQUEST_MESSAGE:
                WebMessage webMessage = intent.getParcelableExtra(ServiceHelper.KEY_MESSAGE);
                mProcessor.perform(webMessage);
                break;

            case ServiceHelper.REQUEST_SYNCHRONIZE:
                Synchronize request = intent.getParcelableExtra(ServiceHelper.KEY_SYNCHRONIZE);
                ResultReceiver syncingReceiver = intent.getParcelableExtra(ServiceHelper.KEY_RESULT_RECEIVER);
                mProcessor.perform(request, syncingReceiver);
                break;

            default:
                Log.w(TAG, "handle unknown request.");
                break;
        }
    }

}
