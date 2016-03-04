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
        //As soon as the activity is stopped the UI should not be touched.
        logic.getActivityTaskManager().detach();
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

        logic.getActivityTaskManager().detach();
        super.onDestroy();
    }
}
