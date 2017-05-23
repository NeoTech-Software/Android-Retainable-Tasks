package org.neotech.library.retainabletasks.providers;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import org.neotech.library.retainabletasks.Task;
import org.neotech.library.retainabletasks.TaskManager;
import org.neotech.library.retainabletasks.TaskManagerLifeCycleProxy;
import org.neotech.library.retainabletasks.TaskManagerOwner;

/**
 * <p>
 * A support library {@link Fragment} with support for {@link Task Tasks}. You can use the
 * {@link TaskFragmentCompat#getTaskManager()} method to get the Fragment's {@link TaskManager} and use it
 * to execute Tasks which will automatically be retained across configuration changes by the
 * {@link TaskFragmentCompat}.</p>
 *
 * <p>
 * Each task that's started using the {@link TaskFragmentCompat TaskFragment's} {@link TaskManager} will
 * automatically be retained, this happens during the {@link Fragment#onStart()} method. You will
 * receive calls to the {@link TaskFragmentCompat#onPreAttach(Task)} method for each active {@link Task}
 * and you must return a {@link Task.Callback} listener for each of the Tasks.
 * </p>
 *
 * <p>
 * If you already use an extended version of the {@link Fragment} class you can implement the
 * {@link TaskFragmentCompat TaskFragment's} behaviour yourself using the
 * {@link TaskManagerLifeCycleProxy}.</p>
 *
 * @see AppCompatActivity
 * @see TaskManager
 * @see TaskManagerLifeCycleProxy
 */
public class TaskFragmentCompat extends Fragment implements TaskManagerOwner {

    private final TaskManagerLifeCycleProxy proxy = new TaskManagerLifeCycleProxy(this);

    @Override
    @CallSuper
    public void onStart() {
        super.onStart();
        proxy.onStart();
    }

    @Override
    @CallSuper
    public void onStop() {
        proxy.onStop();
        super.onStop();
    }

    @Override
    public final TaskManager getTaskManager(){
       return proxy.getTaskManager();
    }

    @Override
    public Task.Callback onPreAttach(@NonNull Task<?, ?> task) {
        return null;
    }
}
