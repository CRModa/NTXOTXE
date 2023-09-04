package com.example.appz.Utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ImageUtils {
    public Bitmap resizeBitmap(Bitmap bitmap, int width, int height){
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    public static Bitmap rotateBitmat(Bitmap bitmap, float degrees){
        Matrix matrix = new Matrix();

        matrix.postRotate(degrees);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static float[] normalizeBitmapForModel(Bitmap bitmap, int numChannels, float mean, float std) {
        int[] intValues = new int[bitmap.getWidth() * bitmap.getHeight()];
        float[] floatValues = new float[bitmap.getWidth() * bitmap.getHeight() * numChannels];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * numChannels + 0] = (((val >> 16) & 0xFF) / 255.0f - mean) / std;
            floatValues[i * numChannels + 1] = (((val >> 8) & 0xFF) / 255.0f - mean) / std;
            floatValues[i * numChannels + 2] = ((val & 0xFF) / 255.0f - mean) / std;
        }
        return floatValues;
    }


    public float[] processInput(Bitmap bitmap, int inputSize){
        Bitmap resized = resizeBitmap(bitmap, inputSize,inputSize);

        return normalizeBitmapForModel(resized, 3, 0.5f, 0.5f);
    }

    public byte[][] processInputData(Bitmap bitmap, int inputSize){

        Bitmap resized = resizeBitmap(bitmap, inputSize,inputSize);
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(inputSize * inputSize * 3 * 4);

        inputBuffer.order(ByteOrder.nativeOrder());
//        inputBuffer.rewind();

        inputBuffer.rewind();

        for (int y = 0; y < inputSize; y++){
            for (int x = 0; x < inputSize; x++){
                int pixelValue = resized.getPixel(x,y);

                inputBuffer.putFloat(((pixelValue >> 16) & 0XFF) / 255.0f);
                inputBuffer.putFloat(((pixelValue >> 8) & 0XFF) / 255.0f);
                inputBuffer.putFloat((pixelValue & 0XFF) / 255.0f);
            }
        }
        inputBuffer.rewind();

        return new byte[][]{inputBuffer.array()};
    }

    public ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(256 * 256 * 3 * 4);
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] pixels = new int[256 * 256];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int pixel = 0;
        for (int i = 0; i < 256; ++i) {
            for (int j = 0; j < 256; ++j) {
                final int val = pixels[pixel++];

                byteBuffer.putFloat(((val >> 16) & 0xFF) / 255.0f);
                byteBuffer.putFloat(((val >> 8) & 0xFF) / 255.0f);
                byteBuffer.putFloat((val & 0xFF) / 255.0f);
            }
        }

        return byteBuffer;
    }


    public ByteBuffer convertBitmapToByteBuffer1(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(256 * 256 * 3 * 4);
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] pixels = new int[256 * 256];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int pixel = 0;
        for (int i = 0; i < 256; ++i) {
            for (int j = 0; j < 256; ++j) {
                final int val = pixels[pixel++];

                byteBuffer.putFloat(((val >> 16) & 0xFF));
                byteBuffer.putFloat(((val >> 8) & 0xFF));
                byteBuffer.putFloat((val & 0xFF));
            }
        }

        return byteBuffer;
    }
}
