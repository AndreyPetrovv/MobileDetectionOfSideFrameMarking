package org.pytorch.demo.objectdetection;

import android.graphics.Bitmap;
import android.widget.ImageView;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;

import java.util.ArrayList;

public class DetectionModel {
    private Module model;

    private PreProcessor preProcessor;
    private PostProcessor postProcessor;

    public static int inputImageWidth;
    public static int inputImageHeight;

    public static String[] predictedClasses;


    public DetectionModel(Module detectionNetwork, String[] predictedClasses, PreProcessor preProcessor, PostProcessor postProcessor, int inputImageWidth, int inputImageHeight) {
        if (preProcessor == null) {
            preProcessor = new PreProcessor();
        }
        if (postProcessor == null) {
            postProcessor = new PostProcessor();
        }
        this.model = detectionNetwork;
        this.predictedClasses = predictedClasses;

        this.preProcessor = preProcessor;
        this.postProcessor = postProcessor;

        this.inputImageWidth = inputImageWidth;
        this.inputImageHeight = inputImageHeight;
    }


    public ArrayList<DetectionResult> detect(Bitmap image) {
        countScales(image);

        Tensor preprocessedImage = preProcessor.preProcessing(image, inputImageWidth, inputImageHeight);
        IValue[] results = model.forward(IValue.from(preprocessedImage)).toTuple();

        return postProcessor.postProcessing(results, imgScaleX, imgScaleY, startX, startY);
    }

    public ArrayList<ImageCrop> makeImageCrops(ArrayList<DetectionResult> detectionResults, Bitmap image) {
        ArrayList<ImageCrop> imageCrops = new ArrayList<>();

        for (DetectionResult result : detectionResults) {
            int x = result.rect.left;
            int y = result.rect.top;
            int width = result.rect.right - result.rect.left;
            int height = result.rect.bottom - result.rect.top;

            Bitmap imageCrop = Bitmap.createBitmap(image, x, y, width, height);
            imageCrops.add(new ImageCrop(predictedClasses[result.classIndex], "", imageCrop, false));
        }

        return imageCrops;
    }

    private float imgScaleX;
    private float imgScaleY;
    private float startX;
    private float startY;

    public void countScales(Bitmap image) {
        imgScaleX = (float) image.getWidth() / inputImageWidth;
        imgScaleY = (float) image.getHeight() / inputImageHeight;

        //float mIvScaleX = (image.getWidth() > image.getHeight() ? (float) imageView.getWidth() / image.getWidth() : (float) imageView.getHeight() / image.getHeight());
        //float mIvScaleY = (image.getHeight() > image.getWidth() ? (float) imageView.getHeight() / image.getHeight() : (float) imageView.getWidth() / image.getWidth());

        //startX = (imageView.getWidth() - mIvScaleX * image.getWidth()) / 2;
        //startY = (imageView.getHeight() - mIvScaleY * image.getHeight()) / 2;
    }


}
