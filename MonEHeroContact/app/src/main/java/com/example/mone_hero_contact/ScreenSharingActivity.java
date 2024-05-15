package com.example.mone_hero_contact;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;



public class ScreenSharingActivity extends AppCompatActivity {

    private TextView sharingText;
    private Button stopSharingButton;
    private DocumentReference docRef;
    private ListenerRegistration docListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screensharing);

        sharingText = findViewById(R.id.sharingText);
        stopSharingButton = findViewById(R.id.stopSharingButton);

        sharingText.setText("Partage d'écran en cours...");

        // Récupérez l'ID du document passé via l'intention
        String documentId = getIntent().getStringExtra("DOCUMENT_ID");
        docRef = FirebaseFirestore.getInstance().collection("controlShare").document(documentId);

        stopSharingButton.setOnClickListener(v -> {
            docRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ScreenSharingActivity.this, "Partage d'écran arrêté", Toast.LENGTH_SHORT).show();
                        finish();

                        // Arrêter le service en premier plan lorsque l'activité est détruite
                        stopService(new Intent(this, MediaProjectionForegroundService.class));

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ScreenSharingActivity.this, "Erreur lors de l'arrêt du partage", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        docListener = docRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                Toast.makeText(ScreenSharingActivity.this, "Erreur lors de l'écoute du document", Toast.LENGTH_SHORT).show();
                return;
            }

            if (documentSnapshot != null && !documentSnapshot.exists()) {
                Toast.makeText(ScreenSharingActivity.this, "Le partage a été arrété.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (docListener != null) {
            docListener.remove();
        }
    }
}
