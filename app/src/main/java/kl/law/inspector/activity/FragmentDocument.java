package kl.law.inspector.activity;

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

import kl.law.inspector.R;
import kl.law.inspector.databinding.FragmentDocumentBinding;
import kl.law.inspector.vm.DocumentViewModel;

public class FragmentDocument extends Fragment {
    private DocumentViewModel viewModel;

    public FragmentDocument() {
        // Required empty public constructor
    }

    public static FragmentDocument newInstance() {
        FragmentDocument fragment = new FragmentDocument();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentDocumentBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_document, container, false);
        viewModel = new DocumentViewModel(getActivity(), binding);
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
        inflater.inflate(R.menu.menu_document, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(getActivity(), DocumentCreateActivity.class);

        switch (item.getItemId()){
            case R.id.menu_item_create:
                startActivity(intent);
                break;
        }

        return  true;
    }
}
