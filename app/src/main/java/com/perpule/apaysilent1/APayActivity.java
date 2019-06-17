package com.perpule.apaysilent1;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.api.Listener;
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import amazonpay.silentpay.APayCallback;
import amazonpay.silentpay.APayError;
import amazonpay.silentpay.AmazonPay;
import amazonpay.silentpay.EncryptedRequest;
import amazonpay.silentpay.GetBalanceRequest;
import amazonpay.silentpay.GetBalanceResponse;

public class APayActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
   // private static final String MERCHANT_ID =  "A1T5DRWMBFNP17";
   private static final String MERCHANT_ID =  "AZ4WQCLDT2DF0";

    boolean isSandbox = true;
    String amount,amt ;
    Float am;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apay);
        amount = getIntent().getStringExtra("TOTAL_AMOUNT");
        am  = Float.parseFloat(amount);
        amt = String.format("%.2f", am);
        //  am = String.format("%.2f", amount);

        //   (TextView) findViewById(R.id.aPayMoreInfoView)).setText("Sign out failed, try again");
        //    Toast.makeText(AmazonPayActivity.this, "Amount is "+ amount , Toast.LENGTH_SHORT).show();


    }

    @Override
    protected  void onStart(){
        TextView text = (TextView) findViewById(R.id.textView);
        text.setText("Total Amount: "+ amt);
        super.onStart();
    }

    @Override
    protected void onResume() {
        getBalance();
        super.onResume();
    }

    @Override
    protected void onPause() {
//        hideProgressBar();
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
    //        ResponseProcessor.processIntentForTextView(data, (TextView) findViewById(R.id.aPayMoreInfoView), this.getLocalClassName());
        } else {
            Log.e(this.getLocalClassName(), "received no response");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void onSignOutClick(View v) {
     //   showProgressBar();
        AuthorizationManager.signOut(this, new Listener<Void, AuthError>() {
            @Override
            public void onSuccess(Void aVoid) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLinkAccountView();
                 //       ((TextView) findViewById(R.id.aPayMoreInfoView)).setText("Sign out successful");
               //         hideProgressBar();
                    }
                });
                getBalance();
            }

            @Override
            public void onError(AuthError authError) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        ((TextView) findViewById(R.id.aPayMoreInfoView)).setText("Sign out failed, try again");
                        showSignOutView();
//                        hideProgressBar();
                    }
                });
            }
        });
    }

    public void onLinkAccountClick(View v) {
        //       if (((Switch) findViewById(R.id.pendingIntentSwitch)).isChecked()) {
        //         showProgressBar();
        //       linkUsingPendingIntent(getCustomTabIntent());
        // } else {
        linkUsingAuthIntent(getCustomTabIntent());
        //      }
    }

    private void getBalance() {
   //     showProgressBar();

        //      boolean isSandbox = true;
        //    boolean isSandbox = findViewById(R.id.sandboxSwitch)).isChecked();
        GetBalanceRequest request = new GetBalanceRequest(MERCHANT_ID, isSandbox);
        AmazonPay.getBalance(this, request, new APayCallback() {
            @Override
            public void onSuccess(Bundle bundle) {
                final GetBalanceResponse response = GetBalanceResponse.fromBundle(bundle);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.balanceView)).setText(response.getBalance()+"INR");


                        showSignOutView();
//                        enableUnsafeUiElements();
//                        hideProgressBar();
                    }
                });
            }

            @Override
            public void onError(final APayError aPayError) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                 //       ResponseProcessor.processErrorForTextView(aPayError, (TextView) findViewById(R.id.aPayMoreInfoView), "AmazonPayActivity");
                //        ((TextView) findViewById(R.id.balanceView)).setText("Balance: -");
                        showLinkAccountView();
