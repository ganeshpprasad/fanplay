package com.fanplayiot.core.db.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class EventRecorder {
    @PrimaryKey
    var id: Int? = null
    var data: String? = null
}

@Entity
class Advertiser {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    var imageUrl: String? = null
    var clickUrl: String? = null
}