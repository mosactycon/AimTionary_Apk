package com.example.aimtect;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.speech.tts.TextToSpeech;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class objectDetectorClass {

    private Interpreter interpreter;
    private List<String> labelList;
    private List<String> labelText;
    private int INPUT_SIZE;
    private int PIXEL_SIZE=3;
    private int IMAGE_MEAN=0;
    private  float IMAGE_STD=255.0f;
    private GpuDelegate gpuDelegate;
    private int height=0;
    private  int width=0;
    private TextToSpeech textToSpeech;
    private String language="";
    private Context context;
    private String final_text="";
    private String final_text_label="";

    private TextView label_text;


    objectDetectorClass(Context context, Button text_speech_button, Button languageButton, PopupMenu popupMenu, TextView label_text, AssetManager assetManager, String modelPath, String labelPathIndo, String labelPath, String labelPathLatin, String labelPathArab, int inputSize) throws IOException{
        INPUT_SIZE=inputSize;
        Interpreter.Options options=new Interpreter.Options();
        gpuDelegate=new GpuDelegate();
        options.addDelegate(gpuDelegate);
        options.setNumThreads(4);

        interpreter=new Interpreter(loadModelFile(assetManager,modelPath),options);
        labelList=loadLabelList(assetManager,labelPath);
        labelText=loadLabelText(assetManager,labelPath);

        this.label_text = label_text;

        this.context = context;
        textToSpeech=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status !=TextToSpeech.ERROR){
//                    textToSpeech.setLanguage(new Locale("id","ID"));
                    textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });

        languageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show the popup menu
                popupMenu.show();
            }
        });

        // Set the click listener for the menu items
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item1:
                        try {
                            labelList=loadLabelList(assetManager,labelPathIndo);
                            labelText=loadLabelText(assetManager,labelPathIndo);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        textToSpeech=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if(status !=TextToSpeech.ERROR){
                                    textToSpeech.setLanguage(new Locale("id","ID"));
                                }
                            }
                        });
                        Toast.makeText(context, "Bahasa Indonesia Selected", Toast.LENGTH_SHORT).show();
                        return true;


                    case R.id.item2:
                        try {
                            labelList=loadLabelList(assetManager,labelPath);
                            labelText=loadLabelText(assetManager,labelPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        textToSpeech=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if(status !=TextToSpeech.ERROR){
                                    textToSpeech.setLanguage(Locale.ENGLISH);
                                }
                            }
                        });
                        Toast.makeText(context, "English Language Selected", Toast.LENGTH_SHORT).show();
                        return true;

                    case R.id.item3:
                        try {
                        labelList=loadLabelList(assetManager,labelPathLatin);
                        labelText=loadLabelText(assetManager,labelPathArab);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        textToSpeech=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if(status !=TextToSpeech.ERROR){
                                    textToSpeech.setLanguage(new Locale("ar","SA"));
                                }
                            }
                        });
                        Toast.makeText(context, "Arabic Language Selected", Toast.LENGTH_SHORT).show();
                        return true;

                    default:
                        try {
                            labelList=loadLabelList(assetManager,labelPath);
                            labelText=loadLabelText(assetManager,labelPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        textToSpeech=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if(status !=TextToSpeech.ERROR){
                                    textToSpeech.setLanguage(Locale.ENGLISH);
                                }
                            }
                        });
