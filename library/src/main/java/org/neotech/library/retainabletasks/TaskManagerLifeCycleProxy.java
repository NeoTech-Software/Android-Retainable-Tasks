package org.neotech.library.retainabletasks;

import android.annotation.TargetApi;
import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.os.Build;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import org.neotech.library.retainabletasks.internal.BaseTaskManager;

/**
 * Created by Rolf on 3-3-2016.
 */
public final class TaskManagerLifeCycleProxy implements LifecycleObserver {

    private BaseTaskManager fragmentTaskManager;
    private final TaskManagerOwner provider;
    private boolean uiReady = false;

    public TaskManagerLifeCycleProxy(@NonNull TaskManagerOwner provider){
        if(!(provider instanceof FragmentActivity || provider instanceof Activity || provider instanceof Fragment || provider instanceof android.app.Fragment)){
            throw new IllegalArgumentException("The TaskManagerProvider needs to be an instance of android.app.Activity (including the derived support library activities), android.app.Fragment or android.support.v4.app.Fragment!");
        }
        this.provider = provider;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    @MainThread
    public void onStart(){
        uiReady = true;
        ((BaseTaskManager) getTaskManager()).setUIReady(uiReady);
        ((BaseTaskManager) getTaskManager()).attach(provider);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    @MainThread
    public void onStop(){
        uiReady = false;
        ((BaseTaskManager) getTaskManager()).setUIReady(uiReady);
        ((BaseTaskManager) getTaskManager()).detach();
    }

    @MainThread
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public TaskManager getTaskManager(){
        if(fragmentTaskManager != null) {
            return fragmentTaskManager;
        }
        if(provider instanceof FragmentActivity){
            fragmentTaskManager = (BaseTaskManager) TaskManager.getActivityTaskManager((FragmentActivity) provider);
        } else if(provider instanceof Fragment){
            fragmentTaskManager = (BaseTaskManager) TaskManager.getFragmentTaskManager((Fragment) provider);
        } else if(provider instanceof Activity){
            fragmentTaskManager = (BaseTaskManager) TaskManager.getActivityTaskManager((Activity) provider);
        } else if(provider instanceof android.app.Fragment){
            fragmentTaskManager = (BaseTaskManager) TaskManager.getFragmentTaskManager((android.app.Fragment) provider);
        } /* else {
            //This should never happen as the constructor checks everything.
        }
        */
        fragmentTaskManager.setUIReady(uiReady);
        return fragmentTaskManager;
    }
}
