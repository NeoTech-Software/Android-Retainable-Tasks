package org.neotech.library.retainabletasks;

import java.util.HashMap;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

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
                    assertIsNotSet(methods.postExecute);
                    methods.postExecute = element;
                    break;
                case PRE:
                    assertIsNotSet(methods.preExecute);
                    methods.preExecute = element;
                    break;
                case PROGRESS:
                    assertIsNotSet(methods.progressUpdate);
                    methods.progressUpdate = element;
                    break;
                case CANCELED:
                    assertIsNotSet(methods.cancel);
                    methods.cancel = element;
                    break;
                case REATTACH:
                    assertIsNotSet(methods.reattach);
                    methods.reattach = element;
            }
        }
    }


    private void assertIsNotSet(Element element){
        if(element != null){
            throw new IllegalArgumentException("Double annotated postExecute method found for task with tag %s!");
        }
    }

    public HashMap<String, TaskMethods> getTaskMethods() {
        return methodsPerTask;
    }
}
