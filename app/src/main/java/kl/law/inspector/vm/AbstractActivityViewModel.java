package kl.law.inspector.vm;

import android.app.Activity;

/**
 * Created by yinyy on 2017/10/31.
 */

public class AbstractActivityViewModel<T> extends AbstractViewModel<T>{
    protected Activity activity;

    public AbstractActivityViewModel(Activity activity, T binding) {
        super(activity, binding);

        this.activity = activity;
    }
}
