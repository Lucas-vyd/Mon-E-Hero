package com.example.mone_hero_contact;

import static android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ListenerRegistration controlRequestListener;
    private AlertDialog controlShareDialog;
    private static final String TAG = "MainActivity";
    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private ImageReader mImageReader;
    private Handler mHandler;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;

    private static final int REQUEST_MEDIA_PROJECTION = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler(Looper.getMainLooper());

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

                // Initialiser le MediaProjectionManager
                mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                startMediaProjectionForegroundService();
                startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);

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
    private void startMediaProjectionForegroundService() {
        Intent serviceIntent = new Intent(this, MediaProjectionForegroundService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

//    @Override
//    protected void onDestroy() {
//        // Arrêter le service en premier plan lorsque l'activité est détruite
//        stopService(new Intent(this, MediaProjectionForegroundService.class));
//        super.onDestroy();
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == RESULT_OK) {
                // L'utilisateur a autorisé la projection des médias, initialiser la capture d'écran
                mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
                startScreenCapture();
            } else {
                // L'utilisateur a refusé la permission, afficher un message approprié
                Toast.makeText(this, "Permission de projection des médias refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("WrongConstant")
    private void startScreenCapture() {
        // Récupérer les dimensions de l'écran
        mScreenWidth = getWindowManager().getDefaultDisplay().getWidth();
        mScreenHeight = getWindowManager().getDefaultDisplay().getHeight();
        mScreenDensity = getResources().getDisplayMetrics().densityDpi;

        // Initialiser ImageReader pour capturer les images de l'écran
        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 2);
        mMediaProjection.createVirtualDisplay("ScreenCapture",
                mScreenWidth, mScreenHeight, mScreenDensity,
                VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, mHandler);

        // Démarrer la capture d'écran périodique
        startPeriodicScreenCapture();
    }

    private void startPeriodicScreenCapture() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                captureAndSendScreen();
            }
        }, 0, 1500); // Capture toutes les 1.5 secondes, ajustez selon vos besoins
    }

    private void captureAndSendScreen() {
        // Capture de l'écran
        Image image = mImageReader.acquireLatestImage();
        if (image != null) {
            Bitmap bitmap = imageToBitmap(image);
            if (bitmap != null) {
                // Envoyer la capture d'écran au serveur
                sendScreenData(bitmap);
            }
            image.close();
        }
    }

    private Bitmap imageToBitmap(Image image) {
        if (image == null) return null;
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * mScreenWidth;

        Bitmap bitmap = Bitmap.createBitmap(mScreenWidth + rowPadding / pixelStride, mScreenHeight, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);

        return bitmap;
    }

    private void sendScreenData(final Bitmap bitmap) {
        // Convertir l'image en chaîne Base64
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        final String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

        // Envoyer l'image encodée au serveur PHP
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String serverUrl = "http://407.projet3il.fr/index.php";
                    URL url = new URL(serverUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);

                    // Envoyer l'image encodée dans le corps de la requête
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(encodedImage.getBytes());
                    outputStream.flush();
                    outputStream.close();

                    connection.getResponseCode(); // Important pour déclencher la requête
                    connection.disconnect();
                } catch (Exception e) {
                    Log.e("SendImageDataTask", "Erreur lors de l'envoi de l'image au serveur", e);
                }
            }
        }).start();
    }
}
