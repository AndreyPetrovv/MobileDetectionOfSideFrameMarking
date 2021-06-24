// Copyright (c) 2020 Facebook, Inc. and its affiliates.
// All rights reserved.
//
// This source code is licensed under the BSD-style license found in the
// LICENSE file in the root directory of this source tree.

package org.pytorch.demo.objectdetection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.pytorch.Module;
import org.pytorch.PyTorchAndroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Runnable {
    private int mImageIndex = 0;
    private final String[] mTestImages = {"t1.JPG", "t3.JPG", "t4.JPG", "t6.JPG",};

    private Bitmap image;

    private Button mButtonDetect;
    private Button mButtonSelect;
    private ProgressBar mProgressBar;
    private ProgressBar mProgressBar2;
    private DetectionModel detectionModel = null;
    private OCRModel ocrModel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }

        setContentView(R.layout.activity_main);


        //mImageView = findViewById(R.id.imageView);

        initImageView();
        initTestButton();
        initSelectButton();
        initDetectButton();
        loadModel();

        System.loadLibrary("opencv_java3");
    }


    @SuppressLint("DefaultLocale")
    private void initTestButton() {
        final Button buttonTest = findViewById(R.id.testButton);
        buttonTest.setText(String.format("Test Image 1/%d", mTestImages.length));
        buttonTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mImageIndex = (mImageIndex + 1) % mTestImages.length;
            }
        });
    }

    private void initImageView() {
        //mImageView.setImageBitmap(readImage());
    }

    private void initSelectButton() {
        final Button buttonSelect = findViewById(R.id.selectButton);
        buttonSelect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 1);
            }
        });
    }

    private void initDetectButton() {
        mButtonDetect = findViewById(R.id.detectButton);
        mButtonSelect = findViewById(R.id.selectButton);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar2 = (ProgressBar) findViewById(R.id.progressBar2);

        mButtonDetect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mButtonDetect.setEnabled(false);
                mButtonSelect.setEnabled(false);

                mProgressBar.setVisibility(ProgressBar.VISIBLE);
                mProgressBar2.setVisibility(ProgressBar.VISIBLE);
                //mButtonDetect.setText(getString(R.string.run_model));

                Thread thread = new Thread(MainActivity.this);
                thread.start();
            }
        });
    }

    private void loadModel() {
        try {
            Module detectionNetwork = PyTorchAndroid.loadModuleFromAsset(getAssets(), "detection.pt");
            Module ocrNetwork = PyTorchAndroid.loadModuleFromAsset(getAssets(), "ocr.pt");

            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("classes.txt")));
            String line;
            List<String> classes = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                classes.add(line);
            }

            PreProcessor preProcessor = new PreProcessor();
            PostProcessor postProcessor = new PostProcessor();

            detectionModel = new DetectionModel(detectionNetwork, (String[]) classes.toArray(new String[0]), preProcessor, postProcessor, 1024, 1024);
            ocrModel = new OCRModel(ocrNetwork, "0123456789-", preProcessor, postProcessor, 512, 512);

        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            finish();
        }
    }

    private Bitmap readImage() {
        Bitmap image = null;
        try {
            image = BitmapFactory.decodeStream(getAssets().open(mTestImages[mImageIndex]));
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            finish();
        }
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        image = (Bitmap) data.getExtras().get("data");
                        //Matrix matrix = new Matrix();
                        //matrix.postRotate(90.0f);
                        //image = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
                        //mImageView.setImageBitmap(mBitmap);
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                image = BitmapFactory.decodeFile(picturePath);
                                // Matrix matrix = new Matrix();
                                // matrix.postRotate(90.0f);
                                //image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
                                cursor.close();
                            }
                        }
                    }
                    break;
            }
            mButtonDetect.setEnabled(false);
            mButtonSelect.setEnabled(false);

            mProgressBar.setVisibility(ProgressBar.VISIBLE);
            mProgressBar2.setVisibility(ProgressBar.VISIBLE);
            //mButtonDetect.setText(getString(R.string.run_model));

            Thread thread = new Thread(MainActivity.this);
            thread.start();
        }


    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public void run() {
        //if (image == null) {
        //    image = readImage();
        //}
        ArrayList<DetectionResult> results = detectionModel.detect(image);

        ArrayList<ImageCrop> imageCrops = detectionModel.makeImageCrops(results, image);

        EditText factureID = findViewById(R.id.editTextTextFactureID);
        EditText numder = findViewById(R.id.editTextTextNumber);
        EditText year = findViewById(R.id.editTextTextYear);


        ArrayList<ImageCrop> imageCropsWithoutOther = new ArrayList<ImageCrop>();


        for (ImageCrop imageCrop : imageCrops) {
            if (!imageCrop.cropClass.equals("other")) {
                imageCrop.ocrValue = ocrModel.detect(imageCrop.image);
                imageCropsWithoutOther.add(imageCrop);
            }
        }

        runOnUiThread(() -> {
            for (ImageCrop imageCrop : imageCropsWithoutOther) {
                if (imageCrop.cropClass.equals("facture id")) {
                    factureID.setText(imageCrop.ocrValue);
                }
                if (imageCrop.cropClass.equals("number")) {
                    numder.setText(imageCrop.ocrValue);
                }
                if (imageCrop.cropClass.equals("year")) {
                    year.setText(imageCrop.ocrValue);
                }
            }

            mButtonDetect.setEnabled(true);
            mButtonSelect.setEnabled(true);
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            mProgressBar2.setVisibility(ProgressBar.INVISIBLE);
            viewListHandler(imageCropsWithoutOther);
        });


    }


    public void viewListHandler(ArrayList<ImageCrop> imageCrops) {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                MainActivity.this, R.style.BottomSheetDialogTheme
        );
        View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(
                R.layout.layout_bottom_sheet,
                (LinearLayout) findViewById(R.id.BottomSheetContainer)
        );

        bottomSheetDialog.setContentView(bottomSheetView);

        ListView listView = (ListView) bottomSheetDialog.findViewById(R.id.ListCrops);

        CropAdapter adapter = new CropAdapter(
                MainActivity.this,
                imageCrops
        );
        //привяжем массив через адаптер к Listview

        listView.setAdapter(adapter);

        bottomSheetDialog.show();
    }

}
