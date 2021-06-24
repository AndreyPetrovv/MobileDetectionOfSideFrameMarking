package org.pytorch.demo.objectdetection;

import android.graphics.Rect;

public class DetectionResult {
    public final int classIndex;
    public final Float score;
    public final Rect rect;

    public DetectionResult(int cls, Float output, Rect rect) {
        this.classIndex = cls;
        this.score = output;
        this.rect = rect;
    }

}
