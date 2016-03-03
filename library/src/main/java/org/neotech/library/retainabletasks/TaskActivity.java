package org.neotech.library.retainabletasks;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

/**
 * <p>
 * A regular {@link AppCompatActivity} with support for {@link Task Tasks}. You can use the
 * {@link TaskActivity#getTaskManager()} method to get the Activity's {@link TaskManager} and use it
 * to execute Tasks which will automatically be retained across configuration changes by the
 * {@link TaskActivity}.</p>
 *
 * <p>
 * Each task that's started using the {@link TaskActivity TaskActivty's} {@link TaskManager} will
 * automatically be retained, this happens during the {@link AppCompatActivity#onStart()} method.
 * You will receive calls to the {@link TaskActivity#onPreAttach(Task)} method for each active
 * {@link Task} and you must return a {@link Task.Callback} listener for each of the Tasks.
 * </p>
 *
 * <p>
 * If you already use an extended version of the {@link AppCompatActivity} class you can implement
 * the {@link TaskActivity TaskActiity's} behaviour yourself using the
 * {@link TaskManagerLifeCycleProxy}.</p>
 *
 * @see AppCompatActivity
 * @see TaskManagerLifeCycleProxy
 */
public class TaskActivity extends AppCompatActivity implements TaskManagerProvider {

    private TaskManagerLifeCycleProxy proxy = new TaskManagerLifeCycleProxy(this);

    @Override
    protected void onStart() {
        super.onStart();
        proxy.onStart();
    }

    @Override
    protected void onStop() {
        proxy.onStop();
        super.onStop();
    }

    @Override
    public TaskManager getTaskManager() {
        return proxy.getTaskManager();
    }

    @Override
    public Task.Callback onPreAttach(@NonNull Task<?, ?> task) {
        return null;
    }
}
