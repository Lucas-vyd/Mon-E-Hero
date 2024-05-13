package com.example.mone_hero_2;

import android.os.AsyncTask;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendClickCoordinatesToServerTask extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... params) {
        String clickInfo = params[0];
        String serverUrl = "http://407.projet3il.fr/action_click.php";

        try {
            // Créer la connexion HTTP
            URL url = new URL(serverUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            // Envoyer les informations du clic au serveur PHP
            OutputStream outputStream = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(clickInfo);
            writer.flush();
            writer.close();
            outputStream.close();

            // Lire la réponse du serveur si nécessaire
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Traitement de la réponse du serveur si nécessaire
            } else {
                // Gérer les erreurs de connexion au serveur
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Autres méthodes de l'application réceptrice...

}
