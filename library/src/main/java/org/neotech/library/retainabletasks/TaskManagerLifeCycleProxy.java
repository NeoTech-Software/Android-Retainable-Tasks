package org.neotech.library.retainabletasks;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import org.neotech.library.retainabletasks.internal.BaseTaskManager;

/**
 * Created by Rolf on 3-3-2016.
 */
public class TaskManagerLifeCycleProxy {

    private BaseTaskManager fragmentTaskManager;
    private final TaskManagerProvider provider;

    public TaskManagerLifeCycleProxy(@NonNull TaskManagerProvider provider){
        if(!(provider instanceof AppCompatActivity || provider instanceof Fragment)){
            throw new IllegalArgumentException("The TaskManagerProvider needs to be an instance of android.support.v7.app.AppCompatActivity, or android.support.v4.app.Fragment.");
        }
        this.provider = provider;
    }

    public void onStart(){
        ((BaseTaskManager) getTaskManager()).attach(provider);
    }

    public void onStop(){
        ((BaseTaskManager) getTaskManager()).detach();
    }

    public TaskManager getTaskManager(){
        if(fragmentTaskManager != null) {
            return fragmentTaskManager;
        }
        if(provider instanceof AppCompatActivity){
            fragmentTaskManager = (BaseTaskManager) TaskManager.getActivityTaskManager(((AppCompatActivity) provider).getSupportFragmentManager());
        } else if(provider instanceof Fragment){
            fragmentTaskManager = (BaseTaskManager) TaskManager.getFragmentTaskManager((Fragment) provider);
        }
        return fragmentTaskManager;
    }
}
