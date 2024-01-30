package com.example.mone_hero_contact;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ListenerRegistration controlRequestListener;
    private AlertDialog controlShareDialog;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        Button logoutButton = findViewById(R.id.logoutButton);

        logoutButton.setOnClickListener(view -> {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        ImageView iconContacts = findViewById(R.id.iconContacts);
        ImageView iconSearch = findViewById(R.id.iconSearch);
        ImageView iconRequests = findViewById(R.id.iconRequests);

        iconContacts.setOnClickListener(v -> loadFragment(new ContactsFragment()));
        iconSearch.setOnClickListener(v -> loadFragment(new SearchFragment()));
        iconRequests.setOnClickListener(v -> loadFragment(new RequestFragment()));

        loadFragment(new ContactsFragment());

        listenForControlShare();
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void listenForControlShare() {
        if (mAuth.getCurrentUser() != null) {
            String currentUserId = mAuth.getCurrentUser().getUid();

            controlRequestListener = FirebaseFirestore.getInstance().collection("controlShare")
                    .whereEqualTo("toUserId", currentUserId)
                    .whereEqualTo("status", "pending")
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            return;
                        }

                        if (snapshots != null && !snapshots.isEmpty()) {
                            String requestingUserId = (String) snapshots.getDocuments().get(0).get("fromUserId");
                            String documentId = snapshots.getDocuments().get(0).getId();
                            showControlShareDialog(requestingUserId, documentId);
                        }
                    });
        }
    }

    private void showControlShareDialog(String requestingUserId, String documentId) {
        getUsernameFromUserId(requestingUserId, new OnUsernameFetchedListener() {
            @Override
            public void onFetched(String username) {
                controlShareDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Demande de prise de contrôle")
                        .setMessage("L'utilisateur " + username + " souhaite prendre le contrôle de votre écran. Acceptez-vous ?")
                        .setPositiveButton("Accepter", (dialog, which) -> {
                            FirebaseFirestore.getInstance().collection("controlShare").document(documentId)
                                    .update("status", "accepted")
                                    .addOnSuccessListener(aVoid -> {
                                        Intent intent = new Intent(MainActivity.this, ScreenSharingActivity.class);
                                        intent.putExtra("DOCUMENT_ID", documentId);
                                        startActivity(intent);
                                    })
                                    .addOnFailureListener(e -> {
                                        // Gérer l'échec de la mise à jour
                                    });
                        })
                        .setNegativeButton("Refuser", (dialog, which) -> {
                            FirebaseFirestore.getInstance().collection("controlShare").document(documentId)
                                    .update("status", "refused")
                                    .addOnSuccessListener(aVoid -> {
                                        FirebaseFirestore.getInstance().collection("controlShare").document(documentId)
                                                .delete();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Gérer l'échec de la mise à jour
                                    });
                        })
                        .show();

                FirebaseFirestore.getInstance().collection("controlShare").document(documentId)
                        .addSnapshotListener((snapshot, e) -> {
                            if (e != null || snapshot == null || !snapshot.exists() || !snapshot.getString("status").equals("pending")) {
                                if (controlShareDialog != null && controlShareDialog.isShowing()) {
                                    controlShareDialog.dismiss();
                                }
                            }
                        });
            }

            @Override
            public void onError() {
                // Gérer l'erreur lors de la récupération du nom d'utilisateur
            }
        });
    }

    private void getUsernameFromUserId(String userId, OnUsernameFetchedListener listener) {
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        listener.onFetched(username);
                    } else {
                        listener.onError();
                    }
                })
                .addOnFailureListener(e -> listener.onError());
    }

    interface OnUsernameFetchedListener {
        void onFetched(String username);
        void onError();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Détachez le listener lorsque l'activité est détruite pour éviter les fuites de mémoire.
        if (controlRequestListener != null) {
            controlRequestListener.remove();
        }
    }
}
