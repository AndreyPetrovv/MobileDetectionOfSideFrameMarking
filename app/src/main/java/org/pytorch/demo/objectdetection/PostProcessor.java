package org.pytorch.demo.objectdetection;

import android.graphics.Rect;

import org.pytorch.IValue;
import org.pytorch.Tensor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class PostProcessor {


    public ArrayList<DetectionResult> postProcessing(IValue[] outputTuple, float imgScaleX, float imgScaleY, float startX, float startY) {
        final Tensor outputTensor = outputTuple[0].toTensor();
        final float[] outputs = outputTensor.getDataAsFloatArray();
        return outputsToNMSPredictions(outputs, imgScaleX, imgScaleY);
    }

    public static ArrayList<DetectionResult> outputsToNMSPredictions(float[] outputs, float imgScaleX, float imgScaleY) {
        ArrayList<DetectionResult> results = new ArrayList<DetectionResult>();
        // as decided by the YOLOv5 model for input image of size 1024*1024 64512
        int mOutputRow = 64512;
        // score above which a detection is generated
        float mThreshold = 0.4f;

        int mOutputColumn = 9;
        for (int i = 0; i < mOutputRow; i++) {
            // left, top, right, bottom, score and 4 class probability
            if (outputs[i * mOutputColumn + 4] > mThreshold) {
                float x = outputs[i * mOutputColumn];
                float y = outputs[i * mOutputColumn + 1];
                float w = outputs[i * mOutputColumn + 2];
                float h = outputs[i * mOutputColumn + 3];

                float left = imgScaleX * (x - w / 2);
                float top = imgScaleY * (y - h / 2);
                float right = imgScaleX * (x + w / 2);
                float bottom = imgScaleY * (y + h / 2);

                float max = outputs[i * mOutputColumn + 5];
                int cls = 0;
                for (int j = 0; j < mOutputColumn - 5; j++) {
                    if (outputs[i * mOutputColumn + 5 + j] > max) {
                        max = outputs[i * mOutputColumn + 5 + j];
                        cls = j;
                    }
                }
                int y1 = (int) (top);
                int y2 = (int) (bottom);
                Rect rect = new Rect((int) (left), y1, (int) (right), y2);
                DetectionResult result = new DetectionResult(cls, outputs[i * 9 + 4], rect);
                results.add(result);
            }
        }
        int mNmsLimit = 15;
        return nonMaxSuppression(results, mNmsLimit, mThreshold);
    }

    /**
     * Removes bounding boxes that overlap too much with other boxes that have
     * a higher score.
     * - Parameters:
     * - boxes: an array of bounding boxes and their scores
     * - limit: the maximum number of boxes that will be selected
     * - threshold: used to decide whether boxes overlap too much
     */
    private static ArrayList<DetectionResult> nonMaxSuppression(ArrayList<DetectionResult> boxes, int limit, float threshold) {

        // Do an argsort on the confidence scores, from high to low.
        Collections.sort(boxes,
                new Comparator<DetectionResult>() {
                    @Override
                    public int compare(DetectionResult o1, DetectionResult o2) {
                        return o1.score.compareTo(o2.score);
                    }
                });

        ArrayList<DetectionResult> selected = new ArrayList<DetectionResult>();
        boolean[] active = new boolean[boxes.size()];
        Arrays.fill(active, true);
        int numActive = active.length;

        // The algorithm is simple: Start with the box that has the highest score.
        // Remove any remaining boxes that overlap it more than the given threshold
        // amount. If there are any boxes left (i.e. these did not overlap with any
        // previous boxes), then repeat this procedure, until no more boxes remain
        // or the limit has been reached.
        boolean done = false;
        for (int i = 0; i < boxes.size() && !done; i++) {
            if (active[i]) {
                DetectionResult boxA = boxes.get(i);
                selected.add(boxA);
                if (selected.size() >= limit) break;

                for (int j = i + 1; j < boxes.size(); j++) {
                    if (active[j]) {
                        DetectionResult boxB = boxes.get(j);
                        if (IOU(boxA.rect, boxB.rect) > threshold) {
                            active[j] = false;
                            numActive -= 1;
                            if (numActive <= 0) {
                                done = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return selected;
    }


    /**
     * Computes intersection-over-union overlap between two bounding boxes.
     */
    public static float IOU(Rect a, Rect b) {
        float areaA = (a.right - a.left) * (a.bottom - a.top);
        if (areaA <= 0.0) return 0.0f;

        float areaB = (b.right - b.left) * (b.bottom - b.top);
        if (areaB <= 0.0) return 0.0f;

        float intersectionMinX = Math.max(a.left, b.left);
        float intersectionMinY = Math.max(a.top, b.top);
        float intersectionMaxX = Math.min(a.right, b.right);
        float intersectionMaxY = Math.min(a.bottom, b.bottom);
        float intersectionArea = Math.max(intersectionMaxY - intersectionMinY, 0) *
                Math.max(intersectionMaxX - intersectionMinX, 0);
        return intersectionArea / (areaA + areaB - intersectionArea);
    }

}
