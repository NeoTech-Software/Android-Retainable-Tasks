package org.neotech.library.retainabletasks;

import javax.annotation.Nullable;

import java.lang.annotation.Annotation;
import java.util.HashMap;

import javax.lang.model.element.Element;

/**
 * TODO documentation
 *
 * Created by Rolf Smit on 24-May-17.
 */
public final class TaskBinding {

    private final HashMap<Class<? extends Annotation>, Element> elementForType = new HashMap<>(6);

    public boolean add(Class<? extends Annotation> annotation, Element element){
        if(elementForType.containsKey(annotation)){
            return false;
        }
        elementForType.put(annotation, element);
        return true;
    }

    public @Nullable
    Element getElementForPostExecute(){
        return elementForType.get(TaskPostExecute.class);
    }
    public @Nullable Element getElementForPreExecute(){
        return elementForType.get(TaskPreExecute.class);
    }

    public @Nullable Element getElementForCancel(){
        return elementForType.get(TaskCancel.class);
    }

    public @Nullable Element getElementForProgress(){
        return elementForType.get(TaskProgress.class);
    }

    public @Nullable Element getElementForAttach(){
        return elementForType.get(TaskAttach.class);
    }
}
