package org.neotech.library.retainabletasks;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * Created by Rolf on 29-2-2016.
 */
public class TaskFragment extends Fragment {

    private final BaseTaskManager taskManager = new BaseTaskManager();

    public static TaskManager getInstance(FragmentManager fragmentManager){
        TaskFragment taskFragment = (TaskFragment) fragmentManager.findFragmentByTag("TaskFragment");
        if(taskFragment == null){
            taskFragment = new TaskFragment();
        }
        if(!taskFragment.isAdded()) {
            fragmentManager.beginTransaction().add(taskFragment, "TaskFragment").commit();
        }
        return taskFragment.taskManager;
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
        taskManager.detachListeners();
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

        taskManager.detachListeners();
        super.onDestroy();
    }
}
