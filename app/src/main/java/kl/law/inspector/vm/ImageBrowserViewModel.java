package kl.law.inspector.vm;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.text.MessageFormat;
import java.util.ArrayList;

import kl.law.inspector.databinding.ActivityImageBrowserBinding;
import uk.co.senab.photoview.PhotoView;

/**
 * Created by yinyy on 2017/11/9.
 */

public class ImageBrowserViewModel extends AbstractViewModel<Activity, ActivityImageBrowserBinding> {
    public ImageBrowserViewModel(Activity owner, ActivityImageBrowserBinding binding) {
        super(owner, binding);
    }

    public void init(final ArrayList<String> files, int position){
        binding.imagePosition.setText(MessageFormat.format("{0} / {1}", position + 1, files.size()));
        final ArrayList<PhotoView> views = new ArrayList<>();
        for(String file : files){
            PhotoView photoView = new PhotoView(context);
            views.add(photoView);
        }

        binding.viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return views.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view==object;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(views.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                PhotoView view = views.get(position);
                container.addView(view);
                CustomDatabindingProperty.loadImage(view, files.get(position)/*.replace("192.168.100.110:9090", "10.0.2.2:8080")*/);

                return views.get(position);
            }
        });
        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                binding.imagePosition.setText(MessageFormat.format("{0} / {1}", position+1,  files.size()));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}
