package com.drobisch.partkeeprscannrapp;

import android.app.Application;
import android.content.Intent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by drobisch on 13/11/2016.
 */
public class PartkeeprScannrApplication extends Application {
    public void onCreate () {
        super.onCreate();
        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                handleUncaughtException(thread, e);
            }
        });
    }

    public void handleUncaughtException (Thread thread, Throwable e)
    {

        e.printStackTrace(); // not all Android versions will print the stack trace automatically

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        String s = writer.toString();

        /*
        Intent intent = new Intent(getApplicationContext(), SendLogActivity.class);
        intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
        //intent.putExtra("stackTrace",s);
        startActivity(intent);
        */


        Intent intent = new Intent(getApplicationContext(), SendLogActivity.class);
        intent.addFlags (Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.putExtra("stackTrace",s);
        startActivity (intent);

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }
}
