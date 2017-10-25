package kl.law.inspector.vm;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;

import java.util.LinkedList;
import java.util.List;

import kl.law.inspector.R;
import kl.law.inspector.activity.FragmentLegalCase;
import kl.law.inspector.activity.FragmentDocument;
import kl.law.inspector.activity.FragmentProfile;
import kl.law.inspector.activity.FragmentHome;
import kl.law.inspector.databinding.ActivityMainBinding;

/**
 * Created by yinyy on 2017/8/17.
 */

public class MainActivityViewModel {
    private Context context;
    private ActivityMainBinding binding;

    private List<BottomNavigationItem> navigationItems;
    public List<BottomNavigationItem> getNavigationItems(){
        return this.navigationItems;
    }

    private PagerAdapter pagerAdapter;
    public PagerAdapter getPagerAdapter(){
        return this.pagerAdapter;
    }

    private List<Fragment> fragments = new LinkedList<>();

    public MainActivityViewModel(Context context, final ActivityMainBinding binding) {
        this.context = context;
        this.binding = binding;

        this.navigationItems = new LinkedList<>();
        this.navigationItems.add(new BottomNavigationItem(R.drawable.ic_home, "首页"));
        this.navigationItems.add(new BottomNavigationItem(R.drawable.ic_legal_case, "行政执法"));
        this.navigationItems.add(new BottomNavigationItem(R.drawable.ic_document, "公文流转"));
        this.navigationItems.add(new BottomNavigationItem(R.drawable.ic_mine, "我的"));

        fragments.add(FragmentHome.newInstance());
        fragments.add(FragmentLegalCase.newInstance());
        fragments.add(FragmentDocument.newInstance());
        fragments.add(FragmentProfile.newInstance());
        this.pagerAdapter = new FragmentPagerAdapter(((FragmentActivity)context).getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }
        };

        this.binding.navigation.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
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
        this.binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
