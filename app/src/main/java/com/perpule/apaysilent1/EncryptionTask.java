package com.perpule.apaysilent1;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class EncryptionTask extends AsyncTask<Object, Void, Void> {

    enum Operation {
        PROCESS_CHARGE,
        GET_CHARGE_STATUS,
        VALIDATE
    }

    private static final String LOG_TAG = "EncryptionTask";

    private static final String ENDPOINT =    "https://amazon-perpule.appspot.com";
    private static final String PROCESS_CHARGE_PATH = "/signin1";
    private static final String GET_CHARGE_STATUS_PATH = "/signAndEncryptForOperation";
    private static final String VALIDATION_PATH = "/VerifySignature";


    private Operation operation = null;
    private Map<String, String> paramMap = null;

    @Override
    protected Void doInBackground(Object... objects) {
        Handler.Callback callback = (Handler.Callback) objects[0];
        operation = (Operation) objects[1];
        paramMap = (HashMap) objects[2];
        try {
            URL url = null;
            HttpURLConnection urlConnection = null;
            if (operation == Operation.PROCESS_CHARGE) {
                url = createUrl(new URL(ENDPOINT), paramMap, PROCESS_CHARGE_PATH);
                Log.d(LOG_TAG, "Url =" + url.toString());
            } else if (operation == Operation.GET_CHARGE_STATUS) {
                url = createUrl(new URL(ENDPOINT), paramMap, GET_CHARGE_STATUS_PATH);
                Log.d(LOG_TAG, "Url =" + url.toString());
            } else if (operation == Operation.VALIDATE) {
                url = createUrl(new URL(ENDPOINT), paramMap, VALIDATION_PATH);
                Log.d(LOG_TAG, "Url =" + url.toString());
            }
            if (url != null) {
                Log.d(LOG_TAG, "Url =" + url.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String responseString = readStream(urlConnection.getInputStream());
              //      responseString = responseString.replaceFirst("https://payments-in.integ.amazon.com/api/chargeStatus\\?", "");

                    responseString = responseString.replaceFirst("https://amazonpay.amazon.in/api/chargeStatus\\?", "");

                    Log.wtf(LOG_TAG, "response:" + responseString);
                    Message message = new Message();
                    Bundle bundle = new Bundle();

                    if (operation == Operation.VALIDATE) {
                        bundle.putString("response", responseString);
                    } else if (operation == Operation.GET_CHARGE_STATUS || operation == Operation.PROCESS_CHARGE) {
                        Map<String, String> decodedParams = getDecodedQueryParameters(responseString);

                        if (decodedParams != null) {
                            for (String key : decodedParams.keySet()) {
                                Log.d(LOG_TAG, "key:" + key);
                                Log.d(LOG_TAG, "value:" + decodedParams.get(key));
                            }
                            bundle.putString("payload", decodedParams.get("payload"));
                            bundle.putString("key", decodedParams.get("key"));
                            bundle.putString("iv", decodedParams.get("iv"));
                        }
                    }
                    message.setData(bundle);
                    callback.handleMessage(message);
                } else {
                    Log.e(LOG_TAG, String.format("Unable to sign payload. Received following status code: %d",
                            responseCode));
                }
            }

        } catch (
                MalformedURLException e)

        {
            Log.e(LOG_TAG, "malformed url error", e);
        } catch (
                IOException e)

        {
            Log.e(LOG_TAG, "io error", e);
        }

        return null;
    }

    /**
     * Returns the uri created from the supplied parameters
     *
     * @param endpoint   The endpoint of the uri
     * @param parameters The parameters to be added to the uri
     * @param path       The path of the uri
     * @return The created URI based on the supplied params
     */

    private URL createUrl(URL endpoint, Map<String, String> parameters, String path) throws MalformedURLException {
        Uri uri = Uri.parse(endpoint.toString());

        if (path != null && !path.isEmpty()) {
            uri = uri.buildUpon().path(path).build();
        }

        if (parameters != null && parameters.size() > 0) {
            uri = addQueryParameters(uri, parameters);
        }

        return new URL(uri.toString());
    }

    /**
     * Used to read response from the input stream provided by url connect
     *
     * @param in the input stream
     * @return the data present in the input stream
     */
    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString().trim().replaceAll("<br/>", "");
    }

    /**
     * Add the supplied query params to the supplied URI
     *
     * @param uri        the uri to which the params have to be added
     * @param parameters the params that are to be added
     * @return the uri with the params added
     */
    private Uri addQueryParameters(Uri uri, Map<String, String> parameters) {
        for (String key : parameters.keySet()) {
            uri = uri.buildUpon().appendQueryParameter(key, parameters.get(key)).build();
        }
        return uri;
    }

    /**
     * Get the decoded Query params
     *
     * @param query the encoded query params
     * @return the decoded query params
     * @throws UnsupportedEncodingException thrown if the encoding used on the query params is not supported
     */
    Map<String, String> getDecodedQueryParameters(String query) throws UnsupportedEncodingException {
        if (query == null || query.trim().length() < 1) {
            return null;
        }
        HashMap<String, String> parameters = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int index = pair.indexOf("=");
            parameters.put(URLDecoder.decode(pair.substring(0, index), "UTF-8").trim(), URLDecoder.decode(pair
                    .substring
                            (index + 1), "UTF-8").trim());
        }
        return parameters;

    }


}
