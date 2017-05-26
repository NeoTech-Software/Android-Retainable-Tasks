package org.neotech.library.retainabletasks;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import com.google.auto.common.MoreElements;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

@SupportedAnnotationTypes("org.neotech.library.retainabletasks.TaskTarget")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class AnnotationsProcessor extends AbstractProcessor {

    private static final String LIBRARY_PACKAGE = "org.neotech.library.retainabletasks";

    private static final ClassName CLASS_TASKMANAGER_TASKATTACHLISTENER = ClassName.get(LIBRARY_PACKAGE, "TaskManager").nestedClass("TaskAttachListener");
    private static final ClassName CLASS_TASK = ClassName.get(LIBRARY_PACKAGE, "Task");
    private static final ClassName CLASS_TASK_CALLBACK = CLASS_TASK.nestedClass("Callback");
    private static final ClassName CLASS_TASK_ADVANCEDCALLBACK = CLASS_TASK.nestedClass("AdvancedCallback");

    private static final ClassName CLASS_TASKMANAGEROWNER = ClassName.get(LIBRARY_PACKAGE, "TaskManagerOwner");


    private static MethodSpec.Builder createOnPreAttachMethod(){
       return MethodSpec.methodBuilder("onPreAttach")
                .addParameter(CLASS_TASK, "task")
                .addModifiers(Modifier.PUBLIC)
                .returns(CLASS_TASK_CALLBACK)
                .addAnnotation(Override.class);
    }



    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
        annotations.add(TaskTarget.class);
        return annotations;
    }
    private final HashMap<TypeElement, TaskBinding> classMap = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("AnnotationsProcessor.process(" + annotations + ")");

        if(annotations.size() != 1){
            return false;
        }

        //note(null, "AnnotationsProcessor called!");

        // for each javax.lang.model.element.Element annotated with the CustomAnnotation
        for (Element element : roundEnv.getElementsAnnotatedWith(TaskTarget.class)) {

            final TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

            TaskBinding binding = classMap.get(enclosingElement);
            System.out.println(enclosingElement);
            if(binding == null){
                binding = new TaskBinding();
                classMap.put(enclosingElement, binding);
            }
            binding.add(element);

        }

        for(Map.Entry<TypeElement, TaskBinding> entry: classMap.entrySet()) {
            System.out.println("Creating java file: " + entry.getKey());
            try {
                createJavaFile(entry.getKey(), entry.getValue()).writeTo(filer);
                note(entry.getKey(), "Created java file for %s", entry.getKey());
            } catch (IOException e) {
                error(entry.getKey(), "Unable to write binding for type %s: %s", entry.getKey(), e.getMessage());
            }

        }


        return false;
    }

    private JavaFile createJavaFile(TypeElement enclosingElement, TaskBinding binding){

        final String packageName = MoreElements.getPackage(enclosingElement).getQualifiedName().toString();
        final String className = enclosingElement.getQualifiedName().toString().substring(packageName.length() + 1).replace('.', '$');
        ClassName bindingClassName = ClassName.get(packageName, className + "_TaskBinding");


        TypeMirror typeMirror = enclosingElement.asType();


        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addParameter(TypeVariableName.get("T"), "target", Modifier.FINAL)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this.target = target").build();



        final MethodSpec.Builder onPreAttachMethod = createOnPreAttachMethod();


        onPreAttachMethod.beginControlFlow("switch(task.getTag())");

        //void onCanceled(Task<?, ?> task);
        //void onProgressUpdate(Task<?, ?> task, Object progress);


        for(Map.Entry<String, TaskMethods> entry: binding.getTaskMethods().entrySet()){

            onPreAttachMethod.addCode("case \"$L\":", entry.getKey());

            final TaskMethods methods = entry.getValue();

            TypeSpec.Builder callbackImplementation = TypeSpec.anonymousClassBuilder("")
                    .addSuperinterface(CLASS_TASK_ADVANCEDCALLBACK);

            MethodSpec.Builder preExecuteMethod = MethodSpec.methodBuilder("onPreExecute")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(CLASS_TASK, "task");

                    if(methods.preExecute != null) {
                        preExecuteMethod.addStatement("target.$L(task)", methods.preExecute.getSimpleName());
                    }
                    callbackImplementation.addMethod(preExecuteMethod.build());


            MethodSpec.Builder postExecuteMethod = MethodSpec.methodBuilder("onPostExecute")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(CLASS_TASK, "task");

            if(methods.postExecute != null) {
                postExecuteMethod.addStatement("target.$L(task)", methods.postExecute.getSimpleName());
            }
            callbackImplementation.addMethod(postExecuteMethod.build());


            MethodSpec.Builder progressUpdateMethod = MethodSpec.methodBuilder("onProgressUpdate")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(CLASS_TASK, "task")
                    .addParameter(TypeName.OBJECT, "object");

            if(methods.progressUpdate != null) {
                progressUpdateMethod.addStatement("target.$L(task, object)", methods.progressUpdate.getSimpleName());
            }
            callbackImplementation.addMethod(progressUpdateMethod.build());


            MethodSpec.Builder canceledMethod = MethodSpec.methodBuilder("onCanceled")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(CLASS_TASK, "task");

            if(methods.cancel != null) {
                canceledMethod.addStatement("target.$L(task)", methods.cancel.getSimpleName());
            }
            callbackImplementation.addMethod(canceledMethod.build());



            if(methods.reattach != null) {
                onPreAttachMethod.addStatement("target.$L(task)", methods.reattach.getSimpleName());
            }
            onPreAttachMethod.addStatement("return $L", callbackImplementation.build());


        }

        onPreAttachMethod.addCode("default:\n");
        onPreAttachMethod.addStatement("return null");

        onPreAttachMethod.endControlFlow();


 /*
        final TaskTarget annotation = element.getAnnotation(TaskTarget.class);

        final String[] taskIds = annotation.taskIds();
        final TaskState state = annotation.value();


        TypeSpec callbackImplementation = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(CLASS_TASK_CALLBACK)
                .addMethod(MethodSpec.methodBuilder("onPreExecute")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(CLASS_TASK, "task")
                        .build())
                .addMethod(MethodSpec.methodBuilder("onPostExecute")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(CLASS_TASK, "task")
                        .addStatement("target.$L(task)", element.getSimpleName())
                        .build())
                .build();


        onPreAttachMethod.beginControlFlow("switch(task.getTag())");
        for(String tag: taskIds){
            onPreAttachMethod.addCode("case \"$L\":", tag);
            onPreAttachMethod.addStatement("return $L", callbackImplementation);
        }
        onPreAttachMethod.addCode("default:\n");
        onPreAttachMethod.addStatement("return null");

        onPreAttachMethod.endControlFlow();

        */

        //onPreAttachMethod.addStatement("return $L", callbackImplementation);


        TypeSpec generatedClass = TypeSpec.classBuilder(bindingClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(TypeVariableName.get("T"), "target", Modifier.FINAL)
                .addMethod(constructor)
                .addTypeVariable(TypeVariableName.get("T", TypeName.get(typeMirror), CLASS_TASKMANAGEROWNER))
                .addSuperinterface(CLASS_TASKMANAGER_TASKATTACHLISTENER)
                .addMethod(onPreAttachMethod.build())
                .build();




        return JavaFile.builder(bindingClassName.packageName(), generatedClass)
                .addFileComment("Generated code from Android Retainable Tasks. Do not modify!")
                .build();
    }

    private void error(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.ERROR, element, message, args);
    }

    private void note(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.NOTE, element, message, args);
    }

    private void printMessage(Diagnostic.Kind kind, Element element, String message, Object[] args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(kind, message, element);
    }
}
