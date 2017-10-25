package kl.law.inspector.tools;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

import kl.law.inspector.BR;

/**
 * Created by yinyy on 2017/10/10.
 */

public class SectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static class Element<VM>{
        private int type;
        private VM viewModel;

        public Element(int type){
            this(type, null);
        }

        public Element(int type, VM viewModel){
            this.type = type;
            this.viewModel = viewModel;
        }

        public int getType(){
            return type;
        }

        public VM getViewModel(){
            return viewModel;
        }
    }

    private List<Element> items;
    private Map<Integer, Integer> layouts;

    public SectionAdapter(List<Element> items, Map<Integer, Integer> layouts){
        this.items = items;
        this.layouts = layouts;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder = new ViewHolder( DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), layouts.get(viewType), parent, false).getRoot());
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewDataBinding binding = DataBindingUtil.getBinding(holder.itemView);
        binding.setVariable(BR.viewModel, items.get(position).getViewModel());
        binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        Element item = items.get(position);
        try{
            return item.getType();
        }catch (Exception e){
            e.printStackTrace();
        }

        return super.getItemViewType(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
