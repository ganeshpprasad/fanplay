package com.fanplayiot.core.background;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.List;

public class WorkerHelper {

    public static void addToWorkManager(@NonNull Context context, @NonNull WorkRequest workRequest) {
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.enqueue(workRequest);
    }

    public static void addUniqueOneTimeToWorkManager(@NonNull Context context, @NonNull String uniqueName, @NonNull OneTimeWorkRequest workRequest) {
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, workRequest);
    }

    public static Constraints getDefaultConstraints() {
        // Create Constraints
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build();
        return constraints;
    }

    public static LiveData<List<WorkInfo>> isWorkStarted(@NonNull Context context, @NonNull String tag) {
        return WorkManager.getInstance(context).getWorkInfosByTagLiveData(tag);
    }
}
