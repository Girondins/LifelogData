package com.mah.ex.lifelogdata;
import android.util.Log;

/**
 * Created by Girondins on 30/01/17.
 */
public class ExecuteThread {
    private Buffer<Runnable> inLine = new Buffer<Runnable>();
    private Executer execute;

    public void start() {
        if(execute==null) {
            execute = new Executer();
            execute.start();
        }
    }

    public void stop() {
        if(execute !=null) {
            execute.interrupt();
            execute =null;
        }
    }

    public void execute(Runnable runnable) {
        inLine.put(runnable);
    }

    private class Executer extends Thread {
        public void run() {
            Runnable runnable;
            while(execute !=null) {
                try {
                    runnable = inLine.get();
                    Log.d("EXECUTE", runnable.toString());
                    runnable.run();
                } catch (InterruptedException e) {
                    execute =null;
                }
            }
        }
    }
}
