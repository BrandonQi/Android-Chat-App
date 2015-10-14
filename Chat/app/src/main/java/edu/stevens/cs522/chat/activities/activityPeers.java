package edu.stevens.cs522.chat.activities;

import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.contracts.ChatContract;
import edu.stevens.cs522.chat.entities.Peer;
import edu.stevens.cs522.chat.managers.EntityManager;
import edu.stevens.cs522.chat.managers.ILoaderQueryListener;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


public class activityPeers extends ListActivity {
    private static final String TAG = activityPeers.class.getSimpleName();

    public static final String PEER_DETAIL_KEY = TAG + ".peer_detail";

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        Intent intent = new Intent();
        intent.setClass(this, activityDetail.class);
        intent.putExtra(PEER_DETAIL_KEY, id);

        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_peers);

        final SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,null,new String[]{Peer.COL_NAME},new int[]{android.R.id.text1},0);

        getListView().setAdapter(simpleCursorAdapter);

        EntityManager<Peer> peerEntityManager = new EntityManager<Peer>(this, null,ChatContract.LOADER_ID_ALL_PEERS);

        peerEntityManager.executeLoaderQuery(ChatContract.CONTENT_URI_PEERS,
                new ILoaderQueryListener() {
                    public void handleResults(Cursor cursor) {simpleCursorAdapter.swapCursor(cursor);}
                    public void closeResults() {simpleCursorAdapter.swapCursor(null);}
                });
    }
}
