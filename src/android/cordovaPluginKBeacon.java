package app.heroesde4patas.cordova.plugin.kbeacon;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.app.Activity;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.cordova.LOG;
import android.util.Log;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.Gravity;
import android.widget.Toast;
import android.os.Build;

import com.kkmcn.kbeaconlib2.KBAdvPackage.KBAccSensorValue;
import com.kkmcn.kbeaconlib2.KBAdvPackage.KBAdvPacketBase;
import com.kkmcn.kbeaconlib2.KBAdvPackage.KBAdvPacketEddyTLM;
import com.kkmcn.kbeaconlib2.KBAdvPackage.KBAdvPacketEddyUID;
import com.kkmcn.kbeaconlib2.KBAdvPackage.KBAdvPacketEddyURL;
import com.kkmcn.kbeaconlib2.KBAdvPackage.KBAdvPacketIBeacon;
import com.kkmcn.kbeaconlib2.KBAdvPackage.KBAdvPacketSensor;
import com.kkmcn.kbeaconlib2.KBAdvPackage.KBAdvPacketSystem;
import com.kkmcn.kbeaconlib2.KBAdvPackage.KBAdvType;
import com.kkmcn.kbeaconlib2.KBeacon;
import com.kkmcn.kbeaconlib2.KBeaconsMgr;

import java.util.HashMap;

/**
 * This class echoes a string called from JavaScript.
 */
public class cordovaPluginKBeacon extends CordovaPlugin {

    String LOG_TAG = "KBeaconPlugin";

    private HashMap<String, KBeacon> mBeaconsDictory;
    private KBeacon[] mBeaconsArray;
    private JSONArray beaconsJSONArray = new JSONArray();
    private JSONArray beaconsDetectedJSONArray = new JSONArray();

    private KBeaconsMgr mBeaconsMgr;
    private KBeaconsMgr.KBeaconMgrDelegate beaconMgr;

    private int mScanFailedContinueNum = 0;

    private final static int  MAX_ERROR_SCAN_NUMBER = 2;
    private final static int PERMISSION_CONNECT = 20;
    private static final int PERMISSION_COARSE_LOCATION = 22;
    private static final int PERMISSION_FINE_LOCATION = 23;
    private static final int PERMISSION_SCAN = 24;


    private CallbackContext command;
    private Activity cordovaActivity;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        beaconMgr = new KBeaconsMgr.KBeaconMgrDelegate() {

            @Override
            public void onBeaconDiscovered(KBeacon[] beacons) {
                for (KBeacon beacon: beacons){
                    for (KBAdvPacketBase advPacket : beacon.allAdvPackets()) {
                        switch (advPacket.getAdvType()) {
                            case KBAdvType.IBeacon: {
                                KBAdvPacketIBeacon advIBeacon = (KBAdvPacketIBeacon) advPacket;
                                JSONArray KBArray = new JSONArray();

                                String mac = beacon.getMac();

                                if (!jsonArrayContains(beaconsDetectedJSONArray, mac)) {
                                    beaconsDetectedJSONArray.put(mac);

                                    KBArray.put(beacon.getMac());
                                    KBArray.put(beacon.getName());
                                    KBArray.put(beacon.getRssi());

                                    KBArray.put(advIBeacon.getRefTxPower());
                                    KBArray.put(advIBeacon.getUuid());
                                    KBArray.put(advIBeacon.getMajorID());
                                    KBArray.put(advIBeacon.getMinorID());

                                    beaconsJSONArray.put(KBArray);
                                }
                               break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                toastShow("Start N scan failed：" + errorCode);
                if (mScanFailedContinueNum >= MAX_ERROR_SCAN_NUMBER){
                    toastShow("Scan encount error, error time:" + mScanFailedContinueNum);
                }
                mScanFailedContinueNum++;
            }

            @Override
            public void onCentralBleStateChang(int newState) {
                toastShow("centralBleStateChang" + newState);
            }


        };

        mBeaconsMgr = KBeaconsMgr.sharedBeaconManager(getContext());

        /**
         * Validate if Manager of Beacons is supported
         */
        if (mBeaconsMgr == null){
            toastShow("mBeaconsMgr es null, El dispositivo no puede leer beacons");
        }

        mBeaconsMgr.delegate = beaconMgr;
        mBeaconsMgr.setScanMinRssiFilter(-80);
        mBeaconsMgr.setScanMode(KBeaconsMgr.SCAN_MODE_LOW_LATENCY);


    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callback) throws JSONException {
        this.command = callback;
        toastShow("We are entering execute");

//        if (action.equals("coolMethod")) {
//            String message = args.getString(0);
//            this.coolMethod(message, callback);
//            return true;
//        }

        if ("startScan".equalsIgnoreCase(action)) {
            this.startScan(callback);
            return true;
        }

        if ("stopScanning".equalsIgnoreCase(action)) {
            this.stopScanning();
            return true;
        }

        if ("checkPermissions".equalsIgnoreCase(action)) {
            this.checkPermissions(callback);
            return true;
        }

        if ("getDiscoveredDevices".equalsIgnoreCase(action)) {
            this.getDiscoveredDevices(callback);
            return true;
        }

        return false;
    }



    private void getDiscoveredDevices(CallbackContext callbackContext){
        if (beaconsJSONArray != null) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, beaconsJSONArray.toString());
            callbackContext.sendPluginResult(pluginResult);
        } else {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, "El arreglo es nulo");
            callbackContext.sendPluginResult(pluginResult);
        }
    }

