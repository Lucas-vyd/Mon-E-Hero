package com.example.mone_hero;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonConnect = findViewById(R.id.buttonConnect);
        final EditText editText = findViewById(R.id.editText); // Référence à votre EditText

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = editText.getText().toString().trim(); // Obtenez et nettoyez le texte

                if (inputText.isEmpty()) { // Vérifiez si le texte est vide
                    Log.d("MainActivity", "texte vide");
                } else {
                    Log.d("MainActivity", inputText); // Affiche le texte dans la console
                }
            }
        });
    }
}
