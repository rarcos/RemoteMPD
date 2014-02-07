package com.facetoe.remotempd;

import android.app.*;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import com.facetoe.remotempd.helpers.RMPDAlertDialogFragment;
import com.facetoe.remotempd.helpers.SettingsHelper;
import com.facetoe.remotempd.helpers.WifiConnectionAsyncTask;
import com.facetoe.remotempd.listeners.MPDManagerChangeListener;

import java.util.ArrayList;

/**
 * Created by facetoe on 31/12/13.
 */

public class RemoteMPDApplication extends Application implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static RemoteMPDApplication instance;
    public final static String APP_PREFIX = "RMPD-";
    private static final String TAG = APP_PREFIX + "RemoteMPDApplication";
    private static final int REQUEST_ENABLE_BT = 2;

    private Activity currentActivity;
    private AbstractMPDManager mpdManager;
    private final ArrayList<MPDManagerChangeListener> mpdManagerChangeListeners = new ArrayList<MPDManagerChangeListener>();
    private boolean connectionInProgress = false;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    public static RemoteMPDApplication getInstance() {
        checkInstance();
        return instance;
    }

    public void registerCurrentActivity(Activity activity) {
        Log.d(TAG, "Registering: " + activity);
        currentActivity = activity;
        checkState();
    }

    public void unregisterCurrentActivity() {
        Log.d(TAG, "Unregistering: " + currentActivity);
        currentActivity = null;
    }

    private void checkState() {
        if (currentActivity instanceof SettingsActivity) {
            return;
        }

            // Show Bluetooth specific dialog.
        if (SettingsHelper.isBluetooth() && !SettingsHelper.hasBluetoothSettings()) {
            DialogFragment dialog = RMPDAlertDialogFragment.getNoBluetoothSettingsDialog();
            showDialog(dialog);
            return;

            // No settings at all, show no settings dialog
        } else if (!SettingsHelper.hasBluetoothSettings() && !SettingsHelper.hasWifiSettings()) {
            DialogFragment dialog = RMPDAlertDialogFragment.getNoSettingsDialog();
            showDialog(dialog);
            return;

            // Show wifi specific dialog.
        } else if (SettingsHelper.isWifi() && !SettingsHelper.hasWifiSettings()) {
            DialogFragment dialog = RMPDAlertDialogFragment.getNoWifiSettingsDialog();
            showDialog(dialog);
            return;
        }

        if (SettingsHelper.isWifi() && !wifiIsEnabled()) {
            enableAndConnectWifi();

        } else if (SettingsHelper.isBluetooth() && !bluetoothIsEnabled()) {
            enableAndConnectBluetooth();

        } else {
            checkConnection();
        }
    }

    private boolean wifiIsEnabled() {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        return wifi.isWifiEnabled();
    }

    private boolean bluetoothIsEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter.isEnabled();
    }

    private void enableAndConnectWifi() {
        WifiConnectionAsyncTask task = new WifiConnectionAsyncTask(currentActivity);
        task.execute((Void) null);
    }

    private void enableAndConnectBluetooth() {
        Intent btEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        currentActivity.startActivityForResult(btEnableIntent, REQUEST_ENABLE_BT);
    }

    private void checkConnection() {
        if (mpdManager != null && !mpdManager.isConnected() && !connectionInProgress) {
            mpdManager.connect();
            connectionInProgress = true;
        }
    }

    private void showDialog(DialogFragment dialog) {
        if(currentActivity == null) {
            Log.e(TAG, "currentActivity was null");
            return;
        }
        dismissDialog();
        FragmentTransaction ft = currentActivity.getFragmentManager().beginTransaction();
        Fragment prev = currentActivity.getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dialog.show(currentActivity.getFragmentManager(), "dialog");
    }

    public void dismissDialog() {
        FragmentTransaction ft = currentActivity.getFragmentManager().beginTransaction();
        Fragment fragment = currentActivity.getFragmentManager().findFragmentByTag("dialog");
        if (fragment != null) {
            DialogFragment dialog = (DialogFragment) fragment;
            if (dialog.getDialog() != null) {
                dialog.getDialog().dismiss();
            }
            ft.remove(fragment);
        }
        ft.addToBackStack(null);
    }

    public AbstractMPDManager getMpdManager() {
        if (SettingsHelper.isBluetooth()) {
            if (mpdManager == null || mpdManager instanceof WifiMPDManager)
                mpdManager = new BluetoothMPDManager();
        } else {
            if (mpdManager == null || mpdManager instanceof BluetoothMPDManager)
                mpdManager = new WifiMPDManager();
        }
        return mpdManager;
    }

    public void connectMPDManager() {
        getMpdManager();
        checkState();
        if(!mpdManager.isConnected() && !connectionInProgress) {
            mpdManager.connect();
            connectionInProgress = true;
        }
    }

    public void notifyConnectionFailed(String message) {
        if (currentActivity != null) {
            dismissDialog();
            maybeShowConnectionFailedDialog(message);
        }
        connectionInProgress = false;
    }

    public void notifyConnectionSucceeded(String message) {
        if (currentActivity != null) {
            dismissDialog();
        } else {
            Log.w(TAG, "notifyConnectionSucceeded called with null currentActivity");
        }
        connectionInProgress = false;
    }

    public void notifyRefusedBluetoothConnection() {
        DialogFragment dialog = RMPDAlertDialogFragment.getRefuseBluetoothEnableDialog();
        showDialog(dialog);
    }

    private void maybeShowConnectionFailedDialog(String message) {
        if (currentActivity == null
                || currentActivity instanceof SettingsActivity
                || mpdManager.isConnected()) {
            return;
        }
        DialogFragment dialog = RMPDAlertDialogFragment.getConnectionFailedDialog(message);
        showDialog(dialog);
    }

    public void showConnectingProgressDialog() {
        if (currentActivity instanceof SettingsActivity) {
            return;
        }
        DialogFragment dialog = RMPDAlertDialogFragment.getConnectionProgressDialog();
        showDialog(dialog);
    }

    public void addMpdManagerChangeListener(MPDManagerChangeListener listener) {
        mpdManagerChangeListeners.add(listener);
    }

    public void removeMpdManagerChangeListener(MPDManagerChangeListener listener) {
        mpdManagerChangeListeners.remove(listener);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i(TAG, "Shared preferences changed: " + key);
        if (key.equals(getString(R.string.remoteMpdConnectionTypeKey))) { //TODO change this to a constant
            fireMPDManagerChanged();
        }
    }

    private void fireMPDManagerChanged() {
        for (MPDManagerChangeListener listener : mpdManagerChangeListeners) {
            listener.mpdManagerChanged();
        }
    }

    private static void checkInstance() {
        if (instance == null)
            throw new IllegalStateException("Application not created yet!");
    }
}
