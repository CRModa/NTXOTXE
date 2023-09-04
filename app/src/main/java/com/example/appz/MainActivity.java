package com.example.appz;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appz.Classifiers.MobileNet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private MobileNet mobileNetClassifier;
    ActivityResultLauncher<Intent> cameraLauncher;
    ActivityResultLauncher<Intent> galleryLauncher;
    private Uri imgUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CardView buttonCamera = (CardView) findViewById(R.id.idCamera);
        CardView buttonGallery = (CardView) findViewById(R.id.idGallery);

        buttonCamera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 0);
                }else {
                    openCamera();
                }
            }
        });

        buttonGallery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                }else {
                    openGallery();
                }
            }
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode()  == RESULT_OK && result.getData() != null){
                    ImageView imageView = findViewById(R.id.idImg);
                    Bundle bundle = result.getData().getExtras();
                    Bitmap bitmap = (Bitmap) bundle.get("data");

//                    int tmnpxl = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 400, getResources().getDisplayMetrics());
//                    Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, tmnpxl, tmnpxl, true);

                    imageView.setImageBitmap(bitmap);
                    String className = "Não classificada";
                    try {
                        className = classifier(bitmap);
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, "Ocorreu algum erro...", Toast.LENGTH_LONG).show();
//                        throw new RuntimeException(e);
                    }
                    TextView textView = findViewById(R.id.idClass);
                    textView.setText(className);
                }
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode()  == RESULT_OK && result.getData() != null){
                    ImageView imageView = findViewById(R.id.idImg);
                    Uri imgURi = result.getData().getData();
                    Bitmap bitmap;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), imgURi);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    imageView.setImageBitmap(bitmap);
                    String className = "Não classificada";
                    try {
                        className = classifier(bitmap);
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, "Ocorreu algum erro...", Toast.LENGTH_LONG).show();
//                        throw new RuntimeException(e);
                    }
                    TextView textView = findViewById(R.id.idClass);
                    textView.setText(className);

                }
            }
        });
    }

    private File createPhotoFile(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageName = "ntxotxe_" + timeStamp + "_";

        String path = Environment.getExternalStorageDirectory().toString() + "/ntxotxe";
        File dir = new File(path);

        if (!dir.exists()){
            dir.mkdir();
        }

        File storageDir = dir;

        try {
            File file = File.createTempFile(imageName, ".jpg", storageDir);
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    private void openCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private void  openGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");

        if(intent.resolveActivity(getPackageManager()) != null){
            galleryLauncher.launch(intent);
        } else {
            Toast.makeText(MainActivity.this, "O App nao suporta está acção", Toast.LENGTH_LONG).show();
        }
    }

    public void popUp(String title, String msg){
        AlertDialog.Builder msgBox = new AlertDialog.Builder(this);
        msgBox.setTitle(title);
        msgBox.setMessage(msg);
        msgBox.setPositiveButton("Permitir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(MainActivity.this, "Permitido", Toast.LENGTH_SHORT).show();
            }
        });

        msgBox.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(MainActivity.this, "Cancelado", Toast.LENGTH_SHORT).show();
            }
        });

//        msgBox.setNeutralButton("Canelar", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                Toast.makeText(MainActivity.this, "Cancelado", Toast.LENGTH_SHORT).show();
//            }
//        });
        msgBox.show();
    }

    private void saveImage(Bitmap bitmap, String fileName) throws IOException {
        String pathFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/ntxotxe";
        String n = "//media/external/images/ntxotxe";
        File dir = new File(pathFolder);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, fileName + ".jpg");

        FileOutputStream fileOutputStream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
        System.out.println("::::::::::::::::::: " + dir.toString());
        fileOutputStream.flush();
        fileOutputStream.close();

        Toast.makeText(MainActivity.this, "Imagem salva", Toast.LENGTH_SHORT).show();
    }

    private String classifier(Bitmap bitmap) throws IOException {
        mobileNetClassifier = new MobileNet(getAssets(), "MN7Lite.tflite");

        return mobileNetClassifier.classifyImage(bitmap);
    }

}