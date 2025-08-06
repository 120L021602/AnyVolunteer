package com.hkucs.groupproject.handler;

import android.util.Log;

import com.hkucs.groupproject.response.MaskTypeResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MaskTypeHandler {
    public static MaskTypeResponse chooseMaskType(String prompt) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        String jsonPrompt = "{\"prompt\": \"" + prompt.replace("\"", "\\\"") + "\"}";
        Log.d("MaskTypeHandler", "Request URL: http://8.138.40.22:5000/project");
        Log.d("MaskTypeHandler", "Request Body: " + jsonPrompt);
        RequestBody body = RequestBody.create(JSON, jsonPrompt);

        Request request = new Request.Builder()
                .url("http://8.138.40.22:5000/project")
                //.header("Connection", "close")
                .post(body)
                .build();

        Log.d("MaskTypeHandler", "sending request");

        String res;
        try (Response httpResponse = client.newCall(request).execute()) {
            ResponseBody httpResponseBody = httpResponse.body();

            if (httpResponseBody == null) {
                Log.e("MaskTypeHandler", "Empty response body");
                return new MaskTypeResponse("Empty response body", false);
            }

            long contentLength = httpResponseBody.contentLength();
            Log.d("MaskTypeHandler", "Response body length: " + contentLength);

            res = httpResponseBody.string();
            Log.d("MaskTypeHandler", "Raw response: " + res);

        } catch (Exception e) {
            Log.e("MaskTypeHandler", "Exception during HTTP request", e);
            throw e;
        }

        JSONObject json = new JSONObject(res);
        float score = (float) json.getDouble("projection_score");
        String interpretation = json.getString("interpretation");

        String maskType;
        if (score > 0.1) {
            maskType = "the_most_lenient";
        } else if (score > 0) {
            maskType = "the_second_most_lenient";
        } else if (score > -0.1) {
            maskType = "the_second_most_strict";
        } else {
            maskType = "the_most_strict";
        }

        Log.d("MaskTypeHandler", "Score: " + score);

        Log.d("MaskTypeHandler", "Interpretation: " + interpretation);
        Log.d("MaskTypeHandler", "MaskType: " + maskType);

        return new MaskTypeResponse("succeed getting mask type from the server.", true, score, interpretation, maskType);
    }
}
