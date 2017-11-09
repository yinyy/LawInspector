package kl.law.inspector.tools;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;

import kl.law.inspector.R;


/**
 * Created by yinyy on 2017/8/17.
 */

public class DialogKit {
    private static AlertDialog loadingDialog;

    public static synchronized void showLoadingDialog(Context context){
        showLoadingDialog(context,R.layout.loading);
    }

    public static void showLoadingDialog(Context context, int layoutRes){
        if(loadingDialog==null) {
            loadingDialog = new android.app.AlertDialog.Builder(context).setTitle("加载数据...")
                    .setView(LayoutInflater.from(context).inflate(layoutRes, null))
                    .setPositiveButton("确定", null).show();
        }else{
            if(!loadingDialog.isShowing()) {
                loadingDialog.show();
            }
        }
    }

    public static void hideLoadingDialog(){
        if(loadingDialog!=null){
            loadingDialog.hide();
        }
    }

    public static void showMessage(Context context, String message){
        new AlertDialog.Builder(context).setTitle("提示").setMessage(message).setPositiveButton("确定", null).show();
    }
}
