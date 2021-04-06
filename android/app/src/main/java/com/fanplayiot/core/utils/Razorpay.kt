package com.fanplayiot.core.utils

import androidx.fragment.app.FragmentActivity
import com.razorpay.Checkout
import org.json.JSONObject

fun FragmentActivity.checkoutRazorpay(key: String, jsonObject: JSONObject) {
    val checkout = Checkout()
    checkout.setKeyID(key)
    checkout.open(this, jsonObject)
}