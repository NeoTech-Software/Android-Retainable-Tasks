package org.neotech.app.retainabletasksdemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.neotech.app.retainabletasksdemo.tasks.CountDownTask;
import org.neotech.app.retainabletasksdemo.tasks.SimpleTask;
import org.neotech.app.retainabletasksdemo.tasks.TaskWithoutCallback;
import org.neotech.library.retainabletasks.Task;
import org.neotech.library.retainabletasks.providers.TaskActivityCompat;
import org.neotech.library.retainabletasks.TaskExecutor;

public class Main extends TaskActivityCompat implements View.OnClickListener, Task.AdvancedCallback, OnAlertDialogClickListener {

    private static final String TASK_RETAIN_UI_STATE = "retain-ui-state";
    private static final String TASK_PROGRESS = "progress-dialog";
    private static final String TASK_SERIAL = "serial-";

    private static final int[] serialTaskText = new int[]{R.string.task_serial_1, R.string.task_serial_2, R.string.task_serial_2};
    private static final Button[] serialTaskButton = new Button[3];


    private static final String DIALOG_PROGRESS = "progress-dialog";

    private ProgressDialog progressDialog;

    private Button retainUserInterfaceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        findViewById(R.id.button_open_fragment_activity).setOnClickListener(this);
        findViewById(R.id.button_open_v11_activity).setOnClickListener(this);

        findViewById(R.id.fab).setOnClickListener(this);
        findViewById(R.id.button_no_ui_task).setOnClickListener(this);
        findViewById(R.id.button_progress_task).setOnClickListener(this);


        ((TextView) findViewById(R.id.text_serial_tasks)).setText(Html.fromHtml(getString(R.string.task_serial)));
        serialTaskButton[0] = (Button) findViewById(R.id.button_serial_task_1);
        serialTaskButton[0].setOnClickListener(this);
        serialTaskButton[1] = (Button) findViewById(R.id.button_serial_task_2);
        serialTaskButton[1].setOnClickListener(this);
        serialTaskButton[2] = (Button) findViewById(R.id.button_serial_task_3);
        serialTaskButton[2].setOnClickListener(this);


