package com.example.mone_hero_2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private ImageView imageViewReceived;
    private String imageUrl;


    TextView textViewReceivedData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageViewReceived = findViewById(R.id.imageViewReceived);
        imageUrl = "http://407.projet3il.fr/index.php";

        Button buttonConnect = findViewById(R.id.buttonConnect);
        final EditText editText = findViewById(R.id.editText); // Référence à votre EditText

        TextView textView = findViewById(R.id.textViewId); // Pour afficher l'id de l'appli
        textViewReceivedData = findViewById(R.id.textViewId1);

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
        //textViewReceivedData.setText("Id_sender");

/*
        Intent intent = getIntent();
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals("com.example.action.SOMETHING")) {
                // Gérer l'action "com.example.action.SOMETHING"
                String data = intent.getStringExtra("data");

                // Maintenant, vous pouvez afficher les données dans votre interface utilisateur
                TextView textView1 = findViewById(R.id.textViewId1); // Remplacez "textView" par l'ID de votre TextView
                textView.setText(data);
            }
        } else {
            // Gérer le cas où l'Intent est null ou son action est null
            System.out.print("Erreur");
        }
*/
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
                //String message = editText.getText().toString().trim(); // Obtenez et nettoyez le texte
                //new GetScreenDataTask().execute();
                new DownloadImageTask().execute();
            }
        });
    }
    private class DownloadImageTask extends AsyncTask<Void, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Void... params) {
            //String imageUrl = params[0];
            // Remplacez l'URL par l'adresse réelle de votre serveur PHP
            String serverUrl = "http://407.projet3il.fr/index.php";
            String imageUrl = "http://407.projet3il.fr/capture.png";


            try {
                // Télécharger l'image depuis l'URL
                InputStream in = new java.net.URL(imageUrl).openStream();

                Bitmap bitmap = BitmapFactory.decodeStream(in);
                //in.close();
                // Convertir l'InputStream en Bitmap
                Log.d("DownloadImageTask", "Téléchargement de l'image");
                return bitmap;
            } catch (Exception e) {
                Log.e("DownloadImageTask", "Erreur lors du téléchargement de l'image", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // Afficher l'image dans l'ImageView
            if (result != null) {
                imageViewReceived.setImageBitmap(result);
            } else {
                // Gérer l'erreur du téléchargement de l'image
                Log.e("DownloadImageTask", "Erreur lors du téléchargement de l'image 2");
            }
        }
    }
}