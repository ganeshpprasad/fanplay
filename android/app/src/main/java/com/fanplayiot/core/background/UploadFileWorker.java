package com.fanplayiot.core.background;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.fanplayiot.core.remote.ApiCallMultipartRequest;
import com.fanplayiot.core.remote.VolleySingleton;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UploadFileWorker extends Worker {
    private static final String TAG = "UploadFileWorker";
    private Context context;
    public static final String INPUT_FILE_PATH = "INPUT_FILE_PATH";
    private static final int DEFAULT_TIMEOUT = 60; // in seconds
    private static final String TEST_URL = "http://seoforworld.com/api/v1/file-upload.php";

    public UploadFileWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();
        try {
            String path = inputData.getString(INPUT_FILE_PATH);
            if (path == null) return Result.failure();
            return uploadFile(path);
        } catch (Exception e) {
            Log.e(TAG, "error in UploadFileWorker doWork " + e.getMessage(), e);
        }
        return Result.failure();
    }

    private Result uploadFile(String path) {
        RequestFuture<NetworkResponse> requestFuture = RequestFuture.newFuture();
        RequestFuture<VolleyError> errorFuture = RequestFuture.newFuture();
        String[] listOfFiles = new String[1];
        listOfFiles[0] = path;
        VolleySingleton.getInstance(context).addApiCallFileToQueue(
                new ApiCallMultipartRequest(TEST_URL, listOfFiles, requestFuture, errorFuture)
        );
        try {
            //VolleyError error = errorFuture.get(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
            //Log.d(TAG, error.getMessage() + "");
            NetworkResponse networkResponse = requestFuture.get(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
            Log.d(TAG, networkResponse.statusCode + "");
            if (networkResponse.statusCode != 200) return Result.failure();
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "error in UploadFileWorker uploadFile " + e.getMessage(), e);
        }
        return Result.failure();
    }

    public static String writeSimpleImage(@NonNull Context context) {
        int width = 40;
        int height = 40;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        canvas.drawRect(0F, 0F, (float) width, (float) height, paint);
        long imagename = System.currentTimeMillis();

        // Write to file
        File internalDir =  context.getFilesDir();
        File file = new File(internalDir, imagename + ".png");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
            fos.write(bos.toByteArray());
            fos.flush();
        } catch (Exception e) {

        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {

                }
            }

                try {
                    bos.close();
                } catch (Exception e) {

                }

        }
        return file.getAbsolutePath();
    }

    private static String writeSimpleFile(@NonNull Context context) {
        File internalDir =  context.getFilesDir();
        File file = new File(internalDir, "SimpleFile.txt");
        if (!file.exists()) {
            try {
                boolean isSucess = file.createNewFile();
                if (!isSucess) return null;
            } catch (Exception e) {
                return null;
            }
        }
        BufferedWriter buf = null;
        try {
            buf = new BufferedWriter(new FileWriter(file, true));
            buf.append("Hello World");
            buf.newLine();
            buf.append("New line");
            buf.newLine();

        } catch (IOException e) {
            Log.e(TAG, "error in UploadFileWorker writeSimpleFile " + e.getMessage(), e);
        } finally {
            try {
                if (buf != null) {
                    buf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file.getAbsolutePath();
    }

    public static UUID uploadSimpleFile(@NonNull Context context) {

        String path = writeSimpleImage(context); //writeSimpleFile(context);
        Log.d(TAG, path + "");
        if (path == null) return null;
        // Create Input
        Data input = new Data.Builder()
                .putString(INPUT_FILE_PATH, path).build();

        // Create request
        OneTimeWorkRequest request =
                new OneTimeWorkRequest.Builder(UploadFileWorker.class)
                        .setInputData(input)
                        .setConstraints(Constraints.NONE)
                        .build();

        WorkManager workManager = WorkManager.getInstance(context);
        workManager.enqueue(request);
        return request.getId();
    }
}
