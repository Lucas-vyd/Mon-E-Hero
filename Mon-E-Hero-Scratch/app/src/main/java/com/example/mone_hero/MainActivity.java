package com.example.mone_hero;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
        textView.setText("ID: "+uniqueIdString);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
/*
                String inputText = editText.getText().toString().trim(); // Obtenez et nettoyez le texte

                if (inputText.isEmpty()) { // Vérifiez si le texte est vide
                    Log.d("MainActivity", "texte vide");
                } else {
                    Log.d("MainActivity", inputText); // Affiche le texte dans la console
                }
 */
                new ConnectToServerTask().execute();
            }
        });
    }

    // AsyncTask pour effectuer la connexion en arrière-plan
    private class ConnectToServerTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            // URL du serveur PHP
            String serverUrl = "http://407.projet3il.fr/index.php";
            //String serverUrl = "home385849624.1and1-data.host";


            try {
                URL url = new URL(serverUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Configuration de la connexion
                connection.setRequestMethod("GET");

                // Lecture de la réponse du serveur
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Fermeture des ressources
                reader.close();
                connection.disconnect();

                // Retourner la réponse du serveur
                return response.toString();

            } catch (Exception e) {
                Log.e("ConnectToServerTask", "Erreur de connexion au serveur", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // Traitement de la réponse du serveur ici
            if (result != null) {
                // Afficher la réponse ou effectuer d'autres actions
                Log.d("ConnectToServerTask", "Réponse du serveur : " + result);
            } else {
                // Gérer l'erreur de connexion
                Log.e("ConnectToServerTask", "Erreur de connexion au serveur");
            }
        }
    }
}