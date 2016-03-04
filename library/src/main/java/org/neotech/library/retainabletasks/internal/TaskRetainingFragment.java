package org.neotech.library.retainabletasks.internal;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import org.neotech.library.retainabletasks.TaskManager;

import java.util.HashMap;

/**
 * Created by Rolf on 4-3-2016.
 */
public final class TaskRetainingFragment {

    public static final String FRAGMENT_TAG = "org.neotech.library.retainabletasks.TaskRetainingFragment";

    private final HashMap<String, TaskManager> fragmentTaskManagers = new HashMap<>();

    private final BaseTaskManager taskManager = new BaseTaskManager();

    public static TaskRetainingFragment getInstance(FragmentManager fragmentManager){
        TaskRetainingFragmentCompat taskFragment = (TaskRetainingFragmentCompat) fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        if(taskFragment == null){
            taskFragment = new TaskRetainingFragmentCompat();
        }
        if(!taskFragment.isAdded()) {
            fragmentManager.beginTransaction().add(taskFragment, FRAGMENT_TAG).commit();
        }
        return taskFragment.logic;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static TaskRetainingFragment getInstance(android.app.FragmentManager fragmentManager){
        TaskRetainingFragmentV11 taskFragment = (TaskRetainingFragmentV11) fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        if(taskFragment == null){
            taskFragment = new TaskRetainingFragmentV11();
        }
        if(!taskFragment.isAdded()) {
            fragmentManager.beginTransaction().add(taskFragment, FRAGMENT_TAG).commit();
        }
        return taskFragment.logic;
    }



    @MainThread
    public void registerTaskManager(@NonNull String tag, @NonNull TaskManager manager){
        if(fragmentTaskManagers.containsKey(tag)){
            throw new IllegalStateException("A TaskManager is already been registered for the given tag '" + tag + "'.");
        }
        fragmentTaskManagers.put(tag, manager);
    }

    @MainThread
    public TaskManager findTaskManagerByTag(@NonNull String tag){
        return fragmentTaskManagers.get(tag);
    }

    public BaseTaskManager getActivityTaskManager(){
        return taskManager;
    }
}
