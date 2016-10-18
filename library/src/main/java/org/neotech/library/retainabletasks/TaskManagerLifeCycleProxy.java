package org.neotech.library.retainabletasks;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import org.neotech.library.retainabletasks.internal.BaseTaskManager;

/**
 * Created by Rolf on 3-3-2016.
 */
public final class TaskManagerLifeCycleProxy {

    private BaseTaskManager fragmentTaskManager;
    private final TaskManagerProvider provider;
    private boolean uiReady = false;

    public TaskManagerLifeCycleProxy(@NonNull TaskManagerProvider provider){
        if(!(provider instanceof Activity || provider instanceof Fragment || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && provider instanceof android.app.Fragment))){
            throw new IllegalArgumentException("The TaskManagerProvider needs to be an instance of android.app.Activity (including the derived support library activities), android.app.Fragment or android.support.v4.app.Fragment!");

            /**
             * Checks if the given provider is not an instance of the support library Activity or Fragment and if the API version is lower than API 11.
             * If so it should throw an exception as on API 11 or below we don't have access to Fragments.
             */
        } else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB && !(provider instanceof FragmentActivity || provider instanceof Fragment)){
            throw new IllegalArgumentException("This library doesn't support API level 11 or lower with the default android.app.Activity or android.app.Fragment class, you should use the android.support.v4.app.FragmentActivity or android.support.v4.app.Fragment!");
        }
        this.provider = provider;
    }

    @MainThread
    public void onStart(){
        uiReady = true;
        ((BaseTaskManager) getTaskManager()).setUIReady(uiReady);
        ((BaseTaskManager) getTaskManager()).attach(provider);
    }

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
            fragmentTaskManager = (BaseTaskManager) TaskManager.getActivityTaskManager(((FragmentActivity) provider).getSupportFragmentManager());
        } else if(provider instanceof Fragment){
            fragmentTaskManager = (BaseTaskManager) TaskManager.getFragmentTaskManager((Fragment) provider);
        } else if(provider instanceof Activity){
            fragmentTaskManager = (BaseTaskManager) TaskManager.getActivityTaskManager(((Activity) provider).getFragmentManager());
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && provider instanceof android.app.Fragment){
            fragmentTaskManager = (BaseTaskManager) TaskManager.getFragmentTaskManager((android.app.Fragment) provider);
        } /* else {
            //This should never happen as the constructor checks everything.
        }
        */
        fragmentTaskManager.setUIReady(uiReady);
        return fragmentTaskManager;
    }
}
