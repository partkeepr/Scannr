package com.drobisch.partkeeprscannrapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

public class SendLogActivity extends Activity {

    private TextView mStackTraceView;
    private String mStackTrace;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature (Window.FEATURE_NO_TITLE); // make a dialog without a titlebar
        setFinishOnTouchOutside (false); // prevent users from dismissing the dialog by tapping outside
        setContentView(R.layout.activity_send_log);

        Bundle bundle = getIntent().getExtras();
        mStackTrace =  bundle.getString("stackTrace");

        mStackTraceView = (TextView) findViewById(R.id.stacktrace);
        mStackTraceView.setText(mStackTrace);
    }
}
