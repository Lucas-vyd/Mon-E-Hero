package com.example.mone_hero;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;



public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonConnect = findViewById(R.id.buttonConnect);
        final EditText editText = findViewById(R.id.editText); // Référence à votre EditText

        TextView textView = findViewById(R.id.textViewId); // Pour afficher l'id de l'appli

        // Générer un identifiant unique (UUID)
        UUID uniqueId = UUID.randomUUID();
        String uniqueIdString = uniqueId.toString();

        // Stocker l'identifiant dans les préférences partagées
        SharedPreferences preferences = getSharedPreferences("MonAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("app_id", uniqueIdString);
        editor.apply();

        // Afficher l'ID généré dans le TextView
        textView.setText("ID: " + uniqueIdString);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Capture de l'écran (vous devez implémenter cette méthode selon vos besoins)
                Bitmap screenshot = captureScreen();

                // Envoyer la capture d'écran au serveur
                new SendScreenDataTask().execute(screenshot);
            }
        });
    }
    private Bitmap captureScreen() {
        // Obtenir la référence à la vue racine de l'activité
        View rootView = getWindow().getDecorView().getRootView();

        // Créer un Bitmap avec les dimensions de la vue racine
        Bitmap screenshot = Bitmap.createBitmap(rootView.getWidth(), rootView.getHeight(), Bitmap.Config.ARGB_8888);

        // Créer un canvas associé au Bitmap
        Canvas canvas = new Canvas(screenshot);

        // Dessiner la vue racine sur le canvas
        rootView.draw(canvas);

        return screenshot;
    }

    private class SendScreenDataTask extends AsyncTask<Bitmap, Void, Void> {
        @SuppressLint("WrongThread")
        @Override
        protected Void doInBackground(Bitmap... bitmaps) {
            // Convertir l'image en chaîne Base64
            Bitmap screenshot = bitmaps[0];
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

            // Envoyer l'image encodée au serveur PHP
            new SendImageDataTask().execute(encodedImage);

            return null;
        }
    }

    private class SendImageDataTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            // Envoyer l'image encodée au serveur PHP
            String encodedImage = params[0];
            // Assurez-vous d'ajuster l'URL du serveur PHP
            String serverUrl = "http://407.projet3il.fr/index.php";
            try {
                URL url = new URL(serverUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Envoyer l'image encodée dans le corps de la requête
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(encodedImage.getBytes());
                outputStream.flush();
                outputStream.close();

                connection.getResponseCode(); // Important pour déclencher la requête
                connection.disconnect();

            } catch (Exception e) {
                Log.e("SendImageDataTask", "Erreur lors de l'envoi de l'image au serveur", e);
            }
            return null;
        }
    }
}