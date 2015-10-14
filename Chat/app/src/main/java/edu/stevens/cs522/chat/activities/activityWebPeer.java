package edu.stevens.cs522.chat.activities;

import android.app.ListActivity;
import android.content.ContentUris;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.List;

import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.contracts.ChatContract;
import edu.stevens.cs522.chat.entities.WebMessage;
import edu.stevens.cs522.chat.entities.WebPeer;
import edu.stevens.cs522.chat.managers.EntityManager;
import edu.stevens.cs522.chat.managers.IAsyncQueryListener;
import edu.stevens.cs522.chat.managers.IEntityCreator;
import edu.stevens.cs522.chat.managers.ILoaderQueryListener;

public class activityWebPeer extends ListActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_peer_detail);
        final long longExtra = getIntent().getLongExtra(activityWebPeers.WEB_PEER_DETAIL_KEY, -1);
        final SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(this,
                                                        android.R.layout.simple_list_item_1,
                                                        null, new String[]{WebMessage.COL_MESSAGE},
                                                        new int[]{android.R.id.text1},
                                                        0);

        getListView().setAdapter(simpleCursorAdapter);

        final EntityManager<WebPeer> webPeerEntityManager = new EntityManager<WebPeer>(this,
                                                                new IEntityCreator<WebPeer>() {
                                                                    public WebPeer create(Cursor cursor) {
                                                                        return new WebPeer(cursor);
                                                                    }
                                                                }, ChatContract.LOADER_ID_SINGLE_WEB_MESSAGE);

        webPeerEntityManager.executeAsyncQuery(
                ContentUris.withAppendedId(ChatContract.CONTENT_URI_WEB_PEERS, longExtra),
                null,
                new IAsyncQueryListener<WebPeer>() {
                    public void handleResults(List<WebPeer> results) {
                        ((TextView) findViewById(R.id.tv_sender_id)).setText(Long.toString(results.get(0).id));
                        ((TextView) findViewById(R.id.tv_sender_name)).setText(results.get(0).name);
                        webPeerEntityManager.executeLoaderQuery(ContentUris.withAppendedId(ChatContract.CONTENT_URI_WEB_MESSAGES, longExtra), new ILoaderQueryListener() {
                            public void handleResults(Cursor cursor) {
                                simpleCursorAdapter.swapCursor(cursor);
                            }
                            public void closeResults() {
                                simpleCursorAdapter.swapCursor(null);
                            }
                        });
                    }
                });
    }

}