    private void startScan(CallbackContext callbackContext){
        mBeaconsMgr.setScanMinRssiFilter(-60);
        int nStartScan = mBeaconsMgr.startScanning();
        if (nStartScan == 0){
            callbackContext.success("Iniciando la lectura de dispositivos");
        }
        else if (nStartScan == KBeaconsMgr.SCAN_ERROR_BLE_NOT_ENABLE) {
            callbackContext.error("BLE function is not enable");
        }
        else if (nStartScan == KBeaconsMgr.SCAN_ERROR_NO_PERMISSION) {
            callbackContext.error("BLE scanning has no location permission");
        }
        else {
            callbackContext.error("BLE scanning unknown error");
        }
    }

    private void stopScanning(){
        mBeaconsMgr.stopScanning();
    }

    private void checkPermissions(CallbackContext callbackContext){
        if (!checkBluetoothPermitAllowed()) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, false);
            callbackContext.sendPluginResult(pluginResult);
        }else{
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, true);
            callbackContext.sendPluginResult(pluginResult);
        }

    }

    private boolean checkBluetoothPermitAllowed() {
        boolean bHasPermission = true;
        /**
         * for android6, the app need corse location permission for BLE scanning
         */
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_COARSE_LOCATION);
            bHasPermission = false;
        }
        /**
         * for android10, the app need fine location permission for BLE scanning
         */
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
            bHasPermission = false;
        }
        /**
         * for android 12, the app need declare follow permissions
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_SCAN}, PERMISSION_SCAN);
                bHasPermission = false;
            }
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_CONNECT);
                bHasPermission = false;
            }
        }
        return bHasPermission;
    }


//    private void coolMethod(String message, CallbackContext callbackContext) {
//
//        toastShow("We are entering coolMethod");
//        if (message != null && message.length() > 0) {
//            callbackContext.success(message);
//        } else {
//            callbackContext.error("Expected one non-empty string argument.");
//        }
//    }



    public static boolean jsonArrayContains(JSONArray jsonArray, Object value) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Object obj = jsonArray.get(i);
                if (obj.equals(value)) {
                    return true;
                }
            } catch (JSONException e) {
                // Handle exception
            }
        }
        return false;
    }


    public static double calculateDistance(double rssi, double calibratedPower, double n) {
        double distance = Math.pow(10, ((Math.abs(rssi) - Math.abs(calibratedPower)) / (10 * n)));
        return distance;
    }


    /**
     * Returns the application context.
     */
    private Context getContext() {
        return cordova.getActivity();
    }

    /**
     * Retirms the application activity
     */
    private Activity getActivity() {
        return cordova.getActivity();
    }

    /**
     * To Show toast messages
     */
    public void toastShow(String strMsg) {
        Toast toast=Toast.makeText(getContext(), strMsg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}
