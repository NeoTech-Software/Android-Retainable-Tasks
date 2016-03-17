package org.neotech.app.retainabletasksdemo.tasks;

import android.os.SystemClock;
import android.util.Log;

import org.neotech.library.retainabletasks.Task;

/**
 * Created by Rolf on 3-3-2016.
 */
public class CountDownTask extends Task<Integer, Void> {

    private final int amount;

    public CountDownTask(String tag, int amount) {
        super(tag);
        this.amount = amount;
    }

    @Override
    protected Void doInBackground() {
        Log.i("CountDownTask", "Task '" + getTag() + "' started!");
        for(int i = amount; i > 0; i--){
            if(isCancelled()){
                Log.i("CountDownTask", "Task '" + getTag() + "' has been cancelled!");
                return null;
            }
            publishProgress(i);
            SystemClock.sleep(1000); //Inaccurate count-down, but hey... its an example :)
        }
        return null;
    }
}
