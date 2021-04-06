package com.fanplayiot.core.background;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.fanplayiot.core.db.local.repository.SponsorRepository;
import com.fanplayiot.core.remote.repository.AdvertiserRepository;

import java.util.UUID;

import static java.util.concurrent.TimeUnit.MINUTES;

public class SponsorsWork extends Worker {
    private static final int DEFAULT_INTERVAL = 15; // in minutes
    public static final String TAG = "SponsorsWork";
    AdvertiserRepository repository;

    public SponsorsWork(
            @NonNull Context context,
            @NonNull WorkerParameters parameters) {
        super(context, parameters);
        SponsorRepository dbRepository = new SponsorRepository(context);
        repository = new AdvertiserRepository(context, dbRepository);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            repository.postAnalyticsAndGetSponsors();
            return Result.success();
        } catch (Exception ie) {
            Log.e(TAG, "error " + ie.getMessage(), ie);
        }
        return Result.failure();
    }

    public static UUID startPeriodicSponsorImages(@NonNull Context context, @NonNull int interval) {
        // Create request
        int repeatInterval = (interval > DEFAULT_INTERVAL) ? interval : DEFAULT_INTERVAL;
        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(SponsorsWork.class, repeatInterval, MINUTES)
                        .setConstraints(WorkerHelper.getDefaultConstraints())
                        .addTag(TAG)
                        .build();

        WorkerHelper.addToWorkManager(context, request);
        return request.getId();
    }

    public static UUID startSponsorImages(@NonNull Context context) {
        // Create request
        OneTimeWorkRequest request =
                new OneTimeWorkRequest.Builder(SponsorsWork.class)
                        .setConstraints(WorkerHelper.getDefaultConstraints())
                        .build();

        WorkerHelper.addToWorkManager(context, request);
        return request.getId();
    }

}
