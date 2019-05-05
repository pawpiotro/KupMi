package com.wpam.kupmi.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import com.wpam.kupmi.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.wpam.kupmi.lib.Constants;

public class FetchAddressIntentService extends IntentService {


    private static final String TAG = "FETCH_ADDRESS_INTENT_SERVICE";

    private ResultReceiver receiver;

    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        if (intent == null) {
            return;
        }

        receiver = intent.getParcelableExtra(Constants.RECEIVER);
        if(receiver == null){
            return;
        }

        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);
        } catch (IOException ioException) {
            Log.e(TAG, "Service not available", ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            Log.e(TAG, "Invalid coordinates", illegalArgumentException);
        }

        if (addresses == null || addresses.size()  == 0) {
            Log.e(TAG, "No address was found");
            deliverResultToReceiver(Constants.FAILURE_RESULT, "No address was found");
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();
            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(TAG, "Address found");
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments));

        }
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        receiver.send(resultCode, bundle);
    }
}
