package org.neotech.library.retainabletasks.internal;

import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.neotech.library.retainabletasks.TaskManager;

import java.util.HashMap;

/**
 * Created by Rolf on 29-2-2016.
 */
public class TaskRetainingFragment extends Fragment {

    private static final String FRAGMENT_TAG = "org.neotech.library.retainabletasks.TaskRetainingFragment";

    private HashMap<String, TaskManager> fragmentTaskManagers = new HashMap<>();


    private final BaseTaskManager taskManager = new BaseTaskManager();

    public static TaskRetainingFragment getInstance(FragmentManager fragmentManager){
        TaskRetainingFragment taskFragment = (TaskRetainingFragment) fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        if(taskFragment == null){
            taskFragment = new TaskRetainingFragment();
        }
        if(!taskFragment.isAdded()) {
            fragmentManager.beginTransaction().add(taskFragment, FRAGMENT_TAG).commit();
        }
        return taskFragment;
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

    public TaskManager getActivityTaskManager(){
        return taskManager;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        //As soon as the activity is stopped the UI should not be touched.
        taskManager.detach();
    }

    @Override
    public void onDestroy() {
        /**
         * The activity is destroyed, this method WON'T be called when the fragment is being
         * propagated between activity instances.
         *
         * Cleanup, but let the tasks finish as they might do something important.
         * The references to the tasks are lost at this point.
         */

        taskManager.detach();
        super.onDestroy();
    }
}
