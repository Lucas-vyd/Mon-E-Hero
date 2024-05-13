package com.example.mone_hero_contact;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class ScreenReceivedActivity extends AppCompatActivity {

    private ImageView imageViewReceived;
    private String imageUrl;
    private Timer timer;
    private Handler handler;
    private DocumentReference docRef;


    TextView textViewReceivedData;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screenreceived);

//        Intent intent = new Intent(this, ScreenSharingActivity.class);
//        intent.putExtra("imageResId", R.id.imageViewReceived);
//        startActivity(intent);

        imageViewReceived = findViewById(R.id.imageViewReceived);
        imageUrl = "http://407.projet3il.fr/index.php";

        timer = new Timer();
        handler = new Handler(Looper.getMainLooper());

        String documentId = getIntent().getStringExtra("DOCUMENT_ID");
        docRef = FirebaseFirestore.getInstance().collection("controlShare").document(documentId);

        startImageDownload();

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

    // Méthode pour démarrer le téléchargement d'image périodique
    private void startImageDownload() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Téléchargement de l'image depuis le serveur
                downloadAndDisplayImage();
            }
        }, 0, 1500); // Télécharge toutes les 1 seconde, ajustez selon vos besoins
    }

    // Méthode pour arrêter le téléchargement d'image périodique
    private void stopImageDownload() {
        timer.cancel();
    }

    // Méthode pour télécharger et afficher l'image depuis le serveur
    private void downloadAndDisplayImage() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                // Téléchargement de l'image depuis le serveur
                new DownloadImageTask().execute();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Arrêtez le téléchargement d'image périodique lorsque l'activité est détruite
        stopImageDownload();
    }

}
