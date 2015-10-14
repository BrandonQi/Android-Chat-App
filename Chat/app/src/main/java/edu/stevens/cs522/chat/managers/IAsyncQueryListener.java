package edu.stevens.cs522.chat.managers;

import java.util.List;

public interface IAsyncQueryListener<T> {
    public void handleResults(List<T> results);
}
