package edu.stevens.cs522.chat.contracts;

import android.net.Uri;

import edu.stevens.cs522.chat.databases.MessageDbAdapter;

public class ChatContract {
    public static final int LOADER_ID_ALL_PEERS = 1000;
    public static final int LOADER_ID_ALL_MESSAGES = 2000;
    public static final int LOADER_ID_SINGLE_MESSAGE = 3000;
    public static final int LOADER_ID_ALL_WEB_PEERS = 4000;
    public static final int LOADER_ID_ALL_WEB_MESSAGES = 5000;
    public static final int LOADER_ID_SINGLE_WEB_MESSAGE = 6000;

    public static final String AUTHORITY = "edu.stevens.cs522.chat";
    public static final String PATH_PEERS = MessageDbAdapter.TABLE_PEERS;
    public static final String CONTENT_TYPE_ALL_ITEMS_PEERS = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + PATH_PEERS;
    public static final String CONTENT_TYPE_SINGLE_ITEM_PEERS = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + PATH_PEERS;
    public static final String PATH_MESSAGES = MessageDbAdapter.TABLE_MESSAGES;
    public static final String CONTENT_TYPE_ALL_ITEMS_MESSAGES = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + PATH_MESSAGES;
    public static final String CONTENT_TYPE_SINGLE_ITEM_MESSAGES = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + PATH_MESSAGES;
    public static final String PATH_WEB_PEERS = MessageDbAdapter.TABLE_WEB_PEERS;
    public static final String CONTENT_TYPE_ALL_ITEMS_WEB_PEERS = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + PATH_WEB_PEERS;
    public static final String CONTENT_TYPE_SINGLE_ITEM_WEB_PEERS = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + PATH_WEB_PEERS;
    public static final String PATH_WEB_MESSAGES = MessageDbAdapter.TABLE_WEB_MESSAGES;
    public static final String CONTENT_TYPE_ALL_ITEMS_WEB_MESSAGES = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + PATH_WEB_MESSAGES;
    public static final String CONTENT_TYPE_SINGLE_ITEM_WEB_MESSAGES = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + PATH_WEB_MESSAGES;
    public static final Uri CONTENT_URI_PEERS = new Uri.Builder().scheme("content").authority(AUTHORITY).path(PATH_PEERS).build();
    public static final Uri CONTENT_URI_MESSAGES = new Uri.Builder().scheme("content").authority(AUTHORITY).path(PATH_MESSAGES).build();
    public static final Uri CONTENT_URI_WEB_PEERS = new Uri.Builder().scheme("content").authority(AUTHORITY).path(PATH_WEB_PEERS).build();
    public static final Uri CONTENT_URI_WEB_MESSAGES = new Uri.Builder().scheme("content").authority(AUTHORITY).path(PATH_WEB_MESSAGES).build();

}
