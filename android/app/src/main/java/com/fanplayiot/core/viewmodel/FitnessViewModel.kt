package com.fanplayiot.core.ui.home.fanfit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fanplayiot.core.db.local.repository.DateRange
import com.fanplayiot.core.db.local.repository.FitnessRepository
import com.fanplayiot.core.viewmodel.FanFitCommon
import com.fanplayiot.core.viewmodel.HomeViewModel

class FitnessViewModel(val common: FanFitCommon, val homeViewModel: HomeViewModel, val repository: FitnessRepository) : ViewModel() {

    fun getAllSCD(groupBy: DateRange) = repository.getAllFitnessSCD(groupBy, viewModelScope)

    fun getAllHR(groupBy: DateRange) = repository.getAllFitnessHR(groupBy, viewModelScope)
}