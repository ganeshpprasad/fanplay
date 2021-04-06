package com.fanplayiot.core.remote.firebase.analytics

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object AnalyticsService {
    private const val TAG = "AnalyticsService"
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    fun init(analytics: FirebaseAnalytics) {
        firebaseAnalytics = analytics
    }

    fun logScreenView(screenName: String, className: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, className)
        }
        if (this::firebaseAnalytics.isInitialized) {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
        }
    }

    fun logClickEvents(itemName: String, className: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, itemName)
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Button")
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, className)
        }
        if (this::firebaseAnalytics.isInitialized) {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
        }
    }

    fun logCustomEvents() {
        if (this::firebaseAnalytics.isInitialized) {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.TUTORIAL_COMPLETE, Bundle())
        }
    }
}