package com.example.eye_tracking_app;

//import static androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;


import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
//import androidx.camera.mlkit.vision.MlKitAnalyzer;
import androidx.camera.view.CameraController;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.eye_tracking_app.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.camera.CameraSourceConfig;
import com.google.mlkit.vision.camera.CameraXSource;
import com.google.mlkit.vision.camera.DetectionTaskCallback;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;
import com.google.mlkit.vision.interfaces.Detector;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private static final String TAG = "MainActivity";
    //    VideoView videoView;
    EditText textView;

    //For looking logs
    ArrayAdapter adapter;
    ArrayList<String> list = new ArrayList<>();

    CameraXSource cameraSource;
    CameraController cameraController;
    PreviewView previewView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        cameraController = new LifecycleCameraController(getBaseContext());
//        PreviewView previewView = findViewById(R.id.previewView);
//        previewView.setController(cameraController);

//        previewView =


        textView = (EditText) findViewById(R.id.editText);
        textView.setText("Dupa testowa");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
            Toast.makeText(this, "Grant Permission and restart app", Toast.LENGTH_SHORT).show();
        } else {
//            videoView = findViewById(R.id.videoView);
            textView = findViewById(R.id.editText);
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
            //videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.videoplayback));
            //videoView.start();
            createCameraSource();
        }


//        binding = ActivityMainBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        setSupportActionBar(binding.toolbar);
//
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
//        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//
//        binding.fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

//    private class EyesTracker extends Tracker<Face> {
//
//        private final float THRESHOLD = 0.75f;
//
//        private EyesTracker() {
//
//        }
//
//        @Override
//        public void onUpdate(Detector.Detections<Face> detections, Face face) {
//            if (face.getIsLeftEyeOpenProbability() > THRESHOLD || face.getIsRightEyeOpenProbability() > THRESHOLD) {
//                Log.i(TAG, "onUpdate: Eyes Detected");
//                FaceLandmark leftEar = face.getLandmark(FaceLandmark.LEFT_EAR);
//                showStatus("Eyes Detected and open, so video continues");
////                if (!videoView.isPlaying())
////                    videoView.start();
//
//            }
//            else {
////                if (videoView.isPlaying())
////                    videoView.pause();
//
//                showStatus("Eyes Detected and closed, so video paused");
//            }
//        }
//
//        @Override
//        public void onMissing(Detector.De<Face> detections) {
//            super.onMissing(detections);
//            showStatus("Face Not Detected yet!");
//        }
//
//        @Override
//        public void onDone() {
//            super.onDone();
//        }
//    }
//
//    private class FaceTrackerFactory implements MultiProcessor.Factory<Face> {
//
//        private FaceTrackerFactory() {
//
//        }
//
//        @Override
//        public Tracker<Face> create(Face face) {
//            return new EyesTracker();
//        }
//    }

    private void onDetectionTaskSuccess(Object faceList) {
        final float THRESHOLD = 0.75f;
        if (((List) faceList).size() > 0) {
            Face face = ((List<Face>) faceList).get(0);
//        Face face = results.get(0);
            if ((face.getLeftEyeOpenProbability() > THRESHOLD || ((Face) face).getRightEyeOpenProbability() > THRESHOLD)) {
                Log.i(TAG, "onUpdate: Eyes Detected");
                FaceLandmark leftEye = ((Face) face).getLandmark(FaceLandmark.LEFT_EYE);
                FaceLandmark rightEye = ((Face) face).getLandmark(FaceLandmark.RIGHT_EYE);
                System.out.println(leftEye.getPosition());
                System.out.println(rightEye.getPosition());

                showStatus("Eyes Detected and open, so video continues");
//                if (!videoView.isPlaying())
//                    videoView.start();

            } else {
//                if (videoView.isPlaying())
//                    videoView.pause();

                showStatus("Eyes Detected and closed, so video paused");
            }
        } else {
            showStatus("Face Not Detected yet!");
        }

    }

    private void onDetectionTaskFailure(Exception e) {

    }

    public void createCameraSource() {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .enableTracking()
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build();
        FaceDetector detector = FaceDetection.getClient(options);
//        detector.
//        detector.setProcessor(new MultiProcessor.Builder(new FaceTrackerFactory()).build());
        DetectionTaskCallback detectionTaskCallback =
                detectionTask ->
                        detectionTask
                                .addOnSuccessListener(this::onDetectionTaskSuccess)
                                .addOnFailureListener(this::onDetectionTaskFailure);


        CameraSourceConfig cameraSourceConfig = new CameraSourceConfig.Builder(this, detector, detectionTaskCallback)
                .setRequestedPreviewSize(1024, 768)
                .setFacing(CameraSourceConfig.CAMERA_FACING_FRONT)
                .build();
        cameraSource = new CameraXSource(cameraSourceConfig);
//        cameraController.se

//        cameraController.setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(this),
//                new MlKitAnalyzer(List.of(detector), COORDINATE_SYSTEM_VIEW_REFERENCED,
//                        ContextCompat.getMainExecutor(this), result -> {
//                    result.getValue(detector).stream().findFirst().ifPresent(face -> {
//                        final float THRESHOLD = 0.75f;
//                        if ((face.getLeftEyeOpenProbability() > THRESHOLD || ((Face) face).getRightEyeOpenProbability() > THRESHOLD)) {
//                            Log.i(TAG, "onUpdate: Eyes Detected");
//                            FaceLandmark leftEye = ((Face) face).getLandmark(FaceLandmark.LEFT_EYE);
//                            FaceLandmark rightEye = ((Face) face).getLandmark(FaceLandmark.RIGHT_EYE);
//                            System.out.println(leftEye.getPosition());
//                            System.out.println(rightEye.getPosition());
//
//                            showStatus("Eyes Detected and open, so video continues");
//
//                            } else {
//
//                                showStatus("Eyes Detected and closed, so video paused");
//                            }
//                        }
//                    );
//                    // The value of result.getResult(barcodeScanner) can be used directly for drawing UI overlay.
//                }));
////        previewView.controller = cameraController

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        cameraSource.start();

    }

    public void showStatus(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView = findViewById(R.id.editText);
                textView.setText(message);
            }
        });
    }
}