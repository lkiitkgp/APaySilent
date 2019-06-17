package com.perpule.apaysilent1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class CancelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel);
        ResponseProcessor.processIntentForTextView(getIntent(), (TextView)findViewById(R.id.cancelMoreInfoView), this.getLocalClassName());
    }
}
