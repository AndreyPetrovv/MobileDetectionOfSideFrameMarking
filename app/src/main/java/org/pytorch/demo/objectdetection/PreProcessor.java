package org.pytorch.demo.objectdetection;

import android.graphics.Bitmap;

import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

public class PreProcessor {

    public static float[] NO_MEAN_RGB = new float[]{0.0f, 0.0f, 0.0f};
    public static float[] NO_STD_RGB = new float[]{1.0f, 1.0f, 1.0f};

    public Tensor preProcessing(Bitmap image, int width, int height) {
        Bitmap resizedImage = resize(image, width, height);
        return convertBitmapToTensor(resizedImage);
    }

    private Bitmap resize(Bitmap image, int newWidth, int newHeight) {
        return Bitmap.createScaledBitmap(image, newWidth, newHeight, true);
    }

    private Tensor convertBitmapToTensor(Bitmap image) {
        return TensorImageUtils.bitmapToFloat32Tensor(image, NO_MEAN_RGB, NO_STD_RGB);
    }

}
