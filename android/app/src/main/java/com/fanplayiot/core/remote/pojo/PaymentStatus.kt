package com.fanplayiot.core.remote.pojo

import com.fanplayiot.core.db.local.entity.json.OrderDetails
import org.json.JSONObject

class PaymentStatus: BaseData<PaymentStatus>() {
    var orderDetails: OrderDetails? = null
    var paymentId: String? = null
    //var errorCode: String? = null
    var message: String? = null

    public override fun getJSONObject(): JSONObject? {
        val jsonObject = JSONObject()
        if (orderDetails == null) {
            return null
        }

        jsonObject.put("orderid", orderDetails!!.orderId)
        jsonObject.put("challengeid", orderDetails!!.challengeId)
        jsonObject.put("packageid", if(orderDetails!!.packageId ?: 0 > 0) orderDetails!!.packageId else 1)
        jsonObject.put("sid", orderDetails!!.sid)
        jsonObject.put("paymentid", if (paymentId != null) paymentId else "")
        jsonObject.put("paymentstatus", if (paymentId != null) "success" else "failure")
        jsonObject.put("paymentmessage", if (message != null) message else "success")
        jsonObject.put("username", orderDetails!!.userName)
        jsonObject.put("useremail", orderDetails!!.email)
        jsonObject.put("usercontact", orderDetails!!.contact)
        jsonObject.put("useraddress", orderDetails!!.address)
        jsonObject.put("userpincode", orderDetails!!.pincode)
        return jsonObject
    }

    public override fun fromJSONObject(jsonObject: JSONObject?): PaymentStatus? {
        TODO("Not yet implemented")
    }

    /*
    {
  "orderid": "string",
  "challengeid": 0,
  "packageid": 0,
  "sid": 0,
  "paymentid": "string",
  "paymentstatus": "string",
  "paymentmessage": "string",
  "username": "string",
  "useremail": "string",
  "usercontact": "string",
  "useraddress": "string",
  "userpincode": "string"
}
     */
}