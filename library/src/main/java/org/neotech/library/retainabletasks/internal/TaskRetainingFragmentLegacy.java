package org.neotech.library.retainabletasks.internal;

import android.app.Activity;
import android.app.Fragment;
import android.support.annotation.RestrictTo;

import org.neotech.library.retainabletasks.internal.utilities.HolderFragmentManagerLegacy;

/**
 * Created by Rolf on 29-2-2016.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public final class TaskRetainingFragmentLegacy extends Fragment {

    public static final String FRAGMENT_TAG = "org.neotech.library.retainabletasks.TaskRetainingFragment";

    private static final HolderFragmentManagerLegacy<TaskRetainingFragmentLegacy> sHolderFragmentManager = new HolderFragmentManagerLegacy<>(TaskRetainingFragmentLegacy.class, FRAGMENT_TAG);


    final TaskRetainingFragmentLogicLegacy logic = new TaskRetainingFragmentLogicLegacy();

    public TaskRetainingFragmentLegacy(){
        setRetainInstance(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        //Is this extra detach needed? Correct usage of the TaskManagerLifeCycleProxy should be enough.
        logic.getTaskManager().detach();
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

    public static TaskRetainingFragmentLogicLegacy holderFragmentFor(Activity activity) {
        return sHolderFragmentManager.retainableFragmentFor(activity).logic;
    }

    public static TaskRetainingFragmentLogicLegacy holderFragmentFor(Fragment fragment) {
        return sHolderFragmentManager.retainableFragmentFor(fragment).logic;
    }
}
