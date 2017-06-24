package org.neotech.library.retainabletasks;

import java.lang.annotation.Annotation;
import java.util.HashMap;

import javax.annotation.Nonnull;
import javax.lang.model.element.Element;

/**
 * TODO documentation
 *
 * Created by Rolf Smit on 24-May-17.
 */
public final class TaskBindingContainer {

    private final HashMap<String, TaskBinding> taskBindings = new HashMap<>();

    public TaskBindingContainer(){

    }

    private @Nonnull TaskBinding getBindingsPerTask(String tag){
        TaskBinding taskMethods = taskBindings.get(tag);
        if(taskMethods == null){
            taskMethods = new TaskBinding();
            taskBindings.put(tag, taskMethods);
        }
        return taskMethods;
    }


    public void add(@Nonnull Element element, @Nonnull Class<? extends Annotation> annotationType){
        final Annotation annotation = element.getAnnotation(annotationType);
        if(annotation == null){
            throw new IllegalArgumentException("Given element does not contain an annotation of the given type!");
        }
        if(annotation instanceof TaskAttach){
            add(((TaskAttach) annotation).value(), annotationType, element);
        } else if(annotation instanceof TaskCancel){
            add(((TaskCancel) annotation).value(), annotationType, element);
        } else if(annotation instanceof  TaskPostExecute){
            add(((TaskPostExecute) annotation).value(), annotationType, element);
        } else if(annotation instanceof TaskPreExecute){
            add(((TaskPreExecute) annotation).value(), annotationType, element);
        } else if(annotation instanceof TaskProgress){
            add(((TaskProgress) annotation).value(), annotationType, element);
        }
    }

    private void add(final String[] taskTags, Class<? extends Annotation> classType, final Element element){
        for(String tag: taskTags) {
            TaskBinding methods = getBindingsPerTask(tag);
            if(!methods.add(classType, element)){
                throw new IllegalArgumentException(String.format("Double annotated %s method found for task with tag %s!", element.getSimpleName(), tag));
            }
        }
    }

    public @Nonnull HashMap<String, TaskBinding> getTaskBindings() {
        return taskBindings;
    }
}
