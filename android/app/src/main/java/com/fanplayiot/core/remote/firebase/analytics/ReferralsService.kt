package com.fanplayiot.core.remote.firebase.analytics

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.demoproject.BuildConfig
import com.demoproject.R
import com.fanplayiot.core.db.local.repository.UserProfileStorage
import com.fanplayiot.core.ui.SplashScreenViewModel
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLink
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase

class ReferralsService {
    fun createDynamicLink(sid: Long, context: Context): Uri {

        val dynamicLink = Firebase.dynamicLinks.dynamicLink {
            link = Uri.parse(context.getString(R.string.referral_link) + sid.toString())
            domainUriPrefix = "https://" + context.getString(R.string.domain_uri_prefix)
            // Open links with this app on Android
            androidParameters(BuildConfig.APPLICATION_ID) {
                minimumVersion = 35
            }
            // Open links with com.example.ios on iOS
            //iosParameters("com.example.ios") { }
        }
        return dynamicLink.uri
    }

    fun inviteFriends(referrerName: String, invitationLink: Uri, context: Context) {
        // val subject = String.format("%s wants you to try Fanplay IoT!", referrerName)
        val subject = context.getString(R.string.referral_subject, referrerName)
        //val msg = "Let's cheer for players in Fanplay IoT together! Use my referrer link: ${invitationLink.toString()}"
        val msg = context.getString(R.string.referral_message_part1, invitationLink.toString())
        //val msgHtml = String.format("<p>Let's cheer for players in Fanplay IoT together! Use my " +
        //        "<a href=\"%s\">referrer link</a>!</p>", invitationLink.toString())
        val msgHtml = context.getString(R.string.referral_message_part2, invitationLink.toString())
        val intent = Intent(Intent.ACTION_SEND).apply {
            //data = Uri.parse("mailto:") // only email apps should handle this
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, msg)
            putExtra(Intent.EXTRA_HTML_TEXT, msgHtml)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.menu_refer)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
    }

    fun handleDynamicLink(activity: FragmentActivity, intent: Intent, viewModel: SplashScreenViewModel) {
        Firebase.dynamicLinks
                .getDynamicLink(intent)
                .addOnSuccessListener(activity) { pendingDynamicLinkData ->
                    // Get deep link from result (may be null if no link is found)
                    var deepLink: Uri? = null
                    if (pendingDynamicLinkData != null) {
                        deepLink = pendingDynamicLinkData.link
                    }

                    // Handle the deep link.
                    if (deepLink != null && deepLink.getBooleanQueryParameter("sid", false)) {
                        deepLink.getQueryParameter("sid")?.let { sid ->
                            val store = UserProfileStorage(activity.applicationContext)
                            if (!store.isReferred) {
                                viewModel.storeReferrerSid(sid)
                            }
                        }
                        //Log.d(TAG, deepLink.toString())
                    }
                }
                .addOnFailureListener(activity) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }

    }

    companion object {
        private const val TAG = "ReferralsService"
    }
}