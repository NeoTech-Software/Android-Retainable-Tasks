package org.neotech.library.retainabletasks.providers;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import androidx.annotation.NonNull;

import org.neotech.library.retainabletasks.Task;
import org.neotech.library.retainabletasks.TaskManager;
import org.neotech.library.retainabletasks.TaskManagerLifeCycleProxy;
import org.neotech.library.retainabletasks.TaskManagerOwner;

/**
 * <p>
 * A regular (non v4 support) {@link Activity} with support for {@link Task Tasks}. You can use the
 * {@link TaskActivity#getTaskManager()} method to get the Activity's {@link TaskManager} and use it
 * to execute Tasks which will automatically be retained across configuration changes by the
 * {@link TaskActivity}.</p>
 *
 * <p>
 * <strong>Note: this class is only works on API 11 and above!</strong> Use the
 * {@link TaskActivityCompat} class for support down to API level 9.
 * </p>
 *
 * <p>
 * Each task that's started using the {@link TaskActivity TaskActivty's} {@link TaskManager} will
 * automatically be retained, this happens during the {@link Activity#onStart()} method.
 * You will receive calls to the {@link TaskActivity#onPreAttach(Task)} method for each active
 * {@link Task} and you must return a {@link Task.Callback} listener for each of the Tasks.
 * </p>
 *
 * <p>
 * If you already use an extended version of the {@link Activity} class you can implement
 * the {@link TaskActivity TaskActivity's} behaviour yourself using the
 * {@link TaskManagerLifeCycleProxy}.</p>
 *
 * @see TaskActivityCompat
 * @see TaskManagerLifeCycleProxy
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public abstract class TaskActivity extends Activity implements TaskManagerOwner {

    private final TaskManagerLifeCycleProxy proxy = new TaskManagerLifeCycleProxy(this);

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
