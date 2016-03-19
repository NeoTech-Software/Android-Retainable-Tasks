package org.neotech.library.retainabletasks.internal;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by Rolf on 29-2-2016.
 */
public final class TaskRetainingFragmentCompat extends Fragment {

    final TaskRetainingFragment logic = new TaskRetainingFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        //Is this extra detach needed? Correct usage of the TaskManagerLifeCycleProxy should be enough.
        logic.getActivityTaskManager().detach();
        logic.assertActivityTasksAreDetached();
    }

    @Override
    public void onDestroy() {
        /**
         * The activity is destroyed, this method WON'T be called when the fragment is being
         * propagated between activity instances.
         */
        logic.assertFragmentTasksAreDetached();
        super.onDestroy();
    }
}
