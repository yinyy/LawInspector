package kl.law.inspector.vm;

import android.support.v4.app.Fragment;

/**
 * Created by yinyy on 2017/10/31.
 */

public class AbstractFragmentViewModel<T> extends AbstractViewModel<T> {

    protected Fragment fragment;

    public AbstractFragmentViewModel(Fragment fragment, T binding) {
        super(fragment.getContext(), binding);
        this.fragment = fragment;
    }
}
