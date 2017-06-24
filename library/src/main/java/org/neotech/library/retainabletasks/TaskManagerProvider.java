package org.neotech.library.retainabletasks;

/**
 * This interface is in general implemented in Fragments or Activities that own a TaskManager and
 * usually the usage is combined with the {@link TaskManagerLifeCycleProxy} class.
 *
 * Created by Rolf on 3-3-2016.
 *
 * @deprecated This class has been renamed to {@link TaskManagerOwner}!
 */
@Deprecated
public interface TaskManagerProvider extends TaskManagerOwner {
}
