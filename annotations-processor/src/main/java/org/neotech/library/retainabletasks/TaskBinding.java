package org.neotech.library.retainabletasks;

import java.util.HashMap;

import javax.lang.model.element.Element;

/**
 * Created by Rolf Smit on 24-May-17.
 */

public class TaskBinding {

    private HashMap<String, TaskMethods> methodsPerTask = new HashMap<>();


    public TaskBinding(){

    }

    private TaskMethods getTaskMethods(String tag){
        TaskMethods taskMethods = methodsPerTask.get(tag);
        if(taskMethods == null){
            taskMethods = new TaskMethods();
            methodsPerTask.put(tag, taskMethods);
        }
        return taskMethods;
    }

    public void add(Element element){
        final TaskTarget target = element.getAnnotation(TaskTarget.class);

        for(String tag: target.taskIds()){
            TaskMethods methods = getTaskMethods(tag);

            switch (target.value()){
                case POST:
                    assertIsNotSet(methods.postExecute, tag);
                    methods.postExecute = element;
                    break;
                case PRE:
                    assertIsNotSet(methods.preExecute, tag);
                    methods.preExecute = element;
                    break;
                case PROGRESS:
                    assertIsNotSet(methods.progressUpdate, tag);
                    methods.progressUpdate = element;
                    break;
                case CANCELED:
                    assertIsNotSet(methods.cancel, tag);
                    methods.cancel = element;
                    break;
                case ATTACH:
                    assertIsNotSet(methods.attach, tag);
                    methods.attach = element;
                    break;
                case REATTACH:
                    assertIsNotSet(methods.reattach, tag);
                    methods.reattach = element;
                    break;
                case ATTACH_ANY:
                    assertIsNotSet(methods.attach, tag);
                    methods.attach = element;
                    assertIsNotSet(methods.reattach, tag);
                    methods.reattach = element;

            }
        }
    }


    private void assertIsNotSet(Element element, String tag){
        if(element != null){
            throw new IllegalArgumentException(String.format("Double annotated %s method found for task with tag %s!", element.getSimpleName(), tag));
        }
    }

    public HashMap<String, TaskMethods> getTaskMethods() {
        return methodsPerTask;
    }
}
