package org.neotech.library.retainabletasks.internal;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;

/**
 * Created by Rolf on 4-3-2016.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public final class TaskRetainingFragmentV11 extends Fragment {

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
        logic.assertAllRemoved();
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
