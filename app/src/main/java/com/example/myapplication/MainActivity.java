package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import com.example.myapplication.Grid_detection;




public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button btOpen;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    Uri imageUri;
    MyTessOCR mTessOCR;

    static {
        OpenCVLoader.initDebug();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        imageView=findViewById(R.id.image_view);
        btOpen=(Button) findViewById(R.id.bt_open);

        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);

        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

        //Camera Permiisson requesting
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.CAMERA)!=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 100);
        }
        Uri uriimage;
        btOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "New Picture");
                    values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                    imageUri = getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        mTessOCR = new MyTessOCR(this);

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode== Activity.RESULT_OK) {
            try {
                Bitmap captureImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                Bitmap processedImage=Grid_detection.detect(captureImage,imageView);
                Log.d("fasak",mTessOCR.getOCRResult(captureImage));
                imageView.setImageBitmap(processedImage);

                Bitmap bmp32 = processedImage.copy(Bitmap.Config.ARGB_8888, true);
                Mat destImage = new Mat (bmp32.getWidth(),
                        bmp32.getHeight(),
                        CvType.CV_8UC1);
                Utils.bitmapToMat(bmp32, destImage);
                org.opencv.core.Rect rect2 = new Rect( 0,0,destImage.width()/9,destImage.height()/9);
                destImage=new Mat(destImage,rect2);
                Bitmap squareBmp = Bitmap.createBitmap(destImage.cols(), destImage.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(destImage, squareBmp);


                //  Log.d("recognised",mTessOCR.getOCRResult(processedImage));
            } catch (Exception e) {
                e.printStackTrace();
            }

            // imageView.setImageBitmap(captureImage);
            //imageView.setImageBitmap(captureImage);
        }
    }
}