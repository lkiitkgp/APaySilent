package com.perpule.apaysilent1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (BuildConfig.DEBUG) {
            Log.e(this.getLocalClassName(), "DEBUG MODE");
        } else {
            Log.e(this.getLocalClassName(), "RELEASE MODE");
        }
    }

    public void onStartButtonClick(View v) {

   //     Intent myintent = new Intent(this,AmazonPayActivity.class);
        Intent myintent = new Intent(this,APayActivity.class);

        EditText amt = (EditText) findViewById(R.id.amountText);
        String amount = amt.getText().toString();
     //  String amount = (EditText) findViewById(R.id.amountText).getText().toString();
     //   String amount = "5";
        myintent.putExtra("TOTAL_AMOUNT", amount );
 //       Toast.makeText(this, "Amount is "+ amount, Toast.LENGTH_SHORT).show();
        startActivity(myintent);
    }
}
