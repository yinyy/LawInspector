package kl.law.inspector.tools;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by yinyy on 2017/9/8.
 */

public class UniversallyRecycleViewBindingAdapter extends RecyclerView.Adapter<UniversallyRecycleViewBindingAdapter.RecycleViewHolder>{
    private ObservableArrayList items;
    private int brId;
    private int oddLayoutId = -1;//奇数行
    private int evenLayoutId = -1;//偶数行

    public UniversallyRecycleViewBindingAdapter(ObservableArrayList items, int brId){
        this.items = items;
        this.brId = brId;
    }

    public UniversallyRecycleViewBindingAdapter(ObservableArrayList items, int layoutId, int brId){
        this.items = items;
        this.brId = brId;
        this.oddLayoutId = layoutId;
    }

    public UniversallyRecycleViewBindingAdapter(ObservableArrayList items, int oddLayoutId, int evenLayoutId, int brId){
        this.items = items;
        this.brId = brId;
        this.oddLayoutId = oddLayoutId;//奇数行
        this.evenLayoutId = evenLayoutId;//偶数行
    }

    @Override
    public RecycleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewDataBinding binding;
        if(evenLayoutId!=-1 && oddLayoutId!=-1 ) {
            binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), viewType%2==0?oddLayoutId:evenLayoutId, parent, false);
        }else if(oddLayoutId!=-1 && evenLayoutId==-1){
            binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), oddLayoutId, parent, false);
        }else{
            binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), viewType, parent, false);
        }

        return new RecycleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(RecycleViewHolder holder, int position) {
        holder.binding.setVariable(this.brId, items.get(position));
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (evenLayoutId == -1 && oddLayoutId == -1 && items.get(position) instanceof RecycleViewBindingItem) {
            return ((RecycleViewBindingItem) items.get(position)).getViewType();
        } else {
            return position;
        }
    }

    public static class RecycleViewHolder extends RecyclerView.ViewHolder{

        private ViewDataBinding binding;

        public RecycleViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface RecycleViewBindingItem{
        int getViewType();
    }
}
