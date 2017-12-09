package kl.law.inspector.tools;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

/**
 * Created by yinyy on 2017/10/12.
 */

public final class NetworkAccessKit {
    public interface Callback<T>{
        int CODE_SUCCESS=0;
        int CODE_INVALID_FILE_TYPE = 40004;
        int CODE_EMPTY_CONTENT = 44004;

        void onSuccess(T data);
        void onFailure(int code, String remark);
        void onError(String message);
    }

    public interface ProgressCallback{
        void onProgress(long bytesWritter, long totalSize);
    }

    public static abstract class DefaultCallback<T> implements Callback<T>{
        @Override
        public void onFailure(int code, String remark) {
            Log.d("TEST", code + ", " + remark);

            handleFailureAndError(remark);
        }

        @Override
        public void onError(String message) {
            Log.d("TEST", message);

            handleFailureAndError(message);
        }

        public void handleFailureAndError(String message){
            //什么也不做
        }
    }

    private static RequestQueue volleyRequest;
    public static RequestQueue getVolleyRequest(Context context){
        if(volleyRequest==null){
            volleyRequest = Volley.newRequestQueue(context);
        }

        return volleyRequest;
    }

    public static void getData(Context context, String url, final Callback callback){
        getData(context, Request.Method.GET, url, callback);
    }

    public static void getData(Context context, int method, String url, final Callback callback) {
        JsonObjectRequest request = new JsonObjectRequest(method, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                int code = response.optInt("code", -1);
                if (code == -1) {
                    callback.onError("系统繁忙，请稍后重试。");
                } else if (code == 0) {
                    String result = response.optString("result", "success");
                    if ("success".equals(result)) {
                        callback.onSuccess(response.opt("data"));
                    } else if (code == 44004) {
                        callback.onFailure(44004, "未查询到数据。");
                    }else {
                        callback.onFailure(0, response.optString("remark"));
                    }
                } else {
                    callback.onFailure(response.optInt("code", -1), response.optString("remark"));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                callback.onError("发生严重错误。");
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(1000 * 30, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = getVolleyRequest(context);
        queue.add(request);
    }

    public static void postData(Context context, String url, JSONObject data, final Callback callback) {
        RequestQueue queue = getVolleyRequest(context);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                int code = response.optInt("code", -1);
                if (code == -1) {
                    callback.onError("系统繁忙，请稍后重试。");
                }else if (code == 0) {
                    String result = response.optString("result", "success");
                    if ("success".equals(result)) {
                        callback.onSuccess(response.opt("data"));
                    } else if (code == 44004) {
                        callback.onFailure(44004, "未查询到数据。");
                    } else {
                        callback.onFailure(0, response.optString("remark"));
                    }
                } else {
                    callback.onFailure(response.optInt("code", -1), response.optString("remark"));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                callback.onError("发生严重错误。");
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(30*1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    private static AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    public static void uploadFile(File file, Callback callback, ProgressCallback progress){
        uploadFile(null, file, callback, progress);
    }

    public static void uploadFile(Map<String, String> data, File file, final Callback callback, final ProgressCallback progress){
        List<File> fileList = new ArrayList<>();
        fileList.add(file);

        uploadFiles(data, fileList, callback, progress);
    }

    public static void uploadFiles(List<File> fileList, Callback callback, ProgressCallback progress){
        uploadFiles(null, fileList, callback, progress);
    }

    public static void uploadFiles(Map<String, String> data, List<File> fileList, final Callback callback, final ProgressCallback progress){
        RequestParams params = new RequestParams();

        if(data!=null){
            for(Map.Entry<String, String> e : data.entrySet()){
                params.put(e.getKey(), e.getValue());
            }
        }

        if(fileList!=null) {
            try{
                params.put("files", fileList.get(0));
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(data==null && fileList==null) {
            callback.onSuccess("sended");
            return;
        }

        asyncHttpClient.post(ApiKit.URL_UPLOAD_FILE, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String response = new String(responseBody, "UTF8");
                    JSONObject jsonObject = new JSONObject(response);

                    int code = jsonObject.optInt("code");
                    if(code==-1){
                        callback.onError("系统繁忙，请稍后重试。");
                    }else if(code==0){
                        String result = jsonObject.optString("result","success");
                        if("success".equals(result)){
                            callback.onSuccess(jsonObject.opt("data"));
                        }else{
                            callback.onFailure(0, jsonObject.optString("remark"));
                        }
                    }else if (code == 44004) {
                        callback.onFailure(44004, "未查询到数据。");
                    } else{
                        callback.onFailure(jsonObject.optInt("code"),  jsonObject.optString("remark"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    callback.onError(e.getMessage());
                }
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);

                if (progress != null) {
                    progress.onProgress(bytesWritten, totalSize);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                error.printStackTrace();
                try {
                    Log.d("TEST", "上传文件失败：" + new String(responseBody, "UTF8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                callback.onError(error.getMessage());
            }
        });
    }
}
