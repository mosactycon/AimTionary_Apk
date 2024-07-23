package com.example.aimtect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{
    static {
        if(OpenCVLoader.initDebug()){
            Log.d("MainActivity: ","Opencv is loaded");
        }
        else {
            Log.d("MainActivity: ","Opencv failed to load");
        }
    }

    private com.google.android.material.bottomnavigation.BottomNavigationView bottom_navigation_view;
    private com.google.android.material.floatingactionbutton.FloatingActionButton camera_button;
    private ImageButton object_detection_button;
    private ImageButton sign_language_menu_button;
    private ImageButton image_to_text_button;
    private ImageButton code_scanner_button;
    private ImageButton facial_expression_button;
    private ImageButton face_recognition_button;
    private MenuItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        ImageSlider imageSlider=findViewById       (R.id.slider);
        List<SlideModel> slideModels=new ArrayList<>();
        slideModels.add(new SlideModel(R.drawable.face_recognition_banner));
        slideModels.add(new SlideModel(R.drawable.sign_language_banner));
        slideModels.add(new SlideModel(R.drawable.expression_recognition_banner));
        slideModels.add(new SlideModel(R.drawable.object_recognition_banner));

        imageSlider.setImageList(slideModels,true);

        bottom_navigation_view=findViewById(R.id.bottom_navigation_view);
        bottom_navigation_view.setItemIconTintList(null);
        bottom_navigation_view.setBackgroundTintList(null);
        bottom_navigation_view.setOnNavigationItemSelectedListener(this);

        camera_button=findViewById(R.id.camera_button);
        camera_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,CameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        object_detection_button=findViewById(R.id.object_detection_button);
        object_detection_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, objectDetectorActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        sign_language_menu_button=findViewById(R.id.sign_language_button);
        sign_language_menu_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, signLanguageActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        image_to_text_button=findViewById(R.id.image_to_text_button);
        image_to_text_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, imageToTextActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        code_scanner_button=findViewById(R.id.code_scanner_button);
        code_scanner_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, codeScannerActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        facial_expression_button=findViewById(R.id.facial_expression_button);
        facial_expression_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, facialExpressionActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        face_recognition_button=findViewById(R.id.face_recognition_button);
        face_recognition_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, faceRecognitionActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.about:
                startActivity(new Intent(MainActivity.this, aboutActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NO_ANIMATION));

                return true;
        }

        return false;
    }


}