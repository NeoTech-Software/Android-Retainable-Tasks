package org.neotech.library.retainabletasks;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

/**
 * <p>
 * A regular {@link Fragment} with support for {@link Task Tasks}. You can use the
 * {@link TaskFragment#getTaskManager()} method to get the Fragment's {@link TaskManager} and use it
 * to execute Tasks which will automatically be retained across configuration changes by the
 * {@link TaskFragment}.</p>
 *
 * <p>
 * Each task that's started using the {@link TaskFragment TaskFragment's} {@link TaskManager} will
 * automatically be retained, this happens during the {@link Fragment#onStart()} method. You will
 * receive calls to the {@link TaskFragment#onPreAttach(Task)} method for each active {@link Task}
 * and you must return a {@link Task.Callback} listener for each of the Tasks.
 * </p>
 *
 * <p>
 * If you already use an extended version of the {@link Fragment} class you can implement the
 * {@link TaskFragment TaskFragment's} behaviour yourself using the
 * {@link TaskManagerLifeCycleProxy}.</p>
 *
 * @see AppCompatActivity
 * @see TaskManager
 * @see TaskManagerLifeCycleProxy
 */
public class TaskFragment extends Fragment implements TaskManagerProvider  {

    private final TaskManagerLifeCycleProxy proxy = new TaskManagerLifeCycleProxy(this);

    @Override
    public void onStart() {
        super.onStart();
        proxy.onStart();
    }

    @Override
    public void onStop() {
        proxy.onStop();
        super.onStop();
    }

    @Override
    public TaskManager getTaskManager(){
       return proxy.getTaskManager();
    }

    @Override
    public Task.Callback onPreAttach(@NonNull Task<?, ?> task) {
        return null;
    }
}