//                        disableUnsafeUiElements();
//                        hideProgressBar();
                    }
                });

            }
        });
    }

    private void linkUsingAuthIntent(CustomTabsIntent customTabIntent) {
        startActivityForResult(AmazonPay.getAuthorizationIntent(this, customTabIntent), REQUEST_CODE);
    }

    private void linkUsingPendingIntent(CustomTabsIntent customTabIntent) {
        AmazonPay.authorize(this,
                customTabIntent,
                PendingIntent.getActivity(this, 0, new Intent(this, CompletionActivity.class), 0),
                PendingIntent.getActivity(this, 0, new Intent(this, CancelActivity.class), 0));
    }

    private CustomTabsIntent getCustomTabIntent() {
        //      if (((Switch) findViewById(R.id.mobileBrowserSwitch)).isChecked()) {
        //        return null;
        //  } else {
        return new CustomTabsIntent.Builder()
                .setToolbarColor(Color.BLACK)
                .build();
        //}
    }

    private void showProgressBar() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ProgressBar) findViewById(R.id.progressBar)).setVisibility(View.VISIBLE);
                disableUnsafeUiElements();
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            }
        });
    }

    private void hideProgressBar() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ProgressBar) findViewById(R.id.progressBar)).setVisibility(View.INVISIBLE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });
    }

    private void enableUnsafeUiElements() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //            ((Switch) findViewById(R.id.sandboxSwitch)).setEnabled(true);
            //    ((EditText) findViewById(R.id.amountEditText)).setEnabled(true);
                ((Button) findViewById(R.id.payButton)).setEnabled(true);
            }
        });
    }

    private void disableUnsafeUiElements() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //          ((Switch) findViewById(R.id.sandboxSwitch)).setEnabled(false);
             //   ((EditText) findViewById(R.id.amountEditText)).setEnabled(false);
                ((Button) findViewById(R.id.payButton)).setEnabled(false);
            }
        });
    }

    private void showSignOutView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                (findViewById(R.id.AccountView)).setVisibility(View.GONE);
                (findViewById(R.id.signOutView)).setVisibility(View.VISIBLE);
                ((Button) findViewById(R.id.payButton)).setEnabled(true);
            }
        });
    }



    private void showLinkAccountView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.balanceView)).setText(" - ");
                (findViewById(R.id.AccountView)).setVisibility(View.VISIBLE);
                (findViewById(R.id.signOutView)).setVisibility(View.GONE);
                ((Button) findViewById(R.id.payButton)).setEnabled(true);
            }
        });
    }

    public void onSandboxClick(View v) {
        getBalance();
    }

    public void onPayNowClick(View v) {
    //    showProgressBar();
        disableUnsafeUiElements();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(APayActivity.this, "Encrypting...", Toast.LENGTH_SHORT).show();
            }
        });
        new EncryptionTask().execute(onEncryptionSuccess(),
                EncryptionTask.Operation.PROCESS_CHARGE,
                getProcessChargeParams());
    }

    private Map<String, String> getProcessChargeParams() {
        return new HashMap<String, String>() {{
            put("orderTotalAmount", amount);
            put("orderTotalCurrencyCode", "INR");
            put("sellerNote", "somesell ernote");
            put("customInformation", "someseller customdata");
            put("sellerStoreName", "someseller storename");
            put("sellerOrderId", UUID.randomUUID().toString());
        }};
    }

    private Handler.Callback onEncryptionSuccess() {
        return new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle data = msg.getData();
                Intent completionIntent = new Intent(APayActivity.this, CompletionActivity.class);
                Bundle b = new Bundle();
                data.putBoolean("isSandbox", isSandbox);
                completionIntent.putExtras(b);

                //

                AmazonPay.processCharge(APayActivity.this,
                        getCustomTabIntent(),
                        PendingIntent.getActivity(APayActivity.this, 0, completionIntent, 0),
                        PendingIntent.getActivity(APayActivity.this, 0, new Intent(APayActivity.this, CancelActivity.class), 0),
                        new EncryptedRequest(data.getString("payload"), data.getString("key"), data.getString("iv"), isSandbox));
                    //    new EncryptedRequest(payload,key,iv, isSandbox));
                return true;
            }
        };
    }

}
