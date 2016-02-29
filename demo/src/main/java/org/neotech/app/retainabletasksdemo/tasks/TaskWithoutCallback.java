package org.neotech.app.retainabletasksdemo.tasks;

import android.content.Context;
import android.os.SystemClock;
import android.widget.Toast;

import org.neotech.library.retainabletasks.Task;

/**
 * Created by Rolf on 29-2-2016.
 */
public class TaskWithoutCallback extends Task<Void, String> {

    private final Context context;

    public TaskWithoutCallback(Context context) {
        super("TaskWithoutCallback");
        this.context = context.getApplicationContext();
    }

    @Override
    protected String doInBackground() {
        SystemClock.sleep(5000);
        return "Result";
    }

    @Override
    protected void onPostExecute() {
        Toast.makeText(context, "Task finished, result: " + getResult(), Toast.LENGTH_SHORT).show();
    }
}
