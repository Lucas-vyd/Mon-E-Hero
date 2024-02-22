package com.example.mone_hero;

import static android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;
import android.view.Display;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;



public class MainActivity extends AppCompatActivity {
    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private ImageReader mImageReader;
    private Handler mHandler;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialiser le MediaProjectionManager
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

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
        textView.setText("ID: " + uniqueIdString);

        startMediaProjectionForegroundService();

        mHandler = new Handler(Looper.getMainLooper());
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Afficher la boîte de dialogue pour demander la permission de projection des médias
                startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
                Log.d("MainActivity","Envoie de l'image au serveur");
            }
        });
    }
    private static final int REQUEST_MEDIA_PROJECTION = 1;

    private void startMediaProjectionForegroundService() {
        Intent serviceIntent = new Intent(this, MediaProjectionForegroundService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    @Override
    protected void onDestroy() {
        // Arrêter le service en premier plan lorsque l'activité est détruite
        stopService(new Intent(this, MediaProjectionForegroundService.class));
        super.onDestroy();
    }

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