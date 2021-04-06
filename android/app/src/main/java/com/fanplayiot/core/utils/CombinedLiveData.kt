package com.fanplayiot.core.utils

import android.util.Pair
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.fanplayiot.core.db.local.entity.Device

class CombinedLiveData(
        source1: LiveData<Device?>,
        source2: LiveData<Long?>) : MediatorLiveData<Pair<Device, Long>>() {
    init {
        super.addSource(source1) { t ->
            if (t != null) {
                value = Pair.create(t, source2.value)
            }
        }
        super.addSource(source2) { f ->
            if (f != null) {
                value = Pair.create(source1.value, f)
            }
        }
    }
}