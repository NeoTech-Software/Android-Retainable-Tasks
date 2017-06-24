package org.neotech.library.retainabletasks.internal;

import android.support.annotation.RestrictTo;

import org.neotech.library.retainabletasks.Task;

/**
 * The generated classes for use with annotations are of this type.
 *
 * Created by Rolf Smit on 26-May-17.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public interface TaskAttachBinding {

    Task.Callback getListenerFor(Task<?, ?> task, boolean isReAttach);

}
