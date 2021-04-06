package com.fanplayiot.core.ui.home.fanfit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanplayiot.core.db.local.repository.DateRange
import com.fanplayiot.core.db.local.repository.FitnessRepository
import com.fanplayiot.core.viewmodel.FanFitCommon

class HrBpViewModel(val common: FanFitCommon, val repository: FitnessRepository) : ViewModel() {

    fun getAllHR(groupBy: DateRange) = repository.getAllFitnessHR(groupBy, viewModelScope)

    fun getAllBP(groupBy: DateRange) = repository.getAllFitnessBP(groupBy, viewModelScope)
}

