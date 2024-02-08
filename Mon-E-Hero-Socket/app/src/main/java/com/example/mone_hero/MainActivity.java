package com.example.mone_hero;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Vous pouvez appeler la tâche asynchrone lorsqu'un événement se produit, par exemple lorsqu'un bouton est cliqué
        findViewById(R.id.buttonCaptureGeste).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SendGestureTask().execute();
            }
        });
    }

    private static class SendGestureTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                // URL du serveur
                String serverUrl = "http://adresse_ip_du_serveur:12345"; // Remplacez "adresse_ip_du_serveur" et 12345 par les détails du serveur

                // Capture du geste (vous devez adapter cette partie en fonction de votre logique de capture)
                String gestureData = captureGeste();

                // Vérification si le geste a été capturé
                if (gestureData != null && !gestureData.isEmpty()) {
                    // Créer l'URL de connexion
                    URL url = new URL(serverUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    // Configurer la connexion pour une requête POST
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    // Créer les données à envoyer
                    String postData = "gestureData=" + gestureData;
                    byte[] postDataBytes = postData.getBytes(StandardCharsets.UTF_8);

                    // Écrire les données dans le flux de sortie
                    try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                        wr.write(postDataBytes);
                    }

                    // Récupérer la réponse du serveur (facultatif)
                    // Vous pouvez ajouter cette partie selon vos besoins

                    // Fermer la connexion
                    conn.disconnect();

                    // Retourner les données du geste à onPostExecute
                    return gestureData;
                } else {
                    System.out.println("Aucun geste capturé.");
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        // Méthode fictive de capture du geste, vous devez adapter cette méthode en fonction de votre logique de capture
        private static String captureGeste() {
            // ... Logique de capture du geste ...

            // Supposons que vous ayez une chaîne de données de geste
            return "DonneesDuGeste";
        }

        @Override
        protected void onPostExecute(String result) {
            // Cette méthode est appelée après l'exécution de doInBackground
            // Utilisez 'result' (données du geste) pour effectuer des opérations sur l'interface utilisateur si nécessaire
        }
    }
}
