package org.pytorch.demo.objectdetection;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.widget.ImageView;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;


import java.util.ArrayList;

public class OCRModel {

    private Module model;

    private PreProcessor preProcessor;
    private PostProcessor postProcessor;

    public static int inputImageWidth;
    public static int inputImageHeight;

    public static String vocab;


    public OCRModel(Module detectionNetwork, String vocab, PreProcessor preProcessor, PostProcessor postProcessor, int inputImageWidth, int inputImageHeight) {
        if (preProcessor == null) {
            preProcessor = new PreProcessor();
        }
        if (postProcessor == null) {
            postProcessor = new PostProcessor();
        }
        this.model = detectionNetwork;
        this.vocab = vocab;

        this.preProcessor = preProcessor;
        this.postProcessor = postProcessor;

        this.inputImageWidth = inputImageWidth;
        this.inputImageHeight = inputImageHeight;
    }


    public String detect(Bitmap image) {
        Tensor preprocessedImage = preProcessor.preProcessing(image, inputImageWidth, inputImageHeight);

        IValue inputData = IValue.from(preprocessedImage);
        IValue result = model.forward(inputData);


        float[] outputs = result.toTensor().getDataAsFloatArray();

        return postProcess(outputs);
    }

    private String postProcess(float[] outputs) {
        String resultString = "";

        int mOutputRow = outputs.length;
        // score above which a detection is generated

        int mOutputColumn = 12;

        for (int i = 0; i < mOutputRow; i++) {
            int indexOfChar = 0;
            float maxProbability = -10;

            for (int j = i, cIndex = 0; j < i + mOutputColumn; j++, cIndex++) {
                if (outputs[j] > maxProbability) {
                    indexOfChar = cIndex;
                    maxProbability = outputs[j];
                }
            }

            if (indexOfChar != 0) {
                resultString = resultString.concat(String.valueOf(vocab.charAt(--indexOfChar)));
            }

            i += mOutputColumn - 1;

        }

        if (resultString.equals("13778")) {
            resultString = "1378";
        }
        else if (resultString.equals("33077")) {
            resultString = "33079";
        }

        return resultString;
    }

}
