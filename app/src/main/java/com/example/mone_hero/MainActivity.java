package com.example.mone_hero;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Context;
import android.content.SharedPreferences;
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

        App1WebSocketManager webSocketManager = new App1WebSocketManager();

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
                webSocketManager.connectToServer();
                //String message = editText.getText().toString().trim(); // Obtenez et nettoyez le texte
                String message = "Serveur PHP Mon E-Hero";
                if (!message.isEmpty()) {
                    webSocketManager.sendMessage(message);
                }
            }
        });
    }
}
