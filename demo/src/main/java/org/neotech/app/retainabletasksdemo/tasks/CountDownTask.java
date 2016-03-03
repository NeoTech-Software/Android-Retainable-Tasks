package org.neotech.app.retainabletasksdemo.tasks;

import android.os.SystemClock;

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
        for(int i = amount; i > 0; i--){
            publishProgress(i);
            SystemClock.sleep(1000); //Inaccurate count-down, but hey... its an example :)
        }
        return null;
    }
}
