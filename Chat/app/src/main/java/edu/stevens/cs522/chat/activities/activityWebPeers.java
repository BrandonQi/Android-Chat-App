package edu.stevens.cs522.chat.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.contracts.ChatContract;
import edu.stevens.cs522.chat.entities.WebPeer;
import edu.stevens.cs522.chat.managers.EntityManager;
import edu.stevens.cs522.chat.managers.ILoaderQueryListener;

public class activityWebPeers extends ListActivity {
    private static final String TAG = activityWebPeers.class.getSimpleName();

    public static final String WEB_PEER_DETAIL_KEY = TAG + ".web_peer_detail";

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        Intent intent = new Intent();
        intent.setClass(this, activityWebPeer.class);
        intent.putExtra(WEB_PEER_DETAIL_KEY, id);

        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_peers);

        final SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this,
                                                    android.R.layout.simple_list_item_2,null,
                                                    new String[]{WebPeer.COL_ID, WebPeer.COL_NAME},
                                                    new int[]{android.R.id.text1, android.R.id.text2},0);

        getListView().setAdapter(cursorAdapter);

        EntityManager entityManager = new EntityManager(this, null,ChatContract.LOADER_ID_ALL_WEB_PEERS);

        entityManager.executeLoaderQuery(ChatContract.CONTENT_URI_WEB_PEERS,
                new ILoaderQueryListener() {

                    public void handleResults(Cursor cursor) {
                        cursorAdapter.swapCursor(cursor);
                    }

                    public void closeResults() {
                        cursorAdapter.swapCursor(null);
                    }
                });
    }
}
