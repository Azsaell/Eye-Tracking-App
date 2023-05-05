package com.example.eye_tracking_app;

//import static androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;


import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
//import androidx.camera.mlkit.vision.MlKitAnalyzer;
import androidx.camera.view.CameraController;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.eye_tracking_app.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tflite.java.TfLite;
import com.google.mlkit.vision.camera.CameraSourceConfig;
import com.google.mlkit.vision.camera.CameraXSource;
import com.google.mlkit.vision.camera.DetectionTaskCallback;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.tensorflow.lite.InterpreterApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    ImageView imageViewLeft;
    ImageView imageViewRight;

    private InterpreterApi interpreter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Task<Void> initializeTask = TfLite.initialize(this);
//        File model = new File("app//main/java/com/example/eye_tracking_app/model.tflite");
        Map<Integer, Object> map_of_indices_to_outputs = new HashMap <>();
        FloatBuffer ith_output = FloatBuffer.allocate(2);
        float [] [] output = new float[1][2];
        map_of_indices_to_outputs.put(0, ith_output);
        map_of_indices_to_outputs.put(0, output);

        float [] [] [] [] matrix = new float  [1] [3] [128] [128];

        for (int i=0; i<matrix.length; i++) {
            for (int j=0; j<matrix[i].length; j++) {
                for (int k=0; k<matrix[i][j].length; k++) {
                    matrix[0][i][j][k] = (float) (Math.random());
                }
            }
        }
        float [] [] [] [] eyeR = matrix;

        for (int i=0; i<matrix.length; i++) {
            for (int j=0; j<matrix[i].length; j++) {
                for (int k=0; k<matrix[i][j].length; k++) {
                    matrix[0][i][j][k] = (float) (Math.random());
                }
            }
        }
        float [] [] [] [] eyeL = matrix;

        float [] lm = new float [8];

        for (int i=0; i<lm.length; i++) {
            lm[i] = (float) (Math.random());

        }




        Object[] inputs = {eyeR, lm, eyeL};
        try {
            MappedByteBuffer  model = loadModelFile();
            initializeTask.addOnSuccessListener(a -> {
                        interpreter = InterpreterApi.create(model,
                                new InterpreterApi.Options().setRuntime(InterpreterApi.Options.TfLiteRuntime.FROM_SYSTEM_ONLY));
                        interpreter.runForMultipleInputsOutputs(inputs,map_of_indices_to_outputs);
                        System.out.println(((float [] []) map_of_indices_to_outputs.get(0)));
                        System.out.println(output);
                        System.out.println(ith_output.get());
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Interpreter", String.format("Cannot initialize interpreter: %s",
                                e.getMessage()));
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }



        System.out.println(interpreter);




//        cameraController = new LifecycleCameraController(getBaseContext());
        previewView =  (PreviewView) findViewById(R.id.previewView);
        imageViewLeft = findViewById(R.id.imageView);
        imageViewRight = findViewById(R.id.imageView3);
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

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd("model.tflite");
        FileInputStream inputStream = new  FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
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
            if (face.getLeftEyeOpenProbability() == null || face.getRightEyeOpenProbability() == null)
                return;
            if ((face.getLeftEyeOpenProbability() > THRESHOLD || face.getRightEyeOpenProbability() > THRESHOLD)) {
                Log.i(TAG, "onUpdate: Eyes Detected");
                FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);
                FaceLandmark rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE);
                FaceContour leftEyeContour = face.getContour(FaceContour.LEFT_EYE);
                FaceContour rightEyeContour = face.getContour(FaceContour.RIGHT_EYE);
                if (leftEyeContour == null || rightEyeContour == null)
                    return;
//                System.out.println("Left eye position from landmark: " + leftEye.getPosition());
                System.out.println("Left eye position from kontur1: " + leftEyeContour.getPoints().get(0));
                System.out.println("Left eye position from kontur2: " + leftEyeContour.getPoints().get(7));
                PointF leftEyePosition = leftEye.getPosition();
