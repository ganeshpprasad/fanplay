package com.fanplayiot.core.ui.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;


import static com.fanplayiot.core.utils.Constant.CAMERA_REQUEST_CODE;

public class CameraUtils {
    public static enum TYPE {
        GREEN, RED
    };


    public static Camera.Size getSmallestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea < resultArea) result = size;
                }
            }
        }

        return result;
    }
    public static boolean checkFlash(@NonNull FragmentActivity activity) {
        return activity.getPackageManager().
                hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

    }
    public static void checkPermission(@NonNull FragmentActivity activity) {
        final boolean hasCameraFlash = activity.getPackageManager().
                hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        boolean isEnabled = hasCameraPermission(activity.getApplicationContext());

        if (hasCameraFlash) {
            if (!isEnabled) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            }
        }
    }

    public static boolean hasCameraPermission(@NonNull Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                           @NonNull int[] grantResults) {
        if (requestCode == CAMERA_REQUEST_CODE && permissions.length > 0) {
            return grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }
}
