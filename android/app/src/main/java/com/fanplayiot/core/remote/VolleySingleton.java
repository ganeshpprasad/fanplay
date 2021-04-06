package com.fanplayiot.core.remote;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class VolleySingleton {
    private static VolleySingleton mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    private VolleySingleton(Context context) {
        mCtx = context.getApplicationContext();
        mRequestQueue = getRequestQueue();
    }

    public static synchronized VolleySingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleySingleton(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public void addToRequestQueue(StringRequest req) {
        getRequestQueue().add(req);
    }

    public void addJSONRequestToQueue(JsonObjectRequest req) {
        getRequestQueue().add(req);
    }

    public void addJSONArrayRequestToQueue(JsonArrayRequest req) {
        getRequestQueue().add(req);
    }

    /*public void addApiCallToQueue(ApiCallRequest req) {
        getRequestQueue().add(req);
    }*/

    public void addApiCallFileToQueue(ApiCallMultipartRequest req) { getRequestQueue().add(req); }

    public void handleError(VolleyError error) {
        if (error instanceof AuthFailureError) {
            // Session expired

        }
    }

    public void logError(VolleyError error) {
        if (error instanceof NoConnectionError) {
            // No internet
        } else if (error instanceof AuthFailureError) {
            // Session expired
        } else {
            FirebaseCrashlytics.getInstance().recordException(error);
        }
    }
}
