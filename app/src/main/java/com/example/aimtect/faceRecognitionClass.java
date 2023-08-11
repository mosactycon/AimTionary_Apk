package com.example.aimtect;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class faceRecognitionClass {
    private Interpreter interpreter;
    private int INPUT_SIZE;
    private int height=0;
    private int width=0;
    private GpuDelegate gpuDelegate=null;
    private CascadeClassifier cascadeClassifier;

    faceRecognitionClass(AssetManager assetManager, Context context, String modelPath,int inputSize) throws IOException {
        INPUT_SIZE=inputSize;
        Interpreter.Options options=new Interpreter.Options();
        gpuDelegate=new GpuDelegate();
//        options.addDelegate(gpuDelegate);
        options.setNumThreads(4);
        interpreter=new Interpreter(loadModelFile(assetManager,modelPath),options);

        Log.d("facial_Expression","Model is loaded");

        try {

            InputStream is=context.getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
            File cascadeDir=context.getDir("cascade",Context.MODE_PRIVATE);
            File mCascadeFile=new File(cascadeDir,"haarcascade_frontalface_alt");
            FileOutputStream os=new FileOutputStream(mCascadeFile);
            byte[] buffer=new byte[4096];
            int byteRead;
            while ((byteRead=is.read(buffer)) !=-1){
                os.write(buffer,0,byteRead);

            }
            is.close();
            os.close();
            cascadeClassifier=new CascadeClassifier(mCascadeFile.getAbsolutePath());
            Log.d("facial_Expression","Classifier is loaded");

        }
        catch (IOException e){
            e.printStackTrace();
        }

    }

    public Mat recognizeImage(Mat mat_image){
        Core.flip(mat_image.t(),mat_image,1);
        Mat grayscaleImage=new Mat();
        Imgproc.cvtColor(mat_image,grayscaleImage,Imgproc.COLOR_RGBA2GRAY);
        height=grayscaleImage.height();
        width=grayscaleImage.width();
        int absoluteFaceSize=(int) (height*0.1);
        MatOfRect faces=new MatOfRect();
        if(cascadeClassifier !=null){

            cascadeClassifier.detectMultiScale(grayscaleImage,faces,1.1,2,2,
                    new Size(absoluteFaceSize,absoluteFaceSize),new Size());

        }

        Rect[] faceArray=faces.toArray();
        for (int i=0;i<faceArray.length;i++){

            Imgproc.rectangle(mat_image,faceArray[i].tl(),faceArray[i].br(),new Scalar(0,255,0,255),2);
            Rect roi=new Rect((int) faceArray[i].tl().x,(int)faceArray[i].tl().y,
                    ((int)faceArray[i].br().x)-((int) faceArray[i].tl().x),
                    ((int)faceArray[i].br().y)-((int)faceArray[i].tl().y));
            Mat cropped_rgb=new Mat(mat_image,roi);
            Bitmap bitmap=null;
            bitmap=Bitmap.createBitmap(cropped_rgb.cols(),cropped_rgb.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(cropped_rgb,bitmap);
            Bitmap scaledBitmap=Bitmap.createScaledBitmap(bitmap,INPUT_SIZE,INPUT_SIZE,false);
            ByteBuffer byteBuffer=convertBitmapToByteBuffer(scaledBitmap);

            float[][] face_value=new float[1][1];
            interpreter.run(byteBuffer,face_value);

            Log.d("faceRecognitionClass","Out:"+Array.get(Array.get(face_value,0),0));
            float read_face=(float) Array.get(Array.get(face_value,0),0);
            String face_name=get_face_name(read_face);
            Imgproc.putText(mat_image,""+face_name,
                    new Point((int)faceArray[i].tl().x+10, (int)faceArray[i].tl().y+20),
                    1,1.5,new Scalar(0,255,0,255),2);

        }
        Core.flip(mat_image.t(),mat_image,0);

        return mat_image;
    }

    private String get_face_name(float read_face) {
        String val="";
        if(read_face>=0 & read_face<0.5){
            val="PUTU_SUPADMA_RUDANA";
        }
        else if(read_face>=0.5 & read_face < 1.5) {
            val="PUAN_MAHARANI";
        }
        else if(read_face>=1.5 & read_face < 2.5) {
            val="INDRA_ISKANDAR";
        }
        else if(read_face>=2.5 & read_face < 3.5) {
            val="_MUHAIMIN_ISKANDAR";
        }
        else if(read_face>=3.5 & read_face < 4.5) {
            val="FADLI_ZON";
        }
        else
        {
            val = "FARIS_MIGHWAR";
        }
//        else
//        {
//            val = "FADLI ZON";
//        }
        return val;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap scaledBitmap) {
        ByteBuffer byteBuffer;
        int input_size=INPUT_SIZE;
        byteBuffer=ByteBuffer.allocateDirect(4*1*input_size*input_size*3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues=new int[input_size*input_size];
        scaledBitmap.getPixels(intValues,0,scaledBitmap.getWidth(),0,0,scaledBitmap.getWidth()
                ,scaledBitmap.getHeight());
        int pixels=0;
        for (int i=0;i<input_size;++i){
            for (int j=0;j<input_size;++j){
                final int val=intValues[pixels++];
                byteBuffer.putFloat((((val>>16)&0xFF))/255.0f);
                byteBuffer.putFloat((((val>>8)&0xFF))/255.0f);
                byteBuffer.putFloat(((val & 0xFF))/255.0f);
            }
        }
        return byteBuffer;
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException{
        AssetFileDescriptor assetFileDescriptor=assetManager.openFd(modelPath);
        FileInputStream inputStream=new FileInputStream(assetFileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();

        long startOffset=assetFileDescriptor.getStartOffset();
        long declaredLength=assetFileDescriptor.getDeclaredLength();
        return  fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declaredLength);

    }


}