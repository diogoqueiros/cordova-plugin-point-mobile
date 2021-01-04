package com.diogoqueiros;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import device.common.DecodeResult;
import device.common.ScanConst;
import device.sdk.ScanManager;

public class PointMobile extends CordovaPlugin {

    private static ScanManager mScanner = new ScanManager();
    private static DecodeResult mDecodeResult = new DecodeResult();
    private static CallbackContext callbackContext = null;
    private static ScanResultReceiver mScanResultReceiver = null;

    static {
        mScanner.aDecodeSetResultType(ScanConst.ResultType.DCD_RESULT_USERMSG);
        mScanner.aDecodeSetTriggerMode(ScanConst.TriggerMode.DCD_TRIGGER_MODE_ONESHOT);
    }

    public static class ScanResultReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mScanner != null) {
                mScanner.aDecodeGetResult(mDecodeResult.recycle());

                if (callbackContext != null) {
                    if (mDecodeResult.symName.equals("READ_FAIL")) {
                        callbackContext.error("READ_FAIL");
                    } else {
                        JSONObject json = new JSONObject();

                        try {
                            json.put("barcode", mDecodeResult.toString());
                            json.put("type", mDecodeResult.symName);
                        } catch (Exception e) {
                            LOG.d("PointMobile", "Error sending point mobile receiver: " + e.getMessage(), e);
                        }

                        callbackContext.success(json);
                    }
                }
            } else {
                if (callbackContext != null) {
                    callbackContext.error("SCANNER_ERROR");
                }
            }
        }
    }

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ScanConst.INTENT_USERMSG);
        this.mScanResultReceiver = new ScanResultReceiver();
        webView.getContext().registerReceiver(this.mScanResultReceiver, filter);
    }

    @Override
    public void onDestroy() {
        if (this.mScanResultReceiver != null) {
            try {
                this.webView.getContext().unregisterReceiver(this.mScanResultReceiver);
            } catch (Exception e) {
                LOG.d("PointMobile", "Error unregistering point mobile receiver: " + e.getMessage(), e);
            } finally {
                mScanResultReceiver = null;
            }
        }

        super.onDestroy();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("scan")) {
            this.callbackContext = callbackContext;
            mScanner.aDecodeSetTriggerOn(1);
        } else if (action.equals("cancel")) {
            this.onDestroy();

            if (this.callbackContext != null) {
                this.callbackContext.error("USER_CANCEL");
                this.callbackContext = null;
            }

            mScanner.aDecodeSetTriggerOn(0);
        } else {
            return false;
        }

        return true;
    }

}
