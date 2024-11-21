package com.example.sarithmetics;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class Subscription {
    //Free (0)
    public static final long FREE_ITEM_COUNT_LIMIT = 70;
    public static final long FREE_TRANSACTION_COUNT_LIMIT = 100;
    public static final long FREE_CATEGORY_COUNT_LIMIT = 5;
    //Premium1 (1)
    public static final long PREMIUM1_ITEM_COUNT_LIMIT = 150;
    public static final long PREMIUM1_TRANSACTION_COUNT_LIMIT = 500;
    public static final long PREMIUM1_CATEGORY_COUNT_LIMIT = 11;
    //Premium2 (2)
    public static final long PREMIUM2_ITEM_COUNT_LIMIT = 240;
    public static final long PREMIUM2_TRANSACTION_COUNT_LIMIT = 180;
    public static final long PREMIUM2_CATEGORY_COUNT_LIMIT = 18;
    //Debug Free (3)
    public static final long DEBUG_FREE_ITEM_COUNT_LIMIT = 10;
    public static final long DEBUG_FREE_TRANSACTION_COUNT_LIMIT = 10;
    public static final long DEBUG_FREE_CATEGORY_COUNT_LIMIT = 10;
    //Debug Premium (4)
    public static final long DEBUG_PREMIUM_ITEM_COUNT_LIMIT = 15;
    public static final long DEBUG_PREMIUM_TRANSACTION_COUNT_LIMIT = 15;
    public static final long DEBUG_PREMIUM_CATEGORY_COUNT_LIMIT = 15;

    //Subscription Types
    public static final int FREE = 0;
    public static final int PREMIUM1 = 1;
    public static final int PREMIUM2 = 2;
    public static final int DEBUG_FREE = 3;
    public static final int DEBUG_PREMIUM = 4;

    //List Types
    public static final int ITEM = 0;
    public static final int TRANSACTION = 1;
    public static final int CATEGORY = 2;

    public static long getLimit(int subscription_type, int limit_type) {
        switch (limit_type) {
            case 0: return getItemLimit(subscription_type);
            case 1: return getTransactionLimit(subscription_type);
            case 2: return getCategoryLimit(subscription_type);
        }
        return -1;
    }

    private static long getItemLimit(int subscription_type) {
        switch (subscription_type) {
            case 0: return FREE_ITEM_COUNT_LIMIT;
            case 1: return PREMIUM1_ITEM_COUNT_LIMIT;
            case 2: return PREMIUM2_ITEM_COUNT_LIMIT;
            case 3: return DEBUG_FREE_ITEM_COUNT_LIMIT;
            case 4: return DEBUG_PREMIUM_ITEM_COUNT_LIMIT;
        }
        return -1;
    }

    private static long getTransactionLimit(int subscription_type) {
        switch (subscription_type) {
            case 0: return FREE_TRANSACTION_COUNT_LIMIT;
            case 1: return PREMIUM1_TRANSACTION_COUNT_LIMIT;
            case 2: return PREMIUM2_TRANSACTION_COUNT_LIMIT;
            case 3: return DEBUG_FREE_TRANSACTION_COUNT_LIMIT;
            case 4: return DEBUG_PREMIUM_TRANSACTION_COUNT_LIMIT;
        }
        return -1;
    }

    private static long getCategoryLimit(int subscription_type) {
        switch (subscription_type) {
            case 0: return FREE_CATEGORY_COUNT_LIMIT;
            case 1: return PREMIUM1_CATEGORY_COUNT_LIMIT;
            case 2: return PREMIUM2_CATEGORY_COUNT_LIMIT;
            case 3: return DEBUG_FREE_CATEGORY_COUNT_LIMIT;
            case 4: return DEBUG_PREMIUM_CATEGORY_COUNT_LIMIT;
        }
        return -1;
    }

    public static Request createCheckOutRequest() {
        String FUNC_TAG = "createCheckOutRequest";

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"data\":{\"attributes\":{\"send_email_receipt\":false,\"show_description\":true,\"show_line_items\":true,\"line_items\":[{\"currency\":\"PHP\",\"amount\":24999,\"description\":\"More storage space for your Sarithmetics account\",\"name\":\"Sarithmetics Premium Subscription\",\"quantity\":1}],\"description\":\"Subscription to premium\",\"payment_method_types\":[\"gcash\",\"card\",\"paymaya\"]}}}");
        Request request = new Request.Builder()
                .url("https://api.paymongo.com/v1/checkout_sessions")
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("authorization", "Basic c2tfdGVzdF91Wm1FTkVWRWdKcWthOVFOQ3dnU3ZlNWY6cGtfdGVzdF9CMlBHYmJEYURXVmhHOUdwZGVNZkdTeVE=")
                .build();

        return request;
    }

    public static String parseCheckOutUrl(String json_response) {
        try {
            JSONObject json = new JSONObject(json_response);

            return json
                    .getJSONObject("data")
                    .getJSONObject("attributes")
                    .getString("checkout_url");

        } catch (JSONException e) {
            return null;
        }
    }

    public static String parseCheckOutStatus(String json_response) {
        try {
            JSONObject json = new JSONObject(json_response);

            return json
                    .getJSONObject("data")
                    .getJSONObject("attributes")
                    .getJSONObject("payment_intent")
                    .getJSONObject("attributes")
                    .getString("status");

        } catch (JSONException e) {
            return null;
        }
    }

    public static String parseCheckOutID(String json_response) {
        try {
            JSONObject json = new JSONObject(json_response);

            return json
                    .getJSONObject("data")
                    .getString("id");

        } catch (JSONException e) {
            return null;
        }
    }

    public static Request createCheckOutGetRequest(String id) {
        return new Request.Builder()
                .url("https://api.paymongo.com/v1/checkout_sessions/" + id)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("authorization", "Basic c2tfdGVzdF91Wm1FTkVWRWdKcWthOVFOQ3dnU3ZlNWY6cGtfdGVzdF9CMlBHYmJEYURXVmhHOUdwZGVNZkdTeVE=")
                .build();
    }

    public static Request createCheckOutExpireRequest(String id) {
        return new Request.Builder()
                .url("https://api.paymongo.com/v1/checkout_sessions/" + id + "/expire")
                .post(RequestBody.create("", null))
                .addHeader("accept", "application/json")
                .addHeader("authorization", "Basic c2tfdGVzdF91Wm1FTkVWRWdKcWthOVFOQ3dnU3ZlNWY6cGtfdGVzdF9CMlBHYmJEYURXVmhHOUdwZGVNZkdTeVE=")
                .build();
    }

    public static long getUnixOneMonthExpiry() {
        // Create a Calendar instance set to the current date and time
        Calendar calendar = Calendar.getInstance();

        // Add one month to the current date
        calendar.add(Calendar.MONTH, 1);

        // Convert to Unix timestamp in seconds
        return calendar.getTimeInMillis() / 1000;
    }

    public static long getUnixOneMinuteExpiry() {
        long currentTimeMillis = System.currentTimeMillis();

        // Add 5 minutes (300,000 milliseconds) to the current time
        long fiveMinutesFromNowMillis = currentTimeMillis + (5 * 60 * 1000);

        // Convert milliseconds to seconds (Unix timestamp) and return
        return fiveMinutesFromNowMillis / 1000;
    }
}
