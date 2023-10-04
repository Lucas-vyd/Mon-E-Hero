package com.example.mone_hero;

import androidx.appcompat.app.AppCompatActivity; // Utilisez ceci pour AppCompatActivity
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

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cette méthode sera appelée lorsque le bouton "Se connecter" sera cliqué.
                Log.d("MainActivity", "Click réussi"); // Affiche "click réussi" dans la console.
            }
        });
    }
}

