package com.fanplayiot.core.db.local.dao

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.*
import com.fanplayiot.core.db.local.entity.*
//import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.Flow

@Dao
abstract class HomeTempDao {
    companion object {
        private const val TAG = "HomeTempDao"
    }
    // Leaderboard related
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(leaderBoard: LeaderBoard?)

    @get:Query("SELECT * FROM LeaderBoard")
    abstract val allLeaderBoardLive: LiveData<Array<LeaderBoard?>?>?

    @Query("DELETE FROM LeaderBoard")
    abstract fun deleteAllLeaderBoard()

    @Transaction
    open fun resetAllLeaderBoard() {
        deleteAllLeaderBoard()
        for (i in 0..14) {
            val item = LeaderBoard()
            item.id = i
            insert(item)
        }
    }

    @Transaction
    open fun refresh(list: Array<LeaderBoard?>) {
        for (item in list) {
            insert(item)
        }
    }

    // Sponsers related
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insert(sponsorData: SponsorData): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun update(sponsorData: SponsorData)

    @Query("SELECT * FROM SponsorData")
    abstract suspend fun getAllAdvertiser(): Array<SponsorData>?

    @Query("SELECT * FROM SponsorData WHERE id = :sponsorId ")
    abstract suspend fun getAdvertiserForId(sponsorId: Int): SponsorData?

    @Query("SELECT id FROM SponsorData")
    abstract fun getAdvertiserIds(): LiveData<List<Int>?>

    @Query("DELETE FROM SponsorData")
    abstract fun deleteAllAdvertiser()

    @Transaction
    open suspend fun insertOrUpdate(sponsorData: SponsorData) {
        val wId = insert(sponsorData)
        if (wId == -1L) update(sponsorData)
    }

    @Transaction
    open suspend fun refresh(list: Array<SponsorData>) {
        for (item in list) {
            insertOrUpdate(item)
            insertOrUpdate(SponsorAnalytics(item.id, item.locationId, 0, "0"))
            try {
                //Picasso.get().load(item.imageUrl).fetch()
            } catch (e: Exception) {
                Log.d(TAG, "error in Sponsor refresh ", e)
                // do nothing
            }
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insert(sponsorAnalytics: SponsorAnalytics): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun update(sponsorAnalytics: SponsorAnalytics)

    @Query("SELECT * FROM SponsorAnalytics")
    abstract fun getAllSponsorAnalytics(): Array<SponsorAnalytics>?

    @Query("SELECT * FROM SponsorAnalytics WHERE id = :sponsorId ")
    abstract suspend fun getSponsorAnalyticsForId(sponsorId: Int): SponsorAnalytics?

    @Query("DELETE FROM SponsorAnalytics")
    abstract fun deleteAllSponsorAnalytics()

    @Transaction
    open suspend fun insertOrUpdate(sponsorAnalytics: SponsorAnalytics) {
        val wId = insert(sponsorAnalytics)
        if (wId == -1L) update(sponsorAnalytics)
    }

    @Transaction
    open suspend fun updateSponsorAnalytics(id: Int, sponsorAnalytics: SponsorAnalytics) {
        val oldAnalytics = getSponsorAnalyticsForId(id)
        if (oldAnalytics != null) {
            update(sponsorAnalytics)
        }
    }

    // Back end API response cache is using Message table
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(messages: Messages?)

    @Delete
    abstract fun delete(messages: Messages?)

    @Delete
    abstract suspend fun deleteMessages(messages: Messages?)

    @Query("SELECT * FROM Messages WHERE id = :id")
    abstract fun getMessageForId(id: Int): Messages?

    @Query("SELECT * FROM Messages WHERE id = :id")
    abstract suspend fun getMessagesForId(id: Int): Messages?

    @Transaction
    open suspend fun insertOrUpdateMessage(messages: Messages) {
        getMessagesForId(messages.id)?.let { old ->
            if (old.textJson == messages.textJson) {
                return
            } else {
                delete(old)
            }
        }
        insert(messages)
    }

    @Transaction
    open fun replaceMessage(old: Messages?, message: Messages?) {
        delete(old)
        insert(message)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(config: ConstantsConfig)

    @Delete
    abstract fun delete(config: ConstantsConfig)

    @Transaction
    open suspend fun insertOrUpdate(newConfig: ConstantsConfig, id: String) {
        val oldConfig = getConstantsConfigForId(id)
        if (oldConfig != null) delete(oldConfig)
        insert(newConfig)
    }

    @Query("SELECT * FROM ConstantsConfig WHERE id = :id")
    abstract fun getConstantsConfigForId(id: String): ConstantsConfig?

    @Query("SELECT * FROM ConstantsConfig WHERE id = :id")
    abstract fun getConstantsConfigForIdAsFlow(id: String): Flow<ConstantsConfig?>

}