package org.neotech.library.retainabletasks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bind a method to the given task, the method will automatically be called when the Task attaches
 * to the TaskManagerOwner, if the onlyCallOnReAttach method is set to true the method will not be
 * called when the task is added to the TaskManager while the TaskManagerOwner is already in an
 * attached state (for an Activity this is in between onStart and onStop).
 *
 * Created by Rolf Smit on 29-May-17.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface TaskAttach {

    String[] value();

    boolean onlyCallOnReAttach() default false;
}