//                        Toast.makeText(MainActivity.this, "English Language Selected", Toast.LENGTH_SHORT).show();
                        return false;
                }
            }
        });

        text_speech_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeech.speak(final_text,TextToSpeech.QUEUE_FLUSH,null);
            }
        });

    }
    private void updateLabelText() {
        label_text.post(new Runnable() {
            @Override
            public void run() {
                label_text.setText(final_text_label);
            }
        });
    }

    private List<String> loadLabelList(AssetManager assetManager, String labelPath) throws IOException {
        List<String> labelList=new ArrayList<>();
        BufferedReader reader=new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
        String line;
        while ((line=reader.readLine())!=null){
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    private List<String> loadLabelText(AssetManager assetManager, String labelPathText) throws IOException {
        List<String> labelText=new ArrayList<>();
        BufferedReader reader=new BufferedReader(new InputStreamReader(assetManager.open(labelPathText)));
        String line;
        while ((line=reader.readLine())!=null){
            labelText.add(line);
        }
        reader.close();
        return labelText;
    }

    private ByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor=assetManager.openFd(modelPath);
        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startOffset =fileDescriptor.getStartOffset();
        long declaredLength=fileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declaredLength);
    }
    public Mat recognizeImage(Mat mat_image){

        Mat rotated_mat_image=new Mat();
        Core.flip(mat_image.t(),rotated_mat_image,1);
        Bitmap bitmap=null;
        bitmap=Bitmap.createBitmap(rotated_mat_image.cols(),rotated_mat_image.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rotated_mat_image,bitmap);
        height=bitmap.getHeight();
        width=bitmap.getWidth();

        Bitmap scaledBitmap=Bitmap.createScaledBitmap(bitmap,INPUT_SIZE,INPUT_SIZE,false);

        ByteBuffer byteBuffer=convertBitmapToByteBuffer(scaledBitmap);

        Object[] input=new Object[1];
        input[0]=byteBuffer;

        Map<Integer,Object> output_map=new TreeMap<>();

        float[][][]boxes =new float[1][10][4];
        float[][] scores=new float[1][10];
        float[][] classes=new float[1][10];


        output_map.put(0,boxes);
        output_map.put(1,classes);
        output_map.put(2,scores);

        interpreter.runForMultipleInputsOutputs(input,output_map);

        Object value=output_map.get(0);
        Object Object_class=output_map.get(1);
        Object score=output_map.get(2);

        for (int i=0;i<10;i++){
            float class_value=(float) Array.get(Array.get(Object_class,0),i);
            float score_value=(float) Array.get(Array.get(score,0),i);

            if(score_value>0.5){
                Object box1=Array.get(Array.get(value,0),i);

                float top=(float) Array.get(box1,0)*height;
                float left=(float) Array.get(box1,1)*width;
                float bottom=(float) Array.get(box1,2)*height;
                float right=(float) Array.get(box1,3)*width;

                final_text = labelList.get(((int) class_value));
                final_text_label = labelText.get(((int) class_value));

                updateLabelText();

                // Update the TextView with the detected label

                Imgproc.rectangle(rotated_mat_image,new Point(left,top),new Point(right,bottom),new Scalar(0,255,0,255),2);
                Imgproc.putText(rotated_mat_image,labelList.get((int) class_value),new Point(left,bottom),3,1,new Scalar(0,255,0,255),2);

            }

        }

        Core.flip(rotated_mat_image.t(),mat_image,0);
        return mat_image;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer;

        int quant=0;
        int size_images=INPUT_SIZE;
        if(quant==0){
            byteBuffer=ByteBuffer.allocateDirect(1*size_images*size_images*3);
        }
        else {
            byteBuffer=ByteBuffer.allocateDirect(4*1*size_images*size_images*3);
        }
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues=new int[size_images*size_images];
        bitmap.getPixels(intValues,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
        int pixel=0;

        for (int i=0;i<size_images;++i){
            for (int j=0;j<size_images;++j){
                final  int val=intValues[pixel++];
                if(quant==0){
                    byteBuffer.put((byte) ((val>>16)&0xFF));
                    byteBuffer.put((byte) ((val>>8)&0xFF));
                    byteBuffer.put((byte) (val&0xFF));
                }
                else {

                    byteBuffer.putFloat((((val >> 16) & 0xFF))/255.0f);
                    byteBuffer.putFloat((((val >> 8) & 0xFF))/255.0f);
                    byteBuffer.putFloat((((val) & 0xFF))/255.0f);
                }
            }
        }
        return byteBuffer;
    }
}