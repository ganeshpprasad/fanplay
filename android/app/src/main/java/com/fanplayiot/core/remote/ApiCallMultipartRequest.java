package com.fanplayiot.core.remote;

import android.util.Log;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"CatchMayIgnoreException", "RedundantThrows"})
public class ApiCallMultipartRequest extends Request<NetworkResponse> {
    private static final String CONTENT_DISPOSITION_FORM = "Content-Disposition: form-data; name=\"";
    private static final String STRING_DBLQOUTE = "\"";
    private static final String STRING_FILENAME = "\"; filename=\"";
    private static final String CONTENT_TYPE = "Content-Type: ";
    public static final int MAX_BUFFER_SIZE = 1024 * 1024;
    private static final String MULTIPART_FORM_DATA_BOUNDARY = "multipart/form-data;boundary=";
    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";
    private final String boundary = "apiclient-" + System.currentTimeMillis();

    private final Object mLock;
    @Nullable
    @GuardedBy("mLock")
    private Response.Listener<NetworkResponse> mListener;
    private Response.ErrorListener mErrorListener;
    private String[] mFilePaths;


    private static final String TAG = "ApiCallMultipartRequest";
    public ApiCallMultipartRequest(@NonNull String url, @Nullable String[] list, @NonNull Response.Listener<NetworkResponse> listener, @Nullable Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        this.mLock = new Object();
        this.mListener = listener;
        this.mErrorListener = errorListener;
        this.mFilePaths = list;
    }

    public void cancel() {
        super.cancel();
        synchronized(this.mLock) {
            this.mListener = null;
        }
    }

    @Override
    public String getBodyContentType() {
        return MULTIPART_FORM_DATA_BOUNDARY + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try {
            // populate text payload
            Map<String, String> params = getParams();
            if (params != null && params.size() > 0) {
                textParse(dos, params, getParamsEncoding());
            }

            // populate data byte payload
            Map<String, DataPart> data = getByteData();
            if (data != null && data.size() > 0) {
                dataParse(dos, data);
            }

            // close multipart form data after text and file data
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            //Log.d(TAG, "Method " + getMethod());
            return bos.toByteArray();
        } catch (IOException e) {
            Log.e(TAG, "getBody error: " + e.getMessage(), e);
        }
        return null;
    }

    /*@Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return super.getParams();
    }*/

    /**
     * Custom method handle data payload.
     *
     * @return Map data part label with data byte
     * @throws AuthFailureError AuthFailureError
     */
    protected Map<String, DataPart> getByteData() throws AuthFailureError {
        Map<String, DataPart> params = new HashMap<>();
        if (mFilePaths == null) {
            return null;
        }
        for(String filepath: mFilePaths) {
            File file = new File(filepath);
            Log.d(TAG, "path " + filepath);
            params.put("file", new DataPart(file.getName(), getFileData(file)));
        }

        return params;
    }

    private byte[] getFileData(File file) {
        ByteArrayOutputStream ous = null;
        InputStream ios = null;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            ios = new FileInputStream(file);
            int read;
            while ((read = ios.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
        } catch (IOException fe) {
            // do nothing
        } finally {
            try {
                if (ous != null)
                    ous.close();
            } catch (IOException e) {
            }

            try {
                if (ios != null)
                    ios.close();
            } catch (IOException e) {
            }
        }

        return ous.toByteArray();
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data);
            Log.d(TAG, "response " + json);
            return Response.success(
                    response,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        Response.Listener<NetworkResponse> listener;
        synchronized(this.mLock) {
            listener = this.mListener;
        }
        if (listener != null) {
            listener.onResponse(response);
        }
    }

    @Override
    public void deliverError(VolleyError error) {
        if(mErrorListener != null) mErrorListener.onErrorResponse(error);
    }

    /**
     * Parse string map into data output stream by key and value.
     *
     * @param dataOutputStream data output stream handle string parsing
     * @param params           string inputs collection
     * @param encoding         encode the inputs, default UTF-8
     * @throws IOException IO Exception
     */
    private void textParse(DataOutputStream dataOutputStream, Map<String, String> params, String encoding) throws IOException {
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                buildTextPart(dataOutputStream, entry.getKey(), entry.getValue());
            }
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + encoding, uee);
        }
    }

    /**
     * Parse data into data output stream.
     *
     * @param dataOutputStream data output stream handle file attachment
     * @param data             loop through data
     * @throws IOException IOException
     */
    private void dataParse(DataOutputStream dataOutputStream, Map<String, DataPart> data) throws IOException {
        for (Map.Entry<String, DataPart> entry : data.entrySet()) {
            buildDataPart(dataOutputStream, entry.getValue(), entry.getKey());
        }
    }

    /**
     * Write string data into header and data output stream.
     *
     * @param dataOutputStream data output stream handle string parsing
     * @param parameterName    name of input
     * @param parameterValue   value of input
     * @throws IOException IOException
     */
    private void buildTextPart(DataOutputStream dataOutputStream, String parameterName, String parameterValue) throws IOException {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes(CONTENT_DISPOSITION_FORM + parameterName + STRING_DBLQOUTE + lineEnd);
        dataOutputStream.writeBytes(lineEnd);
        dataOutputStream.writeBytes(parameterValue + lineEnd);
    }

    /**
     * Write data file into header and data output stream.
     *
     * @param dataOutputStream data output stream handle data parsing
     * @param dataFile         data byte as DataPart from collection
     * @param inputName        name of data input
     * @throws IOException IOException
     */
    private void buildDataPart(DataOutputStream dataOutputStream, DataPart dataFile, String inputName) throws IOException {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes(CONTENT_DISPOSITION_FORM +
                inputName + STRING_FILENAME + dataFile.getFileName() + STRING_DBLQOUTE + lineEnd);
        if (dataFile.getType() != null && !dataFile.getType().trim().isEmpty()) {
            dataOutputStream.writeBytes(CONTENT_TYPE + dataFile.getType() + lineEnd);
        }
        dataOutputStream.writeBytes(lineEnd);

        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(dataFile.getContent());
        int bytesAvailable = fileInputStream.available();

        int maxBufferSize = MAX_BUFFER_SIZE;
        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
        byte[] buffer = new byte[bufferSize];

        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

        while (bytesRead > 0) {
            dataOutputStream.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }

        dataOutputStream.writeBytes(lineEnd);
    }

    /**
     * Simple data container use for passing byte file
     */
    @SuppressWarnings("unused")
    public static class DataPart {
        private String fileName;
        private byte[] content;
        private String type;

        /**
         * Default data part
         */
        public DataPart() {
        }

        /**
         * Constructor with data.
         *
         * @param name label of data
         * @param data byte data
         */
        public DataPart(String name, byte[] data) {
            fileName = name;
            content = data;
        }

        /**
         * Constructor with mime data type.
         *
         * @param name     label of data
         * @param data     byte data
         * @param mimeType mime data like "image/jpeg"
         */
        public DataPart(String name, byte[] data, String mimeType) {
            fileName = name;
            content = data;
            type = mimeType;
        }

        /**
         * Getter file name.
         *
         * @return file name
         */
        public String getFileName() {
            return fileName;
        }

        /**
         * Setter file name.
         *
         * @param fileName string file name
         */
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        /**
         * Getter content.
         *
         * @return byte file data
         */
        public byte[] getContent() {
            return content;
        }

        /**
         * Setter content.
         *
         * @param content byte file data
         */
        public void setContent(byte[] content) {
            this.content = content;
        }

        /**
         * Getter mime type.
         *
         * @return mime type
         */
        public String getType() {
            return type;
        }

        /**
         * Setter mime type.
         *
         * @param type mime type
         */
        public void setType(String type) {
            this.type = type;
        }
    }
}
