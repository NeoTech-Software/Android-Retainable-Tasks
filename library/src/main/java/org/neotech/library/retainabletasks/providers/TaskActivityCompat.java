package org.neotech.library.retainabletasks.providers;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.neotech.library.retainabletasks.Task;
import org.neotech.library.retainabletasks.TaskManager;
import org.neotech.library.retainabletasks.TaskManagerLifeCycleProxy;
import org.neotech.library.retainabletasks.TaskManagerOwner;

/**
 * <p>
 * A regular {@link AppCompatActivity} with support for {@link Task Tasks}. You can use the
 * {@link TaskActivityCompat#getTaskManager()} method to get the Activity's {@link TaskManager} and use it
 * to execute Tasks which will automatically be retained across configuration changes by the
 * {@link TaskActivityCompat}.</p>
 *
 * <p>
 * Each task that's started using the {@link TaskActivityCompat TaskActivty's} {@link TaskManager} will
 * automatically be retained, this happens during the {@link AppCompatActivity#onStart()} method.
 * You will receive calls to the {@link TaskActivityCompat#onPreAttach(Task)} method for each active
 * {@link Task} and you must return a {@link Task.Callback} listener for each of the Tasks.
 * </p>
 *
 * <p>
 * If you already use an extended version of the {@link AppCompatActivity} class you can implement
 * the {@link TaskActivityCompat TaskActivity's} behaviour yourself using the
 * {@link TaskManagerLifeCycleProxy}.</p>
 *
 * @see AppCompatActivity
 * @see TaskManagerLifeCycleProxy
 */
public abstract class TaskActivityCompat extends AppCompatActivity implements TaskManagerOwner {

    private final TaskManagerLifeCycleProxy proxy = new TaskManagerLifeCycleProxy(this);

    @Override
    @CallSuper
    protected void onStart() {
        super.onStart();
        proxy.onStart();
    }

    @Override
    @CallSuper
    protected void onStop() {
        proxy.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        proxy.onDestroy();
        super.onDestroy();
    }

    @Override
    public final TaskManager getTaskManager() {
        return proxy.getTaskManager();
    }

    public final void bindTaskTarget(Object object){
        proxy.bindTaskTarget(object);
    }

    @Override
    public Task.Callback onPreAttach(@NonNull Task<?, ?> task) {
        return null;
    }
}
