package com.example.mone_hero_contact;

import static android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR;

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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public class WaitingActivity extends AppCompatActivity {

    private TextView waitingText;
    private Button stopSharingButton;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference controlRequestRef;
    private ListenerRegistration listenerRegistration;  // Ajouté pour gérer le détachement du listener

//    private MediaProjectionManager mProjectionManager;
//    private MediaProjection mMediaProjection;
//    private ImageReader mImageReader;
//    private Handler mHandler;
//    private int mScreenWidth;
//    private int mScreenHeight;
//    private int mScreenDensity;
//
//    private static final int REQUEST_MEDIA_PROJECTION = 1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        waitingText = findViewById(R.id.waitingTextView);
        stopSharingButton = findViewById(R.id.stopSharingButton);

//        // Initialiser le MediaProjectionManager
//        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
//
//        startMediaProjectionForegroundService();
//
//        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
//
//
//        mHandler = new Handler(Looper.getMainLooper());

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
                                Intent intent = new Intent(WaitingActivity.this, ScreenReceivedActivity.class);
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

//    private void startMediaProjectionForegroundService() {
//        Intent serviceIntent = new Intent(this, MediaProjectionForegroundService.class);
//        ContextCompat.startForegroundService(this, serviceIntent);
//    }
//
//    @Override
//    protected void onDestroy() {
//        // Arrêter le service en premier plan lorsque l'activité est détruite
//        stopService(new Intent(this, MediaProjectionForegroundService.class));
//        super.onDestroy();
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_MEDIA_PROJECTION) {
//            if (resultCode == RESULT_OK) {
//                // L'utilisateur a autorisé la projection des médias, initialiser la capture d'écran
//                mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
//                startScreenCapture();
//            } else {
//                // L'utilisateur a refusé la permission, afficher un message approprié
//                Toast.makeText(this, "Permission de projection des médias refusée", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    @SuppressLint("WrongConstant")
//    private void startScreenCapture() {
//        // Récupérer les dimensions de l'écran
//        mScreenWidth = getWindowManager().getDefaultDisplay().getWidth();
//        mScreenHeight = getWindowManager().getDefaultDisplay().getHeight();
//        mScreenDensity = getResources().getDisplayMetrics().densityDpi;
//
//        // Initialiser ImageReader pour capturer les images de l'écran
//        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 2);
//        mMediaProjection.createVirtualDisplay("ScreenCapture",
//                mScreenWidth, mScreenHeight, mScreenDensity,
//                VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
//                mImageReader.getSurface(), null, mHandler);
//
//        // Démarrer la capture d'écran périodique
//        startPeriodicScreenCapture();
//    }
//
//    private void startPeriodicScreenCapture() {
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                captureAndSendScreen();
//            }
//        }, 0, 1500); // Capture toutes les 1.5 secondes, ajustez selon vos besoins
//    }
//
//    private void captureAndSendScreen() {
//        // Capture de l'écran
//        Image image = mImageReader.acquireLatestImage();
//        if (image != null) {
//            Bitmap bitmap = imageToBitmap(image);
//            if (bitmap != null) {
//                // Envoyer la capture d'écran au serveur
//                sendScreenData(bitmap);
//            }
//            image.close();
//        }
//    }
//
//    private Bitmap imageToBitmap(Image image) {
//        if (image == null) return null;
//        Image.Plane[] planes = image.getPlanes();
//        ByteBuffer buffer = planes[0].getBuffer();
//        int pixelStride = planes[0].getPixelStride();
//        int rowStride = planes[0].getRowStride();
//        int rowPadding = rowStride - pixelStride * mScreenWidth;
//
//        Bitmap bitmap = Bitmap.createBitmap(mScreenWidth + rowPadding / pixelStride, mScreenHeight, Bitmap.Config.ARGB_8888);
//        bitmap.copyPixelsFromBuffer(buffer);
//
//        return bitmap;
//    }
//
//    private void sendScreenData(final Bitmap bitmap) {
//        // Convertir l'image en chaîne Base64
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
//        byte[] byteArray = byteArrayOutputStream.toByteArray();
//        final String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
//
//        // Envoyer l'image encodée au serveur PHP
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    String serverUrl = "http://407.projet3il.fr/index.php";
//                    URL url = new URL(serverUrl);
//                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                    connection.setRequestMethod("POST");
//                    connection.setDoOutput(true);
//
//                    // Envoyer l'image encodée dans le corps de la requête
//                    OutputStream outputStream = connection.getOutputStream();
//                    outputStream.write(encodedImage.getBytes());
//                    outputStream.flush();
//                    outputStream.close();
//
//                    connection.getResponseCode(); // Important pour déclencher la requête
//                    connection.disconnect();
//                } catch (Exception e) {
//                    Log.e("SendImageDataTask", "Erreur lors de l'envoi de l'image au serveur", e);
//                }
//            }
//        }).start();
//    }
}

