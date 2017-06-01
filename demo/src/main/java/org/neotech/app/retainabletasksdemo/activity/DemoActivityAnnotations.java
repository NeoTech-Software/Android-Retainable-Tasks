package org.neotech.app.retainabletasksdemo.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.View;

import org.neotech.app.retainabletasksdemo.OnAlertDialogClickListener;
import org.neotech.app.retainabletasksdemo.R;
import org.neotech.library.retainabletasks.*;
import org.neotech.library.retainabletasks.providers.TaskActivityCompat;

/**
 * This demo activity shows how annotations can be used to get task results (instead of using the
 * {@link Task.Callback} interface). By default every object
 * that is an instance of {@link TaskManagerOwner} works with annotations out-of-the-box. However
 * if you wan't a custom object to receive task results you should manually bind that object.
 *
 * Note: only library based classed that implement {@link TaskManagerOwner} are guaranteed to work
 * with annotations out-of-the -box.
 *
 * Created by Rolf Smit on 29-May-17.
 */
public final class DemoActivityAnnotations extends TaskActivityCompat implements View.OnClickListener, OnAlertDialogClickListener {

    private static final String TASK_PROGRESS = "progress-dialog";

    private final ProgressTaskHandler progressTaskHandler = new ProgressTaskHandler(this);

    public DemoActivityAnnotations(){
        bindTaskTarget(progressTaskHandler);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_annotations);
        findViewById(R.id.button_progress_task).setOnClickListener(this);

        if(savedInstanceState == null) {
            // After starting this activity directly start the task.
            progressTaskHandler.execute();
        }
    }

    @Override
    public void onClick(View v) {
        // On click execute the task
        progressTaskHandler.execute();
    }

    @Override
    public void onDialogFragmentClick(DialogFragment fragment, int which) {
        getTaskManager().cancel(TASK_PROGRESS);
    }
}
