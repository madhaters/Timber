package com.topratedapps.musicplayer.helpers;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.topratedapps.musicplayer.utils.PreferencesUtility;

import java.util.List;

/**
 * Created by bullhead on 11/13/17.
 */

public class BillingHelper implements PurchasesUpdatedListener {
    private static BillingHelper instance;
    private BillingClient mBillingClient;
    private boolean isBillingReady;
    private static final String TAG = "BillingHelper";
    private BillingListener callback;

    private BillingHelper(Activity activity, @Nullable final BillingListener callback) {
        this.callback = callback;
        mBillingClient = BillingClient.newBuilder(activity).setListener(this).build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(int responseCode) {
                if (responseCode == BillingClient.BillingResponse.OK) {
                    isBillingReady = true;
                    if (callback != null) {
                        callback.billingInitialized();
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {

                Log.e(TAG, "There is an error is initializing billing.");
            }
        });
        //query purchases
        Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(SKIN_SKU_ID);
        if (purchasesResult != null && purchasesResult.getResponseCode() == BillingClient.BillingResponse.OK) {
            if (purchasesResult.getPurchasesList() != null) {
                for (Purchase purchase : purchasesResult.getPurchasesList()) {
                    if (SKIN_SKU_ID.equals(purchase.getSku())) {
                        PreferencesUtility.getInstance(activity).setFullUnlocked(true);
                    }
                }
            }
        }
    }

    public static BillingHelper getInstance(Activity activity, @Nullable BillingListener callback) {
        if (instance == null) {
            instance = new BillingHelper(activity, callback);
        } else {
            instance.callback = callback;
        }
        return instance;
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        if (responseCode == BillingClient.BillingResponse.OK) {
            if (purchases != null) {
                for (Purchase purchase : purchases) {
                    if (SKIN_SKU_ID.equals(purchase.getSku())) {
                        if (callback != null) {
                            callback.purchaseSuccessful(SKIN_SKU_ID);
                        }
                    }
                }
            } else {
                if (callback != null) {
                    callback.errorInPurchase();
                }
            }
        } else {
            if (callback != null) {
                callback.errorInPurchase();
            }
        }

    }

    public int makePurchase(Activity activity, String skuId, @NonNull BillingListener callback) {
        this.callback = callback;
        if (isBillingReady) {
            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setSku(skuId)
                    .setType(BillingClient.SkuType.INAPP)
                    .build();
            int responseCode = mBillingClient.launchBillingFlow(activity, flowParams);
            return responseCode;
        } else {
            return BillingClient.BillingResponse.BILLING_UNAVAILABLE;
        }
    }

    public static final String SKIN_SKU_ID = "skin_unlock";

    //public static final String SKIN_SKU_ID="android.test.purchased";
    public interface BillingListener {
        void billingInitialized();

        void errorInPurchase();

        void purchaseSuccessful(String skuId);
    }
}
