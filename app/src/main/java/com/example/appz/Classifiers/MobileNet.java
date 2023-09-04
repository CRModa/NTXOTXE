package com.example.appz.Classifiers;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import com.example.appz.Utils.ImageUtils;
import com.example.appz.Utils.ResultClass;
//import com.google.android.gms.fitness.data.DataType;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.TensorFlowLite;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.DataType.*;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MobileNet {
    private Interpreter tflite;

    public MobileNet(AssetManager assetManager, String pathModel) throws IOException {
        MappedByteBuffer modelBuffer = loadModelFile(assetManager, pathModel);
        tflite = new Interpreter(modelBuffer);
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String pathModel) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(pathModel);

        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());

        FileChannel fileChannel = inputStream.getChannel();

        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    public static ResultClass processOutputData(float[] output, int numClasses){
        String[] className = {"Afideos", "Capsula sã", "Cochonilha", "Fibra sã", "Folha sã", "Folha sã virada", "Jassideos", "Lagarta das folhas", "Lagarta do algodao", "Lagarta mineira", "Mancha Alternaria", "Mancha angular", "Mancha bacteriana", "Manchador de fibra", "Quimeras", "Vermelhao", "Virus do topo"};
        int maxClassIndex = -1;
        float maxClassProbability = 0.0f;

        for (int i = 0; i < numClasses; i++){
            System.out.println("Index: " + i + "prob: " + output[i]);
            if (output[i] > maxClassProbability){
                maxClassIndex = i;
                maxClassProbability = output[i];
            }
        }

        return new ResultClass(maxClassIndex, maxClassProbability, className[maxClassIndex]);
    }

    public String classifyImage(Bitmap image){
        ImageUtils imageUtils = new ImageUtils();

//        byte[][] input = imageUtils.processInputData(image, 256);
        float[] output = new float[17];
        Bitmap r = imageUtils.resizeBitmap(image, 256, 256);
        float[] input = imageUtils.processInput(image, 256);
        TensorImage inputImage = new TensorImage(DataType.FLOAT32);
        inputImage.load(input, new int[]{1, r.getWidth(), r.getHeight(), 3});

        TensorBuffer inputBuffer = TensorBuffer.createFixedSize(new int[]{1, r.getWidth(), r.getHeight(), 3}, DataType.FLOAT32);

        inputBuffer.loadBuffer(inputImage.getBuffer());

        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(new int[]{1, 17}, DataType.FLOAT32);


        ByteBuffer byteBuffer = imageUtils.convertBitmapToByteBuffer1(r);

        tflite.run(byteBuffer, outputBuffer.getBuffer());
        System.out.println("::::::::::::::::::::::" + outputBuffer.getFloatArray());
        ResultClass resultClass = processOutputData(outputBuffer.getFloatArray(), 17);

        String s = resultClass.toString();
        return s;
    }


}
