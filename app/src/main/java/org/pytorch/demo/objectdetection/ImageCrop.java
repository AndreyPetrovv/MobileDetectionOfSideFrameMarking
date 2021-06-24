package org.pytorch.demo.objectdetection;

import android.graphics.Bitmap;

public class ImageCrop {
    public String cropClass;
    public String ocrValue;
    public final Bitmap image;
    public final boolean isChoice;

    public ImageCrop(String cropClass, String ocrValue, Bitmap image, boolean isChoice) {
        this.cropClass = cropClass;
        this.ocrValue = ocrValue;
        this.image = image;
        this.isChoice = isChoice;
    }
}