//                System.out.println("Right eye position from landmark: " + rightEye.getPosition());
                System.out.println("Right eye position from kontur1: " + rightEyeContour.getPoints().get(0));
                System.out.println("Right eye position from kontur2: " + rightEyeContour.getPoints().get(7));
                PointF rightEyePosition = rightEye.getPosition();
                Bitmap bitmapka = previewView.getBitmap();

//                float leftEyeWidth = leftEyeContour.getPoints().get(7).x - leftEyeContour.getPoints().get(0).x;
//                float rightEyeWidth = rightEyeContour.getPoints().get(7).x - rightEyeContour.getPoints().get(0).x;

//                int leftEyeImageX = (int) Math.min(Math.max(0, (bitmapka.getWidth() - leftEyePosition.x - 70 - leftEyeWidth/2)), bitmapka.getWidth()- leftEyeWidth);
//                int leftEyeImageY = (int) Math.min(Math.max(0, (leftEyePosition.y - 150 - leftEyeWidth/2)), bitmapka.getHeight()- leftEyeWidth);
//
//                int rightEyeImageX = (int) Math.min(Math.max(0, (bitmapka.getWidth() - rightEyePosition.x - 70 - rightEyeWidth/2)), bitmapka.getWidth()- rightEyeWidth);
//                int rightEyeImageY = (int) Math.min(Math.max(0,  rightEyePosition.y - 150 - rightEyeWidth/2), bitmapka.getHeight()- rightEyeWidth);


                int leftEyeImageX = Math.min(Math.max(0, (int) (bitmapka.getWidth() - leftEyePosition.x - 70 - 64)), bitmapka.getWidth()- 128);
                int leftEyeImageY = Math.min(Math.max(0, (int) leftEyePosition.y - 150 - 64), bitmapka.getHeight()- 128);

                int rightEyeImageX = Math.min(Math.max(0, (int) (bitmapka.getWidth() - rightEyePosition.x - 70 - 64)), bitmapka.getWidth()- 128);
                int rightEyeImageY = Math.min(Math.max(0, (int) rightEyePosition.y - 150 - 64), bitmapka.getHeight()- 128);



                Bitmap croppedBitmapLeft = Bitmap.createBitmap(bitmapka, leftEyeImageX, leftEyeImageY, 128, 128);
                Bitmap croppedBitmapRight = Bitmap.createBitmap(bitmapka, rightEyeImageX, rightEyeImageY, 128, 128);

//                Bitmap croppedBitmapLeft = Bitmap.createBitmap(bitmapka, leftEyeImageX, leftEyeImageY, (int) leftEyeWidth, (int) leftEyeWidth);
//                Bitmap croppedBitmapRight = Bitmap.createBitmap(bitmapka, rightEyeImageX, rightEyeImageY, (int) rightEyeWidth, (int) rightEyeWidth);

//                croppedBitmapRight = createFlippedBitmap(croppedBitmapRight,true,false);
                croppedBitmapLeft = createFlippedBitmap(croppedBitmapLeft,true,false);  //to je dobre vhyaba
//
//                croppedBitmapLeft = getResizedBitmap(croppedBitmapLeft, 128, 128);
//                croppedBitmapRight = getResizedBitmap(croppedBitmapRight, 128, 128);


                int [] pixele = new int[128*128];
                croppedBitmapRight.getPixels(pixele,0,128,0,0,croppedBitmapRight.getWidth(),croppedBitmapRight.getHeight()); //wykorzystać to zamiast cloppowania bitmapy
                float [][][][] arrayRightEye= convertPixelsToArray(pixele);
                croppedBitmapLeft.getPixels(pixele,0,128,0,0,croppedBitmapRight.getWidth(),croppedBitmapRight.getHeight()); //wykorzystać to zamiast cloppowania bitmapy
                float [][][][] arrayLeftEye= convertPixelsToArray(pixele);



                float [][] lm = new float[1][8];
                lm[0][0] = (bitmapka.getWidth() - leftEyeContour.getPoints().get(0).x - 70)/bitmapka.getWidth();
                lm[0][1] = (leftEyeContour.getPoints().get(0).y - 150)/bitmapka.getHeight();
                lm[0][2] = (bitmapka.getWidth() - leftEyeContour.getPoints().get(7).x - 70)/bitmapka.getWidth();
                lm[0][3] = (leftEyeContour.getPoints().get(7).y - 150)/bitmapka.getHeight();
                lm[0][4] = (bitmapka.getWidth() - rightEyeContour.getPoints().get(0).x - 70)/bitmapka.getWidth();
                lm[0][5] = (rightEyeContour.getPoints().get(0).y - 150)/bitmapka.getHeight();
                lm[0][6] = (bitmapka.getWidth() - rightEyeContour.getPoints().get(7).x - 70)/bitmapka.getWidth();
                lm[0][7] = (rightEyeContour.getPoints().get(7).y - 150)/bitmapka.getHeight();

