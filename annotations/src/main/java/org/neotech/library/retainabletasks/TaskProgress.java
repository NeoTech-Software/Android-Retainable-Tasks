package org.neotech.library.retainabletasks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TODO documentation
 *
 * Created by Rolf Smit on 29-May-17.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface TaskProgress {

    String[] value();
}
