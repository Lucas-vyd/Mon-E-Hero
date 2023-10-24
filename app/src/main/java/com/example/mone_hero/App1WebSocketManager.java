package com.example.mone_hero;


import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class App1WebSocketManager {

    private WebSocket webSocket;

    public void connectToServer() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws:home385849624.1and1-data.host").build();

        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                // La connexion WebSocket est établie
                System.out.print("Connexion réussie");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                // Gérer les messages reçus
            }
        };

        webSocket = client.newWebSocket(request, listener);
    }

    public void sendMessage(String message) {

        webSocket.send(message);
        Log.d("WebSocket", "Message envoyé avec succès : " + message);

    }
}
