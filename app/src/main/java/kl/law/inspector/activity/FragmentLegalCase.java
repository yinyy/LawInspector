package kl.law.inspector.activity;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import kl.law.inspector.R;
import kl.law.inspector.databinding.FragmentLegalCaseBinding;
import kl.law.inspector.vm.LegalCaseViewModel;

public class FragmentLegalCase extends Fragment {

    private FragmentLegalCaseBinding binding;
    private LegalCaseViewModel viewModel;

    public FragmentLegalCase() {
    }

    public static FragmentLegalCase newInstance() {
        FragmentLegalCase fragment = new FragmentLegalCase();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_legal_case, container, false);
        viewModel = new LegalCaseViewModel(this, binding);
        viewModel.init();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_legal_case, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()){
            case R.id.menu_item_create:
                intent = new Intent(getActivity(), LegalCaseCreateActivity.class);
                startActivityForResult(intent, LegalCaseViewModel.REQUEST_CODE_CREATE);
                break;
            case R.id.menu_item_statistics:
                intent = new Intent(getActivity(), LegalCaseStatisticsActivity.class);
                startActivityForResult(intent, LegalCaseViewModel.REQUEST_CODE_STATISTICS);
                break;
        }

        return  true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode== Activity.RESULT_OK) {
            if(requestCode==LegalCaseViewModel.REQUEST_CODE_CREATE){
                viewModel.refreshLegalCase(0);
            }
        }else{
            Toast.makeText(getContext(), "取消操作", Toast.LENGTH_LONG).show();
        }
    }
}
