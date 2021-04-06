package com.fanplayiot.core.ui

import android.net.Uri
import android.util.Log
import com.fanplayiot.core.utils.*
import com.fanplayiot.core.viewmodel.HomeViewModel

fun handleDeepLinks(data: Uri?, viewModel: HomeViewModel) {
    if (data != null && data.path != null && data.getBooleanQueryParameter("tab", false)) {
        when(data.getQueryParameter("tab")) {
            "fanemote" ->
                viewModel.setGotoTabPosition(FANENGAGE_TAB_INDEX, 0)
            "fanfit" ->
                viewModel.setGotoTabPosition(FANFIT_TAB_INDEX, 0)
            "fanfit_activity" ->
                viewModel.setGotoTabPosition(FANFIT_TAB_INDEX, 0)
            "fanfit_heartrate" ->
                viewModel.setGotoTabPosition(FANFIT_TAB_INDEX, 1)
            "fan" ->
                viewModel.setGotoTabPosition(PROFILE_TAB_INDEX, 0)
            "fan_edit1" ->
                viewModel.setGotoTabPosition(PROFILE_TAB_INDEX, 1)
            "fan_edit2" ->
                viewModel.setGotoTabPosition(PROFILE_TAB_INDEX, 2)
            "fan_edit3" ->
                viewModel.setGotoTabPosition(PROFILE_TAB_INDEX, 3)
            "fan_edit4" ->
                viewModel.setGotoTabPosition(PROFILE_TAB_INDEX, 4)
            "fanboard" ->
                viewModel.setGotoTabPosition(LEADERBOARD_TAB_INDEX, 0)
            "fanboard_global" ->
                viewModel.setGotoTabPosition(LEADERBOARD_TAB_INDEX, 0)
            "fanboard_fanemote" ->
                viewModel.setGotoTabPosition(LEADERBOARD_TAB_INDEX, 1)
            "fanboard_fanfit" ->
                viewModel.setGotoTabPosition(LEADERBOARD_TAB_INDEX, 3)
            "fansocial" ->
                viewModel.setGotoTabPosition(FANSOCIAL_TAB_INDEX, 0)
            else ->
                viewModel.setGotoTabPosition(FANENGAGE_TAB_INDEX, 0)
        }
    }
}