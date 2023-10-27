package com.example.mone_hero_contact;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class WaitingActivity extends AppCompatActivity {

    private TextView waitingText;
    private Button stopSharingButton;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference controlRequestRef;
    private ListenerRegistration listenerRegistration;  // Ajouté pour gérer le détachement du listener

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        waitingText = findViewById(R.id.waitingTextView);
        stopSharingButton = findViewById(R.id.stopSharingButton);

        String targetUsername = getIntent().getStringExtra("TARGET_USERNAME");
        String documentId = getIntent().getStringExtra("DOCUMENT_ID");

        if (documentId != null && !documentId.isEmpty()) {
            controlRequestRef = db.collection("controlShare").document(documentId);
        }

        waitingText.setText("En attente de " + targetUsername);

        stopSharingButton.setOnClickListener(v -> {
            if (controlRequestRef != null) {
                controlRequestRef.delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(WaitingActivity.this, "Partage arrêté", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(WaitingActivity.this, "Erreur lors de la suppression.", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(WaitingActivity.this, "Référence de la demande non trouvée.", Toast.LENGTH_SHORT).show();
            }
        });

        listenForControlResponse();
    }

    private void listenForControlResponse() {
        if(controlRequestRef != null) {
            listenerRegistration = controlRequestRef.addSnapshotListener((documentSnapshot, e) -> {
                if (e != null) {
                    Toast.makeText(this, "Erreur lors de l'écoute de la réponse.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(documentSnapshot != null && documentSnapshot.exists()) {
                    String status = documentSnapshot.getString("status");
                    if(status != null) {
                        switch (status) {
                            case "accepted":
                                Intent intent = new Intent(WaitingActivity.this, ScreenSharingActivity.class);
                                intent.putExtra("DOCUMENT_ID", controlRequestRef.getId());
                                startActivity(intent);
                                finish();  // Cette ligne termine l'activité WaitingActivity
                                break;

                            case "refused":
                                Toast.makeText(this, "La demande a été refusée.", Toast.LENGTH_SHORT).show();
                                finish();
                                break;
                        }
                    } else {
                        Toast.makeText(this, "Statut non trouvé dans le document.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Le partage a été arreté.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            Toast.makeText(this, "Référence de la demande non trouvée.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(listenerRegistration != null) {
            listenerRegistration.remove();  // Détache le listener quand l'activité n'est plus au premier plan
        }
    }
}