        retainUserInterfaceButton = (Button) findViewById(R.id.button_retain_ui_state_task);
        retainUserInterfaceButton.setOnClickListener(this);
    }

    @Override
    public Task.Callback onPreAttach(@NonNull Task<?, ?> task) {
        if(task.getTag().equals(TASK_RETAIN_UI_STATE)){
            /**
             * the onPreAttach method will only be called if the task did not deliver its result
             * and thus is still available/referenced by the TaskManger.
             *
             * At this point the UI can be restored to the "task is running" state.
             */
            if (!task.isResultDelivered()) { //This call isn't necessary.
                retainUserInterfaceButton.setEnabled(false);
                retainUserInterfaceButton.setText("" + task.getLastKnownProgress());
            }
        } else if(task.getTag().equals(TASK_PROGRESS)){
            progressDialog = ProgressDialog.getExistingInstance(getSupportFragmentManager(), DIALOG_PROGRESS);
        } else if(task.getTag().startsWith(TASK_SERIAL)){
            onPreAttachSerialTask(task);
        }
        return this;
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if(id == R.id.fab){
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/NeoTech-Software/Android-Retainable-Tasks")));
        } else if(id == R.id.button_progress_task) {
            if (getTaskManager().isRunning(TASK_PROGRESS)) {
                Toast.makeText(this, R.string.toast_task_already_running, Toast.LENGTH_SHORT).show();
            }
            SimpleTask task = new SimpleTask(TASK_PROGRESS);
            getTaskManager().execute(task, this);

        } else if(id == R.id.button_no_ui_task){
            TaskWithoutCallback task = new TaskWithoutCallback(this);
            TaskExecutor.execute(task);
        } else if(id == R.id.button_retain_ui_state_task){
            CountDownTask task = new CountDownTask(TASK_RETAIN_UI_STATE, 10);
            getTaskManager().execute(task, this);
            retainUserInterfaceButton.setEnabled(false);
        } else if(id == R.id.button_open_fragment_activity){
            startActivity(new Intent(this, ActivityWithFragments.class));
        } else if(id == R.id.button_open_v11_activity){
            startActivity(new Intent(this, ActivityV11.class));
        } else if(id == R.id.button_serial_task_1){
            getTaskManager().execute(new CountDownTask(TASK_SERIAL + 1, 10), this, TaskExecutor.SERIAL_EXECUTOR);
            v.setEnabled(false);
        } else if(id == R.id.button_serial_task_2){
            getTaskManager().execute(new CountDownTask(TASK_SERIAL + 2, 10), this, TaskExecutor.SERIAL_EXECUTOR);
            v.setEnabled(false);
        } else if(id == R.id.button_serial_task_3){
            getTaskManager().execute(new CountDownTask(TASK_SERIAL + 3, 10), this, TaskExecutor.SERIAL_EXECUTOR);
            v.setEnabled(false);
        }
    }

    @Override
    public void onPreExecute(Task<?, ?> task) {
        if(task.getTag().equals(TASK_PROGRESS)) {
            progressDialog = ProgressDialog.showIfNotShowing(getSupportFragmentManager(), DIALOG_PROGRESS);
        }
    }

    @Override
    public void onPostExecute(Task<?, ?> task) {
        if(task.getTag().equals(TASK_PROGRESS)) {
            progressDialog.dismiss();
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.toast_task_finished, getString(R.string.task_progress_dialog)), Snackbar.LENGTH_LONG).show();
        } else if(task.getTag().equals(TASK_RETAIN_UI_STATE)){
            retainUserInterfaceButton.setEnabled(true);
            retainUserInterfaceButton.setText(R.string.task_retain_ui_state);
        } else if(task.getTag().startsWith(TASK_SERIAL)) {
            onPostExecuteSerialTask(task);
        }
    }

    @Override
    public void onCanceled(Task<?, ?> task) {
        if(task.getTag().equals(TASK_PROGRESS)) {
            progressDialog.dismiss();
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.toast_task_canceled, getString(R.string.task_progress_dialog)), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onProgressUpdate(Task<?, ?> task, Object progress) {
        if(task.getTag().equals(TASK_PROGRESS)) {
            progressDialog.setProgress((int) progress);
        } else if(task.getTag().equals(TASK_RETAIN_UI_STATE)){
            retainUserInterfaceButton.setText("" + (int) progress);
        } else if(task.getTag().startsWith(TASK_SERIAL)){
            onProgressUpdateSerialTask(task, (Integer) progress);
        }
    }

    @Override
    public void onDialogFragmentClick(DialogFragment fragment, int which) {
        getTaskManager().cancel(TASK_PROGRESS);
    }



    private int getSerialTaskIndex(String tag){
        return Integer.parseInt(tag.substring(TASK_SERIAL.length())) - 1;
    }

    private void onPreAttachSerialTask(Task<?, ?> task) {
        final int index = getSerialTaskIndex(task.getTag());
        serialTaskButton[index].setEnabled(false);
        final Integer progress = (Integer) task.getLastKnownProgress();
        if (progress != null) {
            serialTaskButton[index].setText("" + progress);
        }
    }

    private void onPostExecuteSerialTask(Task<?, ?> task) {
        final int index = getSerialTaskIndex(task.getTag());
        serialTaskButton[index].setEnabled(false);
        serialTaskButton[index].setText(serialTaskText[index]);
        serialTaskButton[index].setEnabled(true);
    }

    private void onProgressUpdateSerialTask(Task<?, ?> task, int progress){
        final int index = getSerialTaskIndex(task.getTag());
        serialTaskButton[index].setText("" + progress);
    }
}
