package org.neotech.app.retainabletasksdemo.activity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import org.neotech.app.retainabletasksdemo.R;
import org.neotech.app.retainabletasksdemo.tasks.CountDownTask;
import org.neotech.library.retainabletasks.Task;
import org.neotech.library.retainabletasks.TaskExecutor;
import org.neotech.library.retainabletasks.providers.TaskActivityCompat;

/**
 * Created by Rolf on 17-3-2016.
 */
public final class DemoActivitySerial extends TaskActivityCompat implements View.OnClickListener, Task.AdvancedCallback {

    private static final String TASK_SERIAL_1 = "serial-1";
    private static final String TASK_SERIAL_2 = "serial-2";
    private static final String TASK_SERIAL_3 = "serial-3";

    private Button buttonOne;
    private Button buttonTwo;
    private Button buttonThree;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_serial);

        buttonOne = (Button) findViewById(R.id.button_serial_task_1);
        buttonOne.setOnClickListener(this);
        buttonTwo = (Button) findViewById(R.id.button_serial_task_2);
        buttonTwo.setOnClickListener(this);
        buttonThree = (Button) findViewById(R.id.button_serial_task_3);
        buttonThree.setOnClickListener(this);
    }

    @Override
    public Task.Callback onPreAttach(@NonNull Task<?, ?> task) {
        switch (task.getTag()){
            case TASK_SERIAL_1:
                setProgress(buttonOne, task);
                break;
            case TASK_SERIAL_2:
                setProgress(buttonTwo, task);
                break;
            case TASK_SERIAL_3:
                setProgress(buttonThree, task);
                break;
        }
        return this;
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if(id == R.id.button_serial_task_1){
            startSerialTask(TASK_SERIAL_1);
        } else if(id == R.id.button_serial_task_2){
            startSerialTask(TASK_SERIAL_2);
        } else if(id == R.id.button_serial_task_3){
            startSerialTask(TASK_SERIAL_3);
        }
        v.setEnabled(false);
    }

    @Override
    public void onPostExecute(Task<?, ?> task) {
        switch (task.getTag()){
            case TASK_SERIAL_1:
                setNormalState(buttonOne, R.string.task_serial_1);
                break;
            case TASK_SERIAL_2:
                setNormalState(buttonTwo, R.string.task_serial_2);
                break;
            case TASK_SERIAL_3:
                setNormalState(buttonThree, R.string.task_serial_3);
                break;
        }
    }

    @Override
    public void onProgressUpdate(Task<?, ?> task, Object progress) {
        switch (task.getTag()){
            case TASK_SERIAL_1:
                setProgress(buttonOne, task);
                break;
            case TASK_SERIAL_2:
                setProgress(buttonTwo, task);
                break;
            case TASK_SERIAL_3:
                setProgress(buttonThree, task);
                break;
        }
    }

    private void startSerialTask(String tag) {
        getTaskManager().execute(new CountDownTask(tag, 10), this, TaskExecutor.SERIAL_EXECUTOR);
    }

    private static void setProgress(Button button, Task task){
        button.setEnabled(false);
        final Integer progress = (Integer) task.getLastKnownProgress();
        if (progress != null) {
            button.setText(String.valueOf(progress));
        }
    }

    private static void setNormalState(Button button, int textResId){
        button.setText(textResId);
        button.setEnabled(true);
    }

    @Override
    public void onCanceled(Task<?, ?> task) {

    }

    @Override
    public void onPreExecute(Task<?, ?> task) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //The Tasks don't necessarily need to finish, cancel them when the activity is finishing.
        if(isFinishing()) {
            getTaskManager().cancel(TASK_SERIAL_1);
            getTaskManager().cancel(TASK_SERIAL_2);
            getTaskManager().cancel(TASK_SERIAL_3);
        }
    }
}
