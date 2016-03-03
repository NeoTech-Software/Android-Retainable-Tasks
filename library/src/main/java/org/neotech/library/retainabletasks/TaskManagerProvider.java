package org.neotech.library.retainabletasks;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

/**
 * Created by Rolf on 3-3-2016.
 */
public interface TaskManagerProvider extends TaskManager.TaskAttachListener {

    @MainThread
    TaskManager getTaskManager();

}
