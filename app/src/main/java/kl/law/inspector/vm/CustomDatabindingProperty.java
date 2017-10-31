package kl.law.inspector.vm;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import kl.law.inspector.R;

/**
 * Created by yinyy on 2017/9/8.
 */

public class CustomDatabindingProperty {
    //初始化ViewPager
    @BindingAdapter("kindAdapter")
    public static void setPagerAdapter(ViewPager viewPager, PagerAdapter adapter){
        viewPager.setAdapter(adapter);
    }

    /**使用Picasso加载图片
     * .MP4.M4V.3GP.3GPP.3G2.3GPP2.WMV
     *
     * .JPG.JPEG.GIF.PNG.BMP.WBMP
     *
     * @param imageView
     * @param url
     */
    @BindingAdapter("imageUrl")
    public static void loadImage(ImageView imageView, String url) {
        if (url != null) {
            if (url.toLowerCase().startsWith("http")) {
                Picasso.with(imageView.getContext()).load(url).placeholder(R.drawable.ic_default_image).into(imageView);
            } else {
                String ext = url.substring(url.lastIndexOf('.')).trim().toUpperCase();
                if(".JPG.JPEG.GIF.PNG.BMP.WBMP".indexOf(ext)>=0) {
                    imageView.setImageBitmap(BitmapFactory.decodeFile(url));
                }else if(".MP4.M4V.3GP.3GPP.3G2.3GPP2.WMV".indexOf(ext)>=0){
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(url);

                    Bitmap bitmap = mmr.getFrameAtTime();//获取第一帧图像
                    imageView.setImageBitmap(bitmap);
                    mmr.release();//释放资源
                }else{
                    imageView.setImageResource(R.drawable.ic_default_image);
                }
            }
        }
    }

//    @BindingAdapter("progress2")
//    public static void setProgress(ProgressBar progressBar, long percent){
//        progressBar.setProgress((int)percent);
//    }

    @BindingAdapter("documentAdapter")
    public static void setAdapter(RecyclerView view, RecyclerView.Adapter adapter){
        view.setAdapter(adapter);
    }

    @BindingAdapter("android:src")
    public static void setImageRes(ImageView image, int resId){
        image.setImageResource(resId);
    }

    @BindingAdapter("android:src")
    public static void setImageDrawable(ImageView image, Drawable drawable){
        image.setImageDrawable(drawable);
    }
}
