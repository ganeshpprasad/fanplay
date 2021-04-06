package com.fanplayiot.core.background;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.fanplayiot.core.db.local.entity.User;
import com.fanplayiot.core.db.local.repository.HomeRepository;
import com.fanplayiot.core.remote.repository.LeaderBoardRepository;

import java.util.UUID;

public class LeaderBoardWork extends Worker {
    public static final String TAG = "LeaderBoardWork";
    HomeRepository repository;
    LeaderBoardRepository leaderBoardRepository;

    public LeaderBoardWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        repository = new HomeRepository(context);
        leaderBoardRepository = new LeaderBoardRepository(context, repository);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
        User user = repository.getUserData();
        if (user != null) {
            leaderBoardRepository.resetAllLeaderBoard();
            return Result.success();
        }
        } catch (Exception ie) {
            Log.e(TAG, "error " + ie.getMessage(), ie);
        }
        return Result.success();
    }

    public static UUID startLeaderBoardWork(@NonNull Context context) {
        // Create request
        OneTimeWorkRequest request =
                new OneTimeWorkRequest.Builder(LeaderBoardWork.class)
                        .setConstraints(WorkerHelper.getDefaultConstraints())
                        .addTag(TAG)
                        .build();

        WorkerHelper.addToWorkManager(context, request);
        return request.getId();
    }
}
