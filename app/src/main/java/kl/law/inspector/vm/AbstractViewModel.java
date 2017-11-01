package kl.law.inspector.vm;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

/**
 * Created by yinyy on 2017/10/9.
 */

public abstract class AbstractViewModel<O, B> {
    protected Context context;
    protected O owner;
    protected B binding;

    public AbstractViewModel(O owner, B binding){
        this.owner = owner;
        this.binding = binding;

        if(owner instanceof Activity){
            context = (Context) owner;
        }else if(owner instanceof Fragment){
            context = ((Fragment)owner).getContext();
        }else if(owner instanceof Context){
            this.context = context;
        }
    }
}
