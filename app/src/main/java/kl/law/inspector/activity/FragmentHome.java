package kl.law.inspector.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kl.law.inspector.R;
import kl.law.inspector.databinding.FragmentHome2Binding;
import kl.law.inspector.vm.Home2ViewModel;

/**
 * Created by yinyy on 2017/9/7.
 */

public class FragmentHome extends Fragment{
    private FragmentHome2Binding binding;
    private Home2ViewModel viewModel;

    public static FragmentHome newInstance(){
        FragmentHome fragmentHome = new FragmentHome();
        return fragmentHome;
    }

    public FragmentHome(){
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home2, container, false);
        viewModel = new Home2ViewModel(getContext(), binding);
        viewModel.init();

        binding.setViewModel(viewModel);

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        viewModel.onResume();
    }
}
