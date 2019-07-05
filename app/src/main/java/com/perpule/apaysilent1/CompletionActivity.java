package com.perpule.apaysilent1;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import amazonpay.silentpay.APayCallback;
import amazonpay.silentpay.APayError;
import amazonpay.silentpay.AmazonPay;
import amazonpay.silentpay.EncryptedRequest;
import amazonpay.silentpay.GetChargeStatusResponse;
import amazonpay.silentpay.ProcessChargeResponse;

public class CompletionActivity extends AppCompatActivity {

    ProcessChargeResponse processChargeResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completion);
        Object response = ResponseProcessor.processIntentForTextView(getIntent(), (TextView) findViewById(R.id.completeMoreInfoView), this.getLocalClassName());
        if (response instanceof ProcessChargeResponse) {
            processChargeResponse = (ProcessChargeResponse) response;
            verifyProcessChargeResponse((ProcessChargeResponse)response);
            //getChargeStatus((ProcessChargeResponse) response);
        }
    }

    private void verifyProcessChargeResponse(ProcessChargeResponse response){
        Log.wtf("validation params","=");
        for(Map.Entry<String,String> e: response.getVerificationParameters().entrySet()){
            Log.wtf(e.getKey(),e.getValue());
        }
        new EncryptionTask().execute(onProcessChargeValidationComplete(),
                EncryptionTask.Operation.VALIDATE,
                response.getVerificationParameters());
    }

    private void verifyChargeStatusResponse(GetChargeStatusResponse response){
        Log.wtf("get charge status","validation params=");
        for(Map.Entry<String,String> e: response.getVerificationParameters().entrySet()){
            Log.wtf(e.getKey(),e.getValue());
        }
        new EncryptionTask().execute(onGetChargeStatusResponseValidationComplete(),
                EncryptionTask.Operation.VALIDATE,
                response.getVerificationParameters());
    }


    private void getChargeStatus(ProcessChargeResponse response) {
        new EncryptionTask().execute(onEncryptionSuccess(),
                EncryptionTask.Operation.GET_CHARGE_STATUS,
                getChargeStatusParams(response.getTransactionId()));
    }

    private Handler.Callback onProcessChargeValidationComplete(){
        return new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle data = msg.getData();
                if(data.getString("response").equalsIgnoreCase("true")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CompletionActivity.this,"Process Charge Validation successful",Toast.LENGTH_LONG).show();
                        }
                    });
                    getChargeStatus(processChargeResponse);
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CompletionActivity.this,"Process charge Validation failed",Toast.LENGTH_LONG).show();
                        }
                    });
                }
                return true;
            }
        };
    }

    private Handler.Callback onGetChargeStatusResponseValidationComplete(){
        return new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle data = msg.getData();
                if(data.getString("response").equalsIgnoreCase("true")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CompletionActivity.this,"Charge status Validation successful",Toast.LENGTH_LONG).show();
                        }
                    });
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CompletionActivity.this,"Charge status Validation failed",Toast.LENGTH_LONG).show();
                        }
                    });
                }
                return true;
            }
        };
    }

    private Handler.Callback onEncryptionSuccess() {
        return new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle data = msg.getData();
                EncryptedRequest encryptedRequest = new EncryptedRequest(data.getString("payload"), data.getString("key"), data.getString("iv"), false);
                Log.wtf("Encryptedreq","payload="+data.getString("payload")+"key="+data.getString("key"));
                pollChargeStatus(encryptedRequest);
                return true;
            }

            private void pollChargeStatus(final EncryptedRequest request) {
                AmazonPay.getChargeStatus(CompletionActivity.this, request, new APayCallback() {
                        @Override
                    public void onSuccess(Bundle bundle) {
                        GetChargeStatusResponse response = GetChargeStatusResponse.fromBundle(bundle);
                        if (response != null && response.getTransactionStatus() == GetChargeStatusResponse.TransactionStatus.PENDING) {
                            showProgressBar();
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    pollChargeStatus(request);
                                }
                            }, 2000);

                        } else if (response != null) {
                            hideProgressBar();
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
                            Date date123 = new Date(Long.parseLong(response.getTransactionDate()));
 //                           String date123 = sdf.format(response.getTransactionDate());



                            ((TextView) findViewById(R.id.completeMoreInfoView)).setText(String.format("Amount:%s %s\n" +
//                                            "Currency Code: %s\n" +
//                                            "Description: %s\n" +
//                                            "Merchant Transaction ID: %s\n" +
//                                            "Reason Code: %s\n" +
//                                            "SellerCustomData: %s\n" +
//                                            "Signature: %s\n" +
                                            "TransactionStatus: %s\n"+
                                            "TransactionDate: %s\n" +
                                            "TransactionID: %s\n",
                                    response.getTransactionValue(),
                                    response.getTransactionCurrencyCode(),
//                                    response.getTransactionStatusDescription(),
//                                    response.getMerchantTransactionId(),
//                                    response.getTransactionStatusCode(),
//                                    response.getMerchantCustomData(),
//                                    response.getSignature(),
                                    //response.getTransactionDate(),
                                    response.getTransactionStatus().name(),
                                    sdf.format(date123),
                                    response.getTransactionId()
                                    ));
                            verifyChargeStatusResponse(response);
                        } else {
                            hideProgressBar();
                            ((TextView) findViewById(R.id.completeMoreInfoView)).setText("Received no response for getChargeStatus");

                        }
                    }

                    @Override
                    public void onError(APayError aPayError) {
                        ((TextView) findViewById(R.id.completeMoreInfoView)).setText(ResponseProcessor.getErrorInfo(aPayError, CompletionActivity.class.getName()));
                    }
                });
            }
        };
    }

    private Map<String, String> getChargeStatusParams(final String transactionId) {
        return new HashMap<String, String>() {{
           // put("sellerId", "AZ4WQCLDT2DF0");
         //   put("awsAccessKeyId", "AKIAJAKG6LME27HVCD3A");
            put("transactionId", transactionId);
            put("transactionIdType", "TRANSACTION_ID");
        }};
    }

    private void showProgressBar() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ProgressBar) findViewById(R.id.progressBar)).setVisibility(View.VISIBLE);
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




}