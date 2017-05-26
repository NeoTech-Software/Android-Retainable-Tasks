package org.neotech.library.retainabletasks.internal;

import org.neotech.library.retainabletasks.Task;

/**
 * The generated classes for use with annotations are of this type. Currently under construction.
 *
 * Created by Rolf Smit on 26-May-17.
 */
public interface TaskAttachBinding {

    Task.Callback getListenerFor(Task<?, ?> task, boolean isReAttach);

}
