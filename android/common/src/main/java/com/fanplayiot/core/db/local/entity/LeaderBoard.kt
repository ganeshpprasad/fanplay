package com.fanplayiot.core.db.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class LeaderBoard {
    @PrimaryKey
    var id: Int? = null
    var rank = 0
    var name: String? = null
    var latitude: Long = 0
    var longitude: Long = 0
    var points = 0
    var imgpath: String? = null
    var avguserfanemote = 0f
    var avguserhr = 0
    var highestuserfanemote = 0f
    var totaltapcount = 0
    var totalwavecount = 0
    var totalwhistleredeemed = 0
    var highestcheeredplayer: String? = null
}