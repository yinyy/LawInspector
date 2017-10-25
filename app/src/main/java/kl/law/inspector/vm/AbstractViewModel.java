package kl.law.inspector.vm;

import android.content.Context;

/**
 * Created by yinyy on 2017/10/9.
 */

public abstract class AbstractViewModel<T> {
    protected Context context;
    protected T binding;

    public AbstractViewModel(Context context, T binding){
        this.context = context;
        this.binding = binding;
    }

}
