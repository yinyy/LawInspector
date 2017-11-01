package kl.law.inspector.vm;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;

import java.util.LinkedList;
import java.util.List;

import kl.law.inspector.R;
import kl.law.inspector.activity.FragmentDocument;
import kl.law.inspector.activity.FragmentHome;
import kl.law.inspector.activity.FragmentLegalCase;
import kl.law.inspector.activity.FragmentProfile;
import kl.law.inspector.databinding.ActivityMainBinding;

/**
 * Created by yinyy on 2017/8/17.
 */

public class MainActivityViewModel extends AbstractViewModel<Activity, ActivityMainBinding>{
    private List<Fragment> fragments = new LinkedList<>();

    public MainActivityViewModel(Activity owner, ActivityMainBinding binding) {
        super(owner, binding);
    }

    public void init(){
        binding.navigation.addItem(new BottomNavigationItem(R.drawable.ic_home, "首页"));
        binding.navigation.addItem(new BottomNavigationItem(R.drawable.ic_legal_case, "行政执法"));
        binding.navigation.addItem(new BottomNavigationItem(R.drawable.ic_document, "公文流转"));
        binding.navigation.addItem(new BottomNavigationItem(R.drawable.ic_mine, "我的"));

        binding.navigation.setFirstSelectedPosition(0)
                .setMode(BottomNavigationBar.MODE_FIXED)
                .setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC)
                .setActiveColor("#ee1619")
                .initialise();

        fragments.add(FragmentHome.newInstance());
        fragments.add(FragmentLegalCase.newInstance());
        fragments.add(FragmentDocument.newInstance());
        fragments.add(FragmentProfile.newInstance());

        binding.viewPager.setOffscreenPageLimit(5);
        binding.viewPager.setAdapter(new FragmentPagerAdapter(((FragmentActivity)context).getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }
        });

        binding.navigation.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                binding.viewPager.setCurrentItem(position);
            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {

            }
        });

        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                binding.navigation.selectTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}
