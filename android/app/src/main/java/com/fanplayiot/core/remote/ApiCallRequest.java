package com.fanplayiot.core.remote;

import android.util.Log;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.fanplayiot.core.remote.pojo.BaseData;
import com.fanplayiot.core.utils.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class ApiCallRequest<R extends BaseData> extends Request<R> {
    private static final String TAG = "ApiCallRequest";
    private Map<String, String> queryParams;
    private Class<R> mClass;
    private final Object mLock;

    @Nullable
    @GuardedBy("mLock")
    private Response.Listener<R> mListener;
    private String tokenId;

    /**
     * Constructor to call when no query params are needed
     *
     * @param path        suffix path to base url for API
     * @param jsonRequest json for POST request
     * @param listener    success listener
     */
    public ApiCallRequest(@NonNull String tokenId, @NonNull String path, @NonNull Class<R> theClass, @Nullable JSONObject jsonRequest,
                          @NonNull Response.Listener<R> listener, @NonNull Response.ErrorListener errorListener) {
        super((jsonRequest == null) ? Method.GET : Method.POST,
                Constant.BASEURL + path, errorListener);
        this.mClass = theClass;
        this.mLock = new Object();
        this.mListener = listener;
    }

    /**
     * @param path        suffix path to base url for API
     * @param queryParams url query params
     * @param jsonRequest json for POST request
     * @param listener    success listener
     */
    public ApiCallRequest(@NonNull String tokenId, @NonNull String path, @NonNull Class<R> theClass, @NonNull Map<String, String> queryParams,
                          @Nullable JSONObject jsonRequest,
                          @NonNull Response.Listener<R> listener, @NonNull Response.ErrorListener errorListener) {
        super((jsonRequest == null) ? Method.GET : Method.POST,
                Constant.BASEURL + path, errorListener);
        this.mLock = new Object();
        this.mClass = theClass;
        this.mListener = listener;
        this.queryParams = queryParams;
        this.tokenId = tokenId;
    }

    public ApiCallRequest(@NonNull String path, @NonNull Class<R> theClass, @NonNull Map<String, String> queryParams,
                          @NonNull Response.Listener<R> listener, @NonNull Response.ErrorListener errorListener) {
        super(Method.GET,
                Constant.BASEURL + path, errorListener);
        this.mLock = new Object();
        this.mClass = theClass;
        this.mListener = listener;
        this.queryParams = queryParams;
    }

    public void cancel() {
        super.cancel();
        synchronized (this.mLock) {
            this.mListener = null;
        }
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> header = new HashMap<>();
        header.put("accept", "text/plain");
        return header;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        if (queryParams != null && !queryParams.isEmpty()) return queryParams;
        return super.getParams();
    }

    @Override
    protected Response<R> parseNetworkResponse(NetworkResponse response) {
        String json;
        try {
            json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            Log.d(TAG, json);

        } catch (UnsupportedEncodingException e) {
            json = new String(response.data);
            Log.d(TAG, json);
        }

        try {
            R object = BaseData.getInstance(mClass, json);
            if (object != null) {
                return Response.success(object,
                        HttpHeaderParser.parseCacheHeaders(response));
            }
        } catch (IllegalAccessException | InstantiationException | JSONException iae) {
            Log.e(TAG, "api call error", iae);
        }
        return Response.error(new VolleyError());
    }

    @Override
    protected void deliverResponse(R response) {
        Response.Listener<R> listener;
        synchronized (this.mLock) {
            listener = this.mListener;
        }

        if (listener != null) {
            listener.onResponse(response);
        }

    }
}

