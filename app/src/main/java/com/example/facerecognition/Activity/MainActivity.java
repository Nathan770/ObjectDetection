package com.example.facerecognition.Activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.facerecognition.Adapter.ObjectAdapter;
import com.example.facerecognition.R;
import com.example.facerecognition.Object.DetectItem;
import com.example.facerecognition.ml.SsdMobilenetV11Metadata1;
import com.google.android.material.button.MaterialButton;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMG = 0;
    private static final float TEXT_SIZE_DIP = 8;
    private static final int DIM_PHOTO = 300;
    private ImageView imageView;
    private MaterialButton main_BTN_select;
    private MaterialButton main_BTN_detect;
    private ArrayList<String> arrLabels;
    private ArrayList<DetectItem> arrDetected;

    private ObjectAdapter theAdapter;
    private RecyclerView theListView;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arrLabels = new ArrayList<>();


        main_BTN_select = findViewById(R.id.main_BTN_select);
        main_BTN_detect = findViewById(R.id.main_BTN_detect);

        main_BTN_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                funcSelect(v);
            }
        });

        main_BTN_detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageView == null){
                    Toast.makeText(getApplicationContext(), "Please select photo before",Toast.LENGTH_SHORT).show();
                }else {
                    funcDetect(v);
                }

            }
        });

        String fileName = "label.txt";


        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open(fileName)));
            String line;
            while ((line = reader.readLine()) != null) {
                arrLabels.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void funcSelect(View view) {

        Intent photoSelected = new Intent(Intent.ACTION_PICK);
        photoSelected.setType("image/*");
        startActivityForResult(photoSelected, RESULT_LOAD_IMG);

    }

    public void funcDetect(View view) {
        theListView = findViewById(R.id.main_LBL_match);
        arrDetected = new ArrayList<>();

        Bitmap bm = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

        Bitmap resize = Bitmap.createScaledBitmap(bm, DIM_PHOTO, DIM_PHOTO, true);

        try {
            SsdMobilenetV11Metadata1 model = SsdMobilenetV11Metadata1.newInstance(this);

            // Creates inputs for reference.
            TensorImage image = TensorImage.fromBitmap(resize);


            // Runs model inference and gets result.
            SsdMobilenetV11Metadata1.Outputs outputs = model.process(image);
            TensorBuffer locations = outputs.getLocationsAsTensorBuffer();
            TensorBuffer classes = outputs.getClassesAsTensorBuffer();
            TensorBuffer scores = outputs.getScoresAsTensorBuffer();
            TensorBuffer numberOfDetections = outputs.getNumberOfDetectionsAsTensorBuffer();


            getDetection(classes.getFloatArray(), scores.getFloatArray());

            findDetection(locations.getFloatArray(), resize);

            layoutManager = new LinearLayoutManager(this);
            theListView.setLayoutManager(layoutManager);
            theAdapter = new ObjectAdapter(this, arrDetected);
            theListView.setAdapter(theAdapter);

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    private void findDetection(float[] locations, Bitmap photo) {


        Paint myRectangle = new Paint();
        myRectangle.setColor(Color.RED);
        myRectangle.setStyle(Paint.Style.STROKE);
        myRectangle.setStrokeWidth(2);

        Paint myTitle = new Paint();
        float textSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        myTitle.setTextSize(textSizePx);
        myTitle.setColor(Color.RED);

        Canvas c = new Canvas(photo);

        int index = 0;
        float left,  top,  right,  bottom ;

        for (int i = 0; i < locations.length; i = i + 4) {
            if (arrDetected.get(index).getMatched() > 0.50) {

                top = locations[i] * DIM_PHOTO ;
                left = locations[i + 1] * DIM_PHOTO ;
                bottom = locations[i + 2] * DIM_PHOTO ;
                right = locations[i + 3] * DIM_PHOTO ;

                c.drawText("" + arrDetected.get(index).getName() + " : " + arrDetected.get(index).getMatched() + " %", left, top, myTitle);
                c.drawRect(left, top, right, bottom , myRectangle);
            }
            index++;
        }
        imageView.setImageBitmap(photo);
    }


    private void getDetection(float[] name, float[] prob) {

        for (int i = 0; i < name.length; i++) {
            float r = prob[i];
            if (r > 0) {
                arrDetected.add(new DetectItem(arrLabels.get((int) name[i]), r));
            }
            if (arrDetected.size() == 100) {
                break;
            }
        }
        SortArray();

    }

    private void SortArray() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            arrDetected.sort(new Comparator<DetectItem>() {
                @Override
                public int compare(DetectItem o1, DetectItem o2) {

                    float f = o2.getMatched() - o1.getMatched();
                    if (f > 0) {
                        return 1;
                    } else {
                        return -1;
                    }

                }
            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        imageView = findViewById(R.id.main_IMG_all);

        if (requestCode == RESULT_LOAD_IMG) {
            Uri imageUri = data.getData();
            InputStream imageS = null;

            try {
                imageS = getContentResolver().openInputStream(imageUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap selectImage = BitmapFactory.decodeStream(imageS);

            imageView.setImageBitmap(selectImage);

        } else {
            Toast.makeText(this, "No Photo selected", Toast.LENGTH_SHORT).show();
        }

    }


}