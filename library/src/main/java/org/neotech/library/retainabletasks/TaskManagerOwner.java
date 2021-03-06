package org.neotech.library.retainabletasks;

import androidx.annotation.MainThread;

/**
 * This interface is in general implemented in Fragments or Activities that own a TaskManager and
 * usually the usage is combined with the {@link TaskManagerLifeCycleProxy} class.
 *
 * Created by Rolf on 23-5-2017.
 */
public interface TaskManagerOwner extends TaskManager.TaskAttachListener {

    @MainThread
    TaskManager getTaskManager();

}
