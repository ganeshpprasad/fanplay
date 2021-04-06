package com.fanplayiot.core.background;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.fanplayiot.core.background.io.JsonFileIO;
import com.fanplayiot.core.db.local.FanplayiotDatabase;
import com.fanplayiot.core.db.local.dao.HomeDao;
import com.fanplayiot.core.db.local.repository.FanEngageRepository;
import com.fanplayiot.core.remote.repository.MainRepository;

import java.io.File;
import java.util.UUID;

public class OfflineSyncWork extends Worker {
    private static final String TAG = "OfflineSyncWork";
    public static final String FE_FILE_NAME = "FanEngage.txt";
    private MainRepository repository;
    private JsonFileIO fanEngagementFile;
    private File feFile;

    public OfflineSyncWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        FanEngageRepository fanEngageRepository = new FanEngageRepository(context);
        FanplayiotDatabase db = FanplayiotDatabase.getDatabase(context);
        HomeDao homeDao = db.homeDao();
        repository = new MainRepository(context, homeDao, fanEngageRepository);
        File internalFileDir = context.getFilesDir();
        feFile = new File(internalFileDir + File.separator + FE_FILE_NAME);
        fanEngagementFile = new JsonFileIO();
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            if (feFile.exists()) {
                repository.postFanEngagementOffline(fanEngagementFile.readJsonArray(feFile));
            }
        } catch (Exception ie) {
            Log.e(TAG, "error " + ie.getMessage(), ie);
        }
        return Result.failure();
    }

    public static UUID startOfflineSyncWork(@NonNull Context context) {
        // Create request
        OneTimeWorkRequest request =
                new OneTimeWorkRequest.Builder(OfflineSyncWork.class)
                        .setConstraints(WorkerHelper.getDefaultConstraints())
                        .build();

        WorkerHelper.addUniqueOneTimeToWorkManager(context, TAG, request);
        return request.getId();
    }
}
/*
[{"hrcount":107,"hrdevicetype":1,"datacollectedts":"2020-10-08T11:47:39Z","teamcheered":1,"tapcounts":17,"wavecounts":35,"whistlesredeemed":13,"whistlecounts":51,"fescore":7.3,"points":5168,"latitude":12.9503969,"longitude":77.5974556,"devicemacid":"00:00:00:00:00:00","hrzoneid":3,"affiliationid":1,"playertapcheer":[],"playerwavecheer":[],"playerwhistleredeemed":[],"sid":14},{"hrcount":47,"hrdevicetype":1,"datacollectedts":"2020-10-08T11:52:01Z","teamcheered":1,"tapcounts":17,"wavecounts":35,"whistlesredeemed":13,"whistlecounts":55,"fescore":3,"points":5545,"latitude":12.9503969,"longitude":77.5974556,"devicemacid":"00:00:00:00:00:00","hrzoneid":1,"affiliationid":1,"playertapcheer":[],"playerwavecheer":[],"playerwhistleredeemed":[],"sid":14},{"hrcount":44,"hrdevicetype":1,"datacollectedts":"2020-10-08T11:52:53Z","teamcheered":1,"tapcounts":17,"wavecounts":35,"whistlesredeemed":13,"whistlecounts":59,"fescore":3,"points":5922,"latitude":12.9503969,"longitude":77.5974556,"devicemacid":"00:00:00:00:00:00","hrzoneid":1,"affiliationid":1,"playertapcheer":[],"playerwavecheer":[],"playerwhistleredeemed":[],"sid":14},{"hrcount":53,"hrdevicetype":1,"datacollectedts":"2020-10-08T11:55:48Z","teamcheered":1,"tapcounts":17,"wavecounts":35,"whistlesredeemed":13,"whistlecounts":63,"fescore":4.7,"points":6329,"latitude":12.9503969,"longitude":77.5974556,"devicemacid":"00:00:00:00:00:00","hrzoneid":1,"affiliationid":1,"playertapcheer":[],"playerwavecheer":[],"playerwhistleredeemed":[],"sid":14}]
 */