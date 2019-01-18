package org.neotech.library.retainabletasks.internal;

import android.os.Bundle;
import androidx.annotation.RestrictTo;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.neotech.library.retainabletasks.internal.utilities.HolderFragmentManager;

/**
 * Created by Rolf on 29-2-2016.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public final class TaskRetainingFragment extends Fragment {

    public static final String FRAGMENT_TAG = "org.neotech.library.retainabletasks.TaskRetainingFragment";

    private static final HolderFragmentManager<TaskRetainingFragment> sHolderFragmentManager = new HolderFragmentManager<>(TaskRetainingFragment.class, FRAGMENT_TAG);

    final TaskRetainingFragmentLogic logic = new TaskRetainingFragmentLogic();

    public TaskRetainingFragment(){
        setRetainInstance(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sHolderFragmentManager.onHolderFragmentCreated(this);
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
        // The activity is destroyed, this method WON'T be called when the fragment is being
        // propagated between activity instances.
        super.onDestroy();
    }

    public static TaskRetainingFragmentLogic holderFragmentFor(FragmentActivity activity) {
        return sHolderFragmentManager.retainableFragmentFor(activity).logic;
    }

    public static TaskRetainingFragmentLogic holderFragmentFor(Fragment fragment) {
        return sHolderFragmentManager.retainableFragmentFor(fragment).logic;
    }
}
