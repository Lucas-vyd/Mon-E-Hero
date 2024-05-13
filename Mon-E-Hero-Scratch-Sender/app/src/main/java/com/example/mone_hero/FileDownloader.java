package com.example.mone_hero;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
public class FileDownloader extends AsyncTask<String, Void, String> {

    private View mView;

    //Timer timer;
    public FileDownloader(View view) {
        this.mView = view;
    }
    @Override
    protected String doInBackground(String... params) {
        String fileUrl = params[0];
        StringBuilder fileContent = new StringBuilder();

        // Initialiser le Timer
        //timer = new Timer();

        //timer.schedule(new TimerTask() {
        //@Override
        //public void run() {
        // Appeler la méthode pour récupérer les coordonnées du clic depuis le serveur
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Lecture des données du fichier
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line);
                fileContent.append("\n");
            }
            reader.close();

            // Fermeture de la connexion
            connection.disconnect();
        } catch (IOException e) {
            Log.e("FileDownloader", "Erreur lors du téléchargement du fichier", e);
        }
        //}
        //}, 0, 1000);

        return fileContent.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        // Traitez les données du fichier ici, par exemple, affichez-les dans un TextView
        //textView.setText(result);
        Log.d("simulateClickOnScreen",""+result);
        if (result != null && !result.isEmpty()) {
            // Diviser les données reçues pour obtenir les coordonnées du clic
            String[] coordinates = result.split(" ");
            if (coordinates.length == 2) {
                // Extrait uniquement les parties numériques des coordonnées
                String xString = coordinates[0].replaceAll("[^0-9.]", "");
                String yString = coordinates[1].replaceAll("[^0-9.]", "");
                // Convertir les parties numériques en float
                float x = Float.parseFloat(xString);
                float y = Float.parseFloat(yString);
                Log.d("sendClickCoordinatesToEmitter","x=" + x + " y=" + y);


                // Créer un MotionEvent pour le toucher (ACTION_DOWN)
                long downTime = SystemClock.uptimeMillis();
                long eventTime = SystemClock.uptimeMillis();
                int action = MotionEvent.ACTION_DOWN;
                MotionEvent motionEventDown = MotionEvent.obtain(downTime, eventTime, action, x, y, 0);

// Envoyer le MotionEvent de toucher à la vue appropriée
                mView.dispatchTouchEvent(motionEventDown);

// Attendre un court instant pour simuler le clic complet
                try {
                    Thread.sleep(100); // ajustez la durée si nécessaire
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

// Créer un MotionEvent pour le relâchement (ACTION_UP)
                eventTime = SystemClock.uptimeMillis();
                action = MotionEvent.ACTION_UP;
                MotionEvent motionEventUp = MotionEvent.obtain(downTime, eventTime, action, x, y, 0);

// Envoyer le MotionEvent de relâchement à la vue appropriée
                mView.dispatchTouchEvent(motionEventUp);
            }
        }
    }
}
