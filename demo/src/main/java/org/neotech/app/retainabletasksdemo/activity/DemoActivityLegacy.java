package org.neotech.app.retainabletasksdemo.activity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import org.neotech.app.retainabletasksdemo.R;
import org.neotech.app.retainabletasksdemo.tasks.CountDownTask;
import org.neotech.library.retainabletasks.Task;
import org.neotech.library.retainabletasks.providers.TaskActivity;

/**
 * Created by Rolf on 4-3-2016.
 */
public final class DemoActivityLegacy extends TaskActivity implements View.OnClickListener, Task.AdvancedCallback {

    private static final String TASK_COUNT_DOWN = "count-down";
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        button = new Button(this);
        button.setText(R.string.task_retain_ui_state);
        button.setOnClickListener(this);

        root.addView(button, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        setContentView(root);
    }

    @Override
    public Task.Callback onPreAttach(@NonNull Task<?, ?> task) {
        button.setEnabled(false);
        button.setText(String.valueOf(task.getLastKnownProgress()));
        return this;
    }

    @Override
    public void onClick(View v) {
        CountDownTask task = new CountDownTask(TASK_COUNT_DOWN, 10);
        getTaskManager().execute(task, this);
        button.setEnabled(false);
    }

    @Override
    public void onPreExecute(Task<?, ?> task) {

    }

    @Override
    public void onPostExecute(Task<?, ?> task) {
        button.setEnabled(true);
        button.setText(R.string.task_retain_ui_state);
    }

    @Override
    public void onCanceled(Task<?, ?> task) {

    }

    @Override
    public void onProgressUpdate(Task<?, ?> task, Object progress) {
        button.setText(String.valueOf(progress));
    }
}
