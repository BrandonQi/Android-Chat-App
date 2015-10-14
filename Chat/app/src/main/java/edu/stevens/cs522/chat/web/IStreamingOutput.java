package edu.stevens.cs522.chat.web;

import java.io.IOException;
import java.net.HttpURLConnection;

public interface IStreamingOutput {
    public boolean outputRequestEntity(HttpURLConnection connection) throws IOException;
}
