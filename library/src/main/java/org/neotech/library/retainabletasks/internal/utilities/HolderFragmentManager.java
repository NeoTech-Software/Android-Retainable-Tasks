package org.neotech.library.retainabletasks.internal.utilities;

import android.app.Activity;
import android.app.Application;
import androidx.annotation.RestrictTo;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rolf Smit on 22-May-17.
 *
 * Based upon Google's Lifecycle HolderFragment.FragmentHolderManager class, which can be found in
 * the Lifecycle Architecture library.
 *
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public final class HolderFragmentManager<F extends Fragment> {

    private static final String TAG = HolderFragmentManager.class.getSimpleName();

    private final String desiredTagInFragmentManager;
    private final Class<F> holderFragmentClass;

    private final Map<Activity, F> mNotCommittedActivityHolders = new HashMap<>();
    private final Map<Fragment, F> mNotCommittedFragmentHolders = new HashMap<>();


    public HolderFragmentManager(Class<F> holderFragmentClass, String desiredTagInFragmentManager){
        this.desiredTagInFragmentManager = desiredTagInFragmentManager;
        this.holderFragmentClass = holderFragmentClass;
        Log.d(TAG, "Test: " + holderFragmentClass.getName());
    }

    private final Application.ActivityLifecycleCallbacks mActivityCallbacks =
            new EmptyActivityLifecycleCallbacks() {
                @Override
                public void onActivityDestroyed(Activity activity) {
                    F fragment = mNotCommittedActivityHolders.remove(activity);
                    if (fragment != null) {
                        Log.e(TAG, "Failed to save a holder fragment for " + activity + " with type: " + holderFragmentClass.getName());
                    }
                }
            };

    private boolean mActivityCallbacksIsAdded = false;

    private final FragmentManager.FragmentLifecycleCallbacks mParentDestroyedCallback =
            new FragmentManager.FragmentLifecycleCallbacks() {
                @Override
                public void onFragmentDestroyed(FragmentManager fm, Fragment parentFragment) {
                    super.onFragmentDestroyed(fm, parentFragment);
                    F fragment = mNotCommittedFragmentHolders.remove(parentFragment);
                    if (fragment != null) {
                        Log.e(TAG, "Failed to save a holder fragment for " + parentFragment);
                    }
                }
            };

    /**
     * This method must be called when an instance of the given retainable fragment is created!
     *
     * @param fragment the retainable fragment instance that has just been created.
     */
    public void onHolderFragmentCreated(F fragment) {
        Fragment parentFragment = fragment.getParentFragment();
        if (parentFragment != null) {
            mNotCommittedFragmentHolders.remove(parentFragment);
            parentFragment.getFragmentManager().unregisterFragmentLifecycleCallbacks(
                    mParentDestroyedCallback);
        } else {
            mNotCommittedActivityHolders.remove(fragment.getActivity());
        }
    }

    private F findHolderFragment(FragmentManager manager) {
        if (manager.isDestroyed()) {
            throw new IllegalStateException("Can't access ViewModels from onDestroy");
        }

        return (F) manager.findFragmentByTag(desiredTagInFragmentManager);
    }

    private F createHolderFragmentInstance(){
        //noinspection TryWithIdenticalCatches below API 19 catch branches with Reflection exceptions cannot be merged.
        try {
            return holderFragmentClass.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("Could not create an instance of the given holder fragment class, make sure the class has an public empty constructor!", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not create an instance of the given holder fragment class, make sure the class has an public empty constructor!", e);
        }
    }

    private F createHolderFragment(FragmentManager fragmentManager) {
        F holder = createHolderFragmentInstance();
        fragmentManager.beginTransaction().add(holder, desiredTagInFragmentManager).commitAllowingStateLoss();
        return holder;
    }

    public F retainableFragmentFor(FragmentActivity activity) {
        FragmentManager fm = activity.getSupportFragmentManager();
        F holder = findHolderFragment(fm);
        if (holder != null) {
            return holder;
        }

        /*
        It can happen that a previous call to a this method for this specific activity has not been
        committed yet, but we do need the same instance of the retainable fragment here.
         */
        holder = mNotCommittedActivityHolders.get(activity);
        if (holder != null) {
            return holder;
        }

        /*
        Register LifecycleCallbacks, if not yet done, so that the not committed holders can be
        cleared when the activity is destroyed to prevent memory leaks.
         */
        if (!mActivityCallbacksIsAdded) {
            mActivityCallbacksIsAdded = true;
            activity.getApplication().registerActivityLifecycleCallbacks(mActivityCallbacks);
        }
        holder = createHolderFragment(fm);
        mNotCommittedActivityHolders.put(activity, holder);
        return holder;
    }

    public F retainableFragmentFor(Fragment forFragment) {
        FragmentManager fm = forFragment.getChildFragmentManager();
        F holder = findHolderFragment(fm);
        if (holder != null) {
            return holder;
        }

        /*
        It can happen that a previous call to a this method for this specific fragment has not been
        committed yet, but we do need the same instance of the retainable fragment here.
         */
        holder = mNotCommittedFragmentHolders.get(forFragment);
        if (holder != null) {
            return holder;
        }

        /*
        Register LifecycleCallbacks so that the not committed holders can be cleared when the
        fragment is destroyed to prevent memory leaks.
         */
        forFragment.getFragmentManager().registerFragmentLifecycleCallbacks(mParentDestroyedCallback, false);
        holder = createHolderFragment(fm);
        mNotCommittedFragmentHolders.put(forFragment, holder);
        return holder;
    }
}
