package com.fanplayiot.core.background;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.fanplayiot.core.db.local.repository.HomeRepository;

import java.util.UUID;

public class PlayersWork extends Worker {
    public static final String TAG = "PlayersWork";
    HomeRepository repository;

    public PlayersWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.repository = new HomeRepository(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            repository.initializeTeam();
            return Result.success();
        } catch (Exception ie) {
            Log.e(TAG, "error " + ie.getMessage(), ie);
        }
        return Result.failure();
    }

    public static UUID startPlayersSync(@NonNull Context context) {
        // Create request
        OneTimeWorkRequest request =
                new OneTimeWorkRequest.Builder(PlayersWork.class)
                        .setConstraints(WorkerHelper.getDefaultConstraints())
                        .addTag(TAG)
                        .build();

        WorkerHelper.addToWorkManager(context, request);
        return request.getId();
    }
}
