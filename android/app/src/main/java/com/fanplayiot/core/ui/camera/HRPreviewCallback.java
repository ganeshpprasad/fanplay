package com.fanplayiot.core.ui.camera;

import android.hardware.Camera;

import androidx.annotation.Nullable;

import com.fanplayiot.core.db.local.entity.HeartRate;
import com.fanplayiot.core.foreground.service.FanEngageListener;
import com.fanplayiot.core.foreground.service.FanFitListener;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Camera Preview callback which process the image captured by surface view
 * and if successfully processed after time out beat count is updated into the database
 */
public class HRPreviewCallback implements Camera.PreviewCallback {
    private static CameraUtils.TYPE currentType = CameraUtils.TYPE.GREEN;
    private static final AtomicBoolean processing = new AtomicBoolean(false);
    private static int averageIndex = 0;
    private static final int averageArraySize = 4;
    private static final int[] averageArray = new int[averageArraySize];
    private static int beatsIndex = 0;
    private static final int beatsArraySize = 3;
    private static final int[] beatsArray = new int[beatsArraySize];
    private static double beats = 0;
    private static long startTime = 0;
    private FanEngageListener fanEngageListener = null;
    private FanFitListener fanFitListener = null;

    public void setFanEngageListener(@Nullable FanEngageListener feListener) {
        this.fanEngageListener = feListener;
    }

    public void setFanFitListener(@Nullable FanFitListener ffListener) {
        this.fanFitListener = ffListener;
    }

    public void resetStartTime() {
        startTime = System.currentTimeMillis();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera cam) {
        if (data == null) throw new NullPointerException();
        Camera.Size size = cam.getParameters().getPreviewSize();
        if (size == null) throw new NullPointerException();

        if (!processing.compareAndSet(false, true)) return;

        int width = size.width;
        int height = size.height;

        int imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(), width, height);
        //Log.d(TAG, "imgAvg="+imgAvg);
        if (imgAvg == 0 || imgAvg == 255) {
            processing.set(false);
            return;
        }

        int averageArrayAvg = 0;
        int averageArrayCnt = 0;
        for (int value : averageArray) {
            if (value > 0) {
                averageArrayAvg += value;
                averageArrayCnt++;
            }
        }

        int rollingAverage = (averageArrayCnt > 0) ? (averageArrayAvg / averageArrayCnt) : 0;
        CameraUtils.TYPE newType = currentType;
        if (imgAvg < rollingAverage) {
            newType = CameraUtils.TYPE.RED;
            if (newType != currentType) {
                beats++;
                //Log.d(TAG, "BEAT!! beats="+beats);
            }
        } else if (imgAvg > rollingAverage) {
            newType = CameraUtils.TYPE.GREEN;
        }

        if (averageIndex == averageArraySize) averageIndex = 0;
        averageArray[averageIndex] = imgAvg;
        averageIndex++;

        // Transitioned from one state to another to the same
        if (newType != currentType) {
            currentType = newType;
        }

        long endTime = System.currentTimeMillis();
        double totalTimeInSecs = (endTime - startTime) / 1000d;
        if (totalTimeInSecs >= 20) {
            double bps = (beats / totalTimeInSecs);
            int dpm = (int) (bps * 60d);
            if (dpm < 30 || dpm > 180) {
                resetStartTime();
                beats = 0;
                processing.set(false);
                return;
            }

            // Log.d(TAG,
            // "totalTimeInSecs="+totalTimeInSecs+" beats="+beats);

            if (beatsIndex == beatsArraySize) beatsIndex = 0;
            beatsArray[beatsIndex] = dpm;
            beatsIndex++;

            int beatsArrayAvg = 0;
            int beatsArrayCnt = 0;
            for (int value : beatsArray) {
                if (value > 0) {
                    beatsArrayAvg += value;
                    beatsArrayCnt++;
                }
            }
            int beatsAvg = (beatsArrayAvg / beatsArrayCnt);
            updateHeartRate(beatsAvg);
            resetStartTime();
            beats = 0;
        }
        processing.set(false);
    }

    private void updateHeartRate(int beatsAvg) {
        if (fanEngageListener != null) fanEngageListener.updateHeartRate(beatsAvg, HeartRate.CAMERA);
        if (fanFitListener != null) fanFitListener.updateHeartRate(beatsAvg, HeartRate.CAMERA);
    }
}
