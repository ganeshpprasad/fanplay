package com.fanplayiot.core.remote.firebase.inappmsg

import android.net.Uri
import com.fanplayiot.core.remote.FirebaseService.Companion.ACTION_HR
import com.fanplayiot.core.remote.FirebaseService.Companion.CUSTOM_ACTION
import com.fanplayiot.core.ui.handleDeepLinks
import com.fanplayiot.core.viewmodel.HomeViewModel
import com.google.firebase.inappmessaging.FirebaseInAppMessagingClickListener
import com.google.firebase.inappmessaging.model.Action
import com.google.firebase.inappmessaging.model.InAppMessage

class InAppButtonListener(private val viewModel: HomeViewModel) : FirebaseInAppMessagingClickListener {
    override fun messageClicked(inAppMessage: InAppMessage, action: Action) {

        if (inAppMessage.data?.get(CUSTOM_ACTION) == ACTION_HR ) {
            viewModel.startCameraHr()
        } else if (action.actionUrl?.contains("fanplayiot.com/view?tab=") == true) {
            handleDeepLinks(Uri.parse(action.actionUrl), viewModel)
        }
    }
}