package org.neotech.library.retainabletasks.internal;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import org.neotech.library.retainabletasks.TaskManager;

import java.util.HashMap;

/**
 * Created by Rolf Smit on 23-May-17.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class TaskRetainingFragmentLogicLegacy extends TaskRetainingFragmentLogic {

    private final HashMap<String, TaskManager> fragmentTaskManagers = new HashMap<>();

    @MainThread
    public void registerTaskManager(@NonNull String tag, @NonNull TaskManager manager){
        if(fragmentTaskManagers.containsKey(tag)){
            throw new IllegalStateException("A TaskManager is already been registered for the given tag '" + tag + "'.");
        }
        fragmentTaskManagers.put(tag, manager);
    }

    @MainThread
    public TaskManager findTaskManagerByTag(@NonNull String tag){
        return fragmentTaskManagers.get(tag);
    }

    public void assertFragmentTasksAreDetached(){
        if(!TaskManager.isStrictDebugModeEnabled()){
            return;
        }
        for(TaskManager taskManager: fragmentTaskManagers.values()){
            taskManager.assertAllTasksDetached();
        }
    }
}
