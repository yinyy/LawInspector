package kl.law.inspector.vm;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import java.io.File;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;

import kl.law.inspector.databinding.ActivityDownloadBinding;
import kl.law.inspector.tools.FileKit;

/**
 * Created by yinyy on 2017/11/1.
 */

public class DownloadViewModel extends AbstractViewModel<Fragment, ActivityDownloadBinding> {
    public final ObservableField<String> percent = new ObservableField<>();
    public final ObservableField<String> speed = new ObservableField<>();
    public final ObservableField<String> total = new ObservableField<>();
    public final ObservableInt progress = new ObservableInt();

    private String url;

    public DownloadViewModel(Fragment owner, ActivityDownloadBinding binding) {
        super(owner, binding);
    }

    public void init(String url) {
        this.url = url;
        binding.progressBar.setProgress(0);
    }

    public void start(){
        final DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, MessageFormat.format("zhxzzfxt_{0}.apk", Long.toHexString(System.currentTimeMillis())));
        request.setVisibleInDownloadsUi(false);//不在通知栏显示
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);

        //request.allowScanningByMediaScanner();//允许被扫描
        //request.setVisibleInDownloadsUi(true);//通知栏一直显示
        //request.setTitle("文件下载");
        final long downloadId = downloadManager.enqueue(request);//得到下载文件的唯一id
        final Timer timer = new Timer();

        final AlertDialog downloadDialog = new AlertDialog.Builder(context)
                .setTitle("下载进度")
                .setView(binding.getRoot())
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //取消下载
                        timer.cancel();

                        downloadManager.remove(downloadId);
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("安装", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = downloadManager.getUriForDownloadedFile(downloadId);

                        File file = FileKit.getFile(context, uri);

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                        context.startActivity(intent);
                        //Process.killProcess(Process.myPid());
                    }
                })
                .setCancelable(false)
                .show();
        downloadDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

        final DownloadValue value = new DownloadValue();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);

                Cursor cursor = downloadManager.query(query);
                cursor.moveToFirst();

                long downloaded = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                long size = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                long time = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP));
                cursor.close();

                float fileSize = size * 1.0f / 1024;
                float currentPercent = downloaded * 100.0f / size;

                NumberFormat nf = NumberFormat.getInstance();
                nf.setGroupingUsed(true);
                nf.setMaximumFractionDigits(0);

                total.set(MessageFormat.format("{0}KB", nf.format(fileSize)));
                progress.set((int)currentPercent);

                nf.setGroupingUsed(false);
                nf.setMaximumFractionDigits(2);

                percent.set(MessageFormat.format("{0}%", nf.format(currentPercent)));

                if(value.time==0) {
                    speed.set("0.00KB/s");
                }else {
                    long dt = time - value.time;
                    long ds = downloaded - value.size;

                    if (dt > 0) {
                        String unit = "KB/s";
                        float spd = ds * 1000.0f / dt / 1024;

                        if (spd > 1024) {
                            spd /= 1024;
                            unit = "MB/s";
                        }

                        speed.set(MessageFormat.format("{0}{1}", nf.format(spd), unit));
                    }
                }

                value.time = time;
                value.size = downloaded;

                if (currentPercent >= 100) {
                    timer.cancel();

                    speed.set("-.--KB/s");

                    owner.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            downloadDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                        }
                    });
                }
            }
        }, 0, 200);
    }

    class DownloadValue{
        public long time;
        public long size;
    }
}
