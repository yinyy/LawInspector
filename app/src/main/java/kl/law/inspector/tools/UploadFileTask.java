package kl.law.inspector.tools;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by yinyy on 2017/9/4.
 */

public class UploadFileTask extends AsyncTask<String, Double, Void>{
    private String url;
    private File file;

    private String boundary = "--CustomBoundary";

    public UploadFileTask(String url, File file) {
        this.url = url;
        this.file = file;

        this.boundary = this.boundary + Long.toHexString(System.currentTimeMillis());
    }

    @Override
    protected Void doInBackground(String... params) {
        HttpURLConnection connection = null;
        int bufferSize = 1024 * 1024;

        try{
            URL u = new URL(this.url);
            connection = (HttpURLConnection) u.openConnection();

            connection.setChunkedStreamingMode(bufferSize);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection","Keep-Alive");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try(DataOutputStream dos = new DataOutputStream(connection.getOutputStream())){
                dos.writeBytes("--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; title=\"file\"; filename=\""+this.file.getName() + "\""+"\r\n");
                dos.writeBytes("Content-Type: image/png" + "\r\n");
                dos.writeBytes("\r\n");

                try(InputStream in = new FileInputStream(this.file)) {
                    long fileLength = this.file.length();
                    long fileRead = 0;
                    int len;
                    byte[] buffer = new byte[bufferSize];
                    while ((len = in.read(buffer)) != -1) {
                        dos.write(buffer, 0, len);

                        fileRead += len;

                        Log.d("TEST", fileRead + " / " + fileLength);

                        publishProgress(1.0 * fileRead / fileLength);
                    }

                    dos.writeBytes("\r\n");
                }catch (Exception ee){
                    ee.printStackTrace();
                }

                dos.writeBytes("--"+boundary+"--\r\n");
            }

            int serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();

            Log.d("TEST", serverResponseCode+" ================= " + serverResponseMessage);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(connection!=null){
                connection.disconnect();
            }
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Double... values) {
        Log.d("TEST", values[0]+"");
    }
}
