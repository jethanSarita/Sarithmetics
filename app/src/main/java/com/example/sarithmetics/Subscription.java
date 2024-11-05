package com.example.sarithmetics;

public class Subscription {
    //Free
    public static final long FREE_ITEM_COUNT_LIMIT = 70;
    public static final long FREE_TRANSACTION_COUNT_LIMIT = 50;
    public static final long FREE_CATEGORY_COUNT_LIMIT = 5;
    //Premium1
    public static final long PREMIUM1_ITEM_COUNT_LIMIT = 150;
    public static final long PREMIUM1_TRANSACTION_COUNT_LIMIT = 110;
    public static final long PREMIUM1_CATEGORY_COUNT_LIMIT = 11;
    //Premium2
    public static final long PREMIUM2_ITEM_COUNT_LIMIT = 240;
    public static final long PREMIUM2_TRANSACTION_COUNT_LIMIT = 180;
    public static final long PREMIUM2_CATEGORY_COUNT_LIMIT = 18;
    //Debug Free
    public static final long DEBUG_FREE_ITEM_COUNT_LIMIT = 10;
    public static final long DEBUG_FREE_TRANSACTION_COUNT_LIMIT = 10;
    public static final long DEBUG_FREE_CATEGORY_COUNT_LIMIT = 10;
    //Debug Premium
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
}
