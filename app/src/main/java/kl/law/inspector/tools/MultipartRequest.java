package kl.law.inspector.tools;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by yinyy on 2017/8/22.
 * http://blog.csdn.net/jxxfzgy/article/details/44064481
 * http://www.jb51.net/article/96863.htm
 * http://tool.oschina.net/commons
 */

public class MultipartRequest extends Request<String> {
    private String name;
    private File file;
    private Response.Listener<String> listener;
    private ProgressListener progressListener;

    private String BOUNDARY = "----CustomBoundary"; //数据分隔线
    private static final String MULTIPART_FORM_DATA = "multipart/form-data";

    public MultipartRequest(String url, String name, File file, Response.Listener<String> listener, Response.ErrorListener errorListener, Map<String,String> params, ProgressListener progressListener){
        super(Method.POST, url, errorListener);

        VolleyLog.d("MultipartRequest constructor");

        this.file = file;
        this.name = name;
        this.listener = listener;
        this.progressListener = progressListener;

        BOUNDARY = BOUNDARY + Long.toHexString(System.currentTimeMillis());
    }

    @Override
    public String getBodyContentType() {
        VolleyLog.d("MultipartRequest getBodyContent");

        return MULTIPART_FORM_DATA + "; boundary=" + BOUNDARY;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        VolleyLog.d("MultipartRequest getBody");

        if(this.file==null){
            return super.getBody();
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StringBuilder sb = new StringBuilder();
        //第一行
        sb.append("--"+BOUNDARY+"\r\n");
        //第二行
        sb.append("Content-Disposition: form-data; title=\"file\"; filename=\"abc.png\"\r\n");
        //第三行
        sb.append("Content-Type: image/png\r\n");
        //第四行
        sb.append("\r\n");

        try {
            bos.write(sb.toString().getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //第五行
        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(this.file))){
            long read = 0;
            long size = this.file.length();

            int len;
            byte[] buffer = new byte[1024 * 1024];
            while((len=bis.read(buffer))!=-1){
                bos.write(buffer, 0, len);

                if(this.progressListener!=null){
                    read += len;
                    this.progressListener.update(read, size, 1.0*read/size);
                }
            }

            bos.write("\r\n".getBytes("UTF-8"));
        }catch(Exception e){
            e.printStackTrace();
        }

        //结尾行
        try {
            bos.write(("--"+BOUNDARY+"--\r\n").getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bos.toByteArray();
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        VolleyLog.d("MultipartRequest parseNetworkResponse");

        String parse;
        try{
            parse = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        }catch (Exception e){
            parse  = new String(response.data);
            e.printStackTrace();
        }

        return Response.success(parse, HttpHeaderParser.parseCacheHeaders(response));
    }

//    @Override
//    public Map<String, String> getHeaders() throws AuthFailureError {
//        VolleyLog.d("MultipartRequest getHeaders");
//
//        Map<String,String> headers = super.getHeaders();
//
//        if(headers==null || headers.equals(Collections.<String, String>emptyMap())){
//            headers = new HashMap<>();
//        }
//
//        return headers;
//    }

    @Override
    protected void deliverResponse(String response) {
        VolleyLog.d("MultipartRequest deliverResponse");

        listener.onResponse(response);
    }

    public interface ProgressListener{
        void update(long read, long size, double percent);
    }
}
