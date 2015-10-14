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
import edu.stevens.cs522.chat.entities.Message;
import edu.stevens.cs522.chat.entities.Peer;
import edu.stevens.cs522.chat.managers.EntityManager;
import edu.stevens.cs522.chat.managers.IAsyncQueryListener;
import edu.stevens.cs522.chat.managers.IEntityCreator;
import edu.stevens.cs522.chat.managers.ILoaderQueryListener;

public class activityDetail extends ListActivity {
    private static final String TAG = activityDetail.class.getSimpleName();

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_peer_detail);

        final long longExtra = getIntent().getLongExtra(activityPeers.PEER_DETAIL_KEY, -1);
        final SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,null, new String[]{Message.COL_MESSAGE},new int[]{android.R.id.text1},0);

        getListView().setAdapter(simpleCursorAdapter);

        final EntityManager<Peer> entityManager =new EntityManager<Peer>(this,new IEntityCreator<Peer>() {
                                                            public Peer create(Cursor c) {
                                                                return new Peer(c);
                                                            }
                                                        },ChatContract.LOADER_ID_SINGLE_MESSAGE);


        entityManager.executeAsyncQuery(
                ContentUris.withAppendedId(ChatContract.CONTENT_URI_PEERS, longExtra),
                null,
                new IAsyncQueryListener<Peer>() {
                    public void handleResults(List<Peer> results){
                        ((TextView) findViewById(R.id.tv_name)).setText(results.get(0).name);
                        ((TextView) findViewById(R.id.tv_ip)).setText(results.get(0).address.toString());
                        ((TextView) findViewById(R.id.tv_port)).setText(Integer.toString(results.get(0).port));
                        entityManager.executeLoaderQuery(
                                ContentUris.withAppendedId(ChatContract.CONTENT_URI_MESSAGES, longExtra),
                                new ILoaderQueryListener() {
                                    public void handleResults(Cursor cursor) {simpleCursorAdapter.swapCursor(cursor);}
                                    public void closeResults() {simpleCursorAdapter.swapCursor(null);}
                                });
                    }
                });
    }
}