//                Object[] inputs = {arrayRightEye, lm, arrayLeftEye};
                Object[] inputs = {arrayLeftEye, lm, arrayRightEye};
                Map<Integer, Object> map_of_indices_to_outputs = new HashMap <>();
                float [] [] output = new float[1][2];
                map_of_indices_to_outputs.put(0, output);

                interpreter.runForMultipleInputsOutputs(inputs,map_of_indices_to_outputs);
                System.out.println(output[0][0]);
                System.out.println(output[0][1]);



                Canvas canvas = new Canvas(bitmapka);
                rightEyeContour.getPoints().stream().forEach(pointt -> canvas.drawCircle(bitmapka.getWidth() - pointt.x  - 50,pointt.y - 150,3, new Paint()));
//                leftEyeContour.getPoints().stream().forEach(pointt -> canvas.drawCircle(bitmapka.getWidth() - pointt.x  - 50,pointt.y - 150,3, new Paint()));
//                imageViewLeft.setImageBitmap(bitmapka);
                imageViewLeft.setImageBitmap(croppedBitmapLeft);
                imageViewRight.setImageBitmap(croppedBitmapRight);

                showStatus("Eyes Detected and open. X? = " +output[0][0] + "  Y? = " + output[0][1]);
            } else {

                showStatus("Eyes Detected and closed");
            }
        } else {
            showStatus("Face Not Detected yet!");
        }

    }

    public static Bitmap createFlippedBitmap(Bitmap source, boolean xFlip, boolean yFlip) {
        Matrix matrix = new Matrix();
        matrix.postScale(xFlip ? -1 : 1, yFlip ? -1 : 1, source.getWidth() / 2f, source.getHeight() / 2f);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    public static float[][][][] convertPixelsToArray(int [] pixels) {
        float[][][][] p = new float[1][3][128][128];
            for (int j = 0; j<128;j++){
                for (int k = 0; k<128;k++){
                    p[0][0][j][k] = (float) (((((float) Color.red(pixels[(j*128)+k]))/255) - 0.3741)/0.02);
                    p[0][1][j][k] = (float) (((((float) Color.green(pixels[(j*128)+k]))/255) - 0.4076)/0.02);
                    p[0][2][j][k] = (float) (((((float) Color.blue(pixels[(j*128)+k]))/255) - 0.5425)/0.02);
                }
            }
            return p;
    }



    private void onDetectionTaskFailure(Exception e) {

    }

    public void createCameraSource() {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .enableTracking()
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build();
        FaceDetector detector = FaceDetection.getClient(options);
        DetectionTaskCallback detectionTaskCallback =
                detectionTask ->
                        detectionTask
                                .addOnSuccessListener(this::onDetectionTaskSuccess)
                                .addOnFailureListener(this::onDetectionTaskFailure);
        CameraSourceConfig cameraSourceConfig = new CameraSourceConfig.Builder(this, detector, detectionTaskCallback)
                .setRequestedPreviewSize(1080, 960)
                .setFacing(CameraSourceConfig.CAMERA_FACING_FRONT)
                .build();
        cameraSource = new CameraXSource(cameraSourceConfig, previewView);
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