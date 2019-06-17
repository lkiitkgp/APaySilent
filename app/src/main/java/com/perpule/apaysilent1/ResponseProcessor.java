package com.perpule.apaysilent1;

import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

import amazonpay.silentpay.APayAuthorizationResult;
import amazonpay.silentpay.APayError;
import amazonpay.silentpay.ProcessChargeResponse;

public class ResponseProcessor {

    static Object processIntentForTextView(Intent intent, TextView textView, String tag) {
        if (intent != null) {
            APayError aPayError = APayError.fromIntent(intent);
            APayAuthorizationResult aPayAuthResult = APayAuthorizationResult.fromIntent(intent);
            ProcessChargeResponse aPayProcessChargeResponse = ProcessChargeResponse.fromIntent(intent);
            if (aPayError != null) {
                textView.setText(getErrorInfo(aPayError, tag));
            } else if (aPayAuthResult != null) {
                textView.setText(getAuthResultInfo(aPayAuthResult));
            } else if (aPayProcessChargeResponse != null) {
                textView.setText(getProcessChargeInfo(aPayProcessChargeResponse));
            } else {
                Log.e(tag, "Received no response");
            }
            if(aPayError!=null){
                return aPayError;
            }else if(aPayAuthResult!=null){
                return aPayAuthResult;
            }else if(aPayProcessChargeResponse!=null){
                return aPayProcessChargeResponse;
            }
        } else {
            Log.e(tag, "Received no response");
        }
        return null;
    }

    static void processErrorForTextView(APayError error, TextView textView, String tag) {
        if (error != null) {
            textView.setText(getErrorInfo(error, tag));
        } else {
            Log.e(tag, "Received no error");
        }
    }

    static String getErrorInfo(APayError error, String tag) {
        if (error.getErrorType() == APayError.ErrorType.AUTH_ERROR) {
            Log.e(tag, "Received Auth Error", error.getAuthError());
            return String.format("Auth Error type: %s\nError Message: %s", error.getAuthError().getType().name(), error.getAuthError().getMessage());
        } else {
            Log.e(tag, "Received Apay Error", error);
            return String.format("Apay Error type: %s\nError Message: %s", error.getErrorType().name(), error.getMessage());
        }
    }

    private static String getProcessChargeInfo(ProcessChargeResponse response) {
        if (response.getSignature() != null && response.getTransactionId() != null)
            return String.format("Process Charge Complete\nSignature: %s\nTransaction ID: %s", response.getSignature(), response.getTransactionId());
        else return "Process Charge Cancelled";
    }

    private static String getAuthResultInfo(APayAuthorizationResult result) {
        return String.format("Auth status: %s",result.getStatus().name());
    }

}
