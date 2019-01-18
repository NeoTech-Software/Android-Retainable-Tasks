package org.neotech.library.retainabletasks.internal;

import androidx.annotation.RestrictTo;

import org.neotech.library.retainabletasks.TaskManager;

/**
 * Created by Rolf on 4-3-2016.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class TaskRetainingFragmentLogic {

    private final BaseTaskManager taskManager = new BaseTaskManager();

    public BaseTaskManager getTaskManager(){
        return taskManager;
    }

    public void assertActivityTasksAreDetached(){
        if(!TaskManager.isStrictDebugModeEnabled()){
            return;
        }
        taskManager.assertAllTasksDetached();
    }
}
