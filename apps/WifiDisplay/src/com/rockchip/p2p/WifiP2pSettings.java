/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rockchip.p2p;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pGroupList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pManager.PersistentGroupInfoListener;
import android.net.wifi.WpsInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.rockchip.wfd.NetworkDetecting;
import com.rockchip.wfd.R;

import java.util.Arrays;
import java.util.List;
import java.util.Collection;

/*
 * Displays Wi-fi p2p settings UI
 */
public class WifiP2pSettings extends PreferenceActivity
        implements PeerListListener, PersistentGroupInfoListener, GroupInfoListener {

    private static final String TAG = "WifiP2pSettings2";
    private static final boolean DBG = false;
    private static final int MENU_ID_SEARCH = Menu.FIRST;
    private static final int MENU_ID_RENAME = Menu.FIRST + 1;

    private final IntentFilter mIntentFilter = new IntentFilter();
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private OnClickListener mRenameListener;
    private OnClickListener mDisconnectListener;
    private OnClickListener mCancelConnectListener;
    private OnClickListener mDeleteGroupListener;
    private WifiP2pPeer mSelectedWifiPeer;
    private WifiP2pPersistentGroup mSelectedGroup;
    private EditText mDeviceNameText;

    private boolean mWifiP2pEnabled;
    private boolean mWifiP2pSearching;
    private int mConnectedDevices;
    private WifiP2pGroup mConnectedGroup;

    private PreferenceGroup mPeersGroup;
    private PreferenceGroup mPersistentGroup;
    private Preference mThisDevicePref;

    private static final int DIALOG_DISCONNECT  = 1;
    private static final int DIALOG_CANCEL_CONNECT = 2;
    private static final int DIALOG_RENAME = 3;
    private static final int DIALOG_DELETE_GROUP = 4;

    private static final String SAVE_DIALOG_PEER = "PEER_STATE";
    private static final String SAVE_DEVICE_NAME = "DEV_NAME";

    private WifiP2pDevice mThisDevice;
    private WifiP2pDeviceList mPeers = new WifiP2pDeviceList();

    private String mSavedDeviceName;
    
    private NetworkDetecting mNetworkDetecting;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                mWifiP2pEnabled = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,
                    WifiP2pManager.WIFI_P2P_STATE_DISABLED) == WifiP2pManager.WIFI_P2P_STATE_ENABLED;
                handleP2pStateChanged();
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                if (mWifiP2pManager != null) {
                    mWifiP2pManager.requestPeers(mChannel, WifiP2pSettings.this);
                }
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                if (mWifiP2pManager == null) return;
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(
                        WifiP2pManager.EXTRA_NETWORK_INFO);
                if (mWifiP2pManager != null) {
                    mWifiP2pManager.requestGroupInfo(mChannel, WifiP2pSettings.this);
                }
                if (networkInfo.isConnected()) {
                    if (DBG) Log.d(TAG, "Connected");
                } else {
                    //start a search when we are disconnected
                    startSearch();
                }
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                mThisDevice = (WifiP2pDevice) intent.getParcelableExtra(
                        WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                if (DBG) Log.d(TAG, "Update device info: " + mThisDevice);
                updateDevicePref();
            } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
                int discoveryState = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE,
                    WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED);
                if (DBG) Log.d(TAG, "Discovery state changed: " + discoveryState);
                if (discoveryState == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {
                    updateSearchMenu(true);
                } else {
                    updateSearchMenu(false);
                }
            } else if (WifiP2pManager.ACTION_WIFI_P2P_PERSISTENT_GROUPS_CHANGED.equals(action)) {
                if (mWifiP2pManager != null) {
                    mWifiP2pManager.requestPersistentGroupInfo(mChannel, WifiP2pSettings.this);
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	addPreferencesFromResource(R.xml.wifi_p2p_settings);
    	setContentView(R.layout.wifip2p);
    	mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
    	mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.ACTION_WIFI_P2P_PERSISTENT_GROUPS_CHANGED);

        mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        if (mWifiP2pManager != null) {
            mChannel = mWifiP2pManager.initialize(this, getMainLooper(), null);
            if (mChannel == null) {
                //Failure to set up connection
                Log.e(TAG, "Failed to set up connection with wifi p2p service");
                mWifiP2pManager = null;
            }
        } else {
            Log.e(TAG, "mWifiP2pManager is null !");
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(SAVE_DIALOG_PEER)) {
            WifiP2pDevice device = savedInstanceState.getParcelable(SAVE_DIALOG_PEER);
            mSelectedWifiPeer = new WifiP2pPeer(this, device);
        }
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVE_DEVICE_NAME)) {
            mSavedDeviceName = savedInstanceState.getString(SAVE_DEVICE_NAME);
        }

        mRenameListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    if (mWifiP2pManager != null) {
                        mWifiP2pManager.setDeviceName(mChannel,
                                mDeviceNameText.getText().toString(),
                                new WifiP2pManager.ActionListener() {
                            public void onSuccess() {
                                if (DBG) Log.d(TAG, " device rename success");
                            }
                            public void onFailure(int reason) {
                                Toast.makeText(WifiP2pSettings.this,
                                        R.string.wifi_p2p_failed_rename_message,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        };

        //disconnect dialog listener
        mDisconnectListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    if (mWifiP2pManager != null) {
                        mWifiP2pManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                            public void onSuccess() {
                                if (DBG) Log.d(TAG, " remove group success");
                            }
                            public void onFailure(int reason) {
                                if (DBG) Log.d(TAG, " remove group fail " + reason);
                            }
                        });
                    }
                }
            }
        };

        //cancel connect dialog listener
        mCancelConnectListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    if (mWifiP2pManager != null) {
                        mWifiP2pManager.cancelConnect(mChannel,
                                new WifiP2pManager.ActionListener() {
                            public void onSuccess() {
                                if (DBG) Log.d(TAG, " cancel connect success");
                            }
                            public void onFailure(int reason) {
                                if (DBG) Log.d(TAG, " cancel connect fail " + reason);
                            }
                        });
                    }
                }
            }
        };

        //delete persistent group dialog listener
        mDeleteGroupListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    if (mWifiP2pManager != null) {
                        mWifiP2pManager.deletePersistentGroup(mChannel,
                                mSelectedGroup.getNetworkId(),
                                new WifiP2pManager.ActionListener() {
                            public void onSuccess() {
                                if (DBG) Log.d(TAG, " delete group success");
                            }
                            public void onFailure(int reason) {
                                if (DBG) Log.d(TAG, " delete group fail " + reason);
                            }
                        });
                    }
                }
            }
        };

//        setHasOptionsMenu(true);

        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        preferenceScreen.removeAll();

        preferenceScreen.setOrderingAsAdded(true);
        mThisDevicePref = new Preference(this);
        preferenceScreen.addPreference(mThisDevicePref);

        mPeersGroup = new PreferenceCategory(this);
        mPeersGroup.setTitle(R.string.wifi_p2p_peer_devices);

        mPersistentGroup = new PreferenceCategory(this);
        mPersistentGroup.setTitle(R.string.wifi_p2p_remembered_groups);
        mNetworkDetecting = new NetworkDetecting(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mNetworkDetecting.detectWifiEnable();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        mWifiP2pManager.stopPeerDiscovery(mChannel, null);
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	int textId = mWifiP2pSearching ? R.string.wifi_p2p_menu_searching :
            R.string.wifi_p2p_menu_search;
	    menu.add(Menu.NONE, MENU_ID_SEARCH, 0, textId)
	        .setEnabled(mWifiP2pEnabled)
	        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	    menu.add(Menu.NONE, MENU_ID_RENAME, 0, R.string.wifi_p2p_menu_rename)
	        .setEnabled(mWifiP2pEnabled)
	        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    	return super.onCreateOptionsMenu(menu);
    }
    

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem searchMenu = menu.findItem(MENU_ID_SEARCH);
        MenuItem renameMenu = menu.findItem(MENU_ID_RENAME);
        if (mWifiP2pEnabled) {
            searchMenu.setEnabled(true);
            renameMenu.setEnabled(true);
        } else {
            searchMenu.setEnabled(false);
            renameMenu.setEnabled(false);
        }

        if (mWifiP2pSearching) {
            searchMenu.setTitle(R.string.wifi_p2p_menu_searching);
        } else {
            searchMenu.setTitle(R.string.wifi_p2p_menu_search);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ID_SEARCH:
                startSearch();
                return true;
            case MENU_ID_RENAME:
                showDialog(DIALOG_RENAME);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
        if (preference instanceof WifiP2pPeer) {
            mSelectedWifiPeer = (WifiP2pPeer) preference;
            if (mSelectedWifiPeer.device.status == WifiP2pDevice.CONNECTED) {
                showDialog(DIALOG_DISCONNECT);
            } else if (mSelectedWifiPeer.device.status == WifiP2pDevice.INVITED) {
                showDialog(DIALOG_CANCEL_CONNECT);
            } else {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = mSelectedWifiPeer.device.deviceAddress;

                int forceWps = SystemProperties.getInt("wifidirect.wps", -1);

                if (forceWps != -1) {
                    config.wps.setup = forceWps;
                } else {
                    if (mSelectedWifiPeer.device.wpsPbcSupported()) {
                        config.wps.setup = WpsInfo.PBC;
                    } else if (mSelectedWifiPeer.device.wpsKeypadSupported()) {
                        config.wps.setup = WpsInfo.KEYPAD;
                    } else {
                        config.wps.setup = WpsInfo.DISPLAY;
                    }
                }

                mWifiP2pManager.connect(mChannel, config,
                        new WifiP2pManager.ActionListener() {
                            public void onSuccess() {
                                if (DBG) Log.d(TAG, " connect success");
                            }
                            public void onFailure(int reason) {
                                Log.e(TAG, " connect fail " + reason);
                                Toast.makeText(WifiP2pSettings.this,
                                        R.string.wifi_p2p_failed_connect_message,
                                        Toast.LENGTH_SHORT).show();
                            }
                    });
            }
        } else if (preference instanceof WifiP2pPersistentGroup) {
            mSelectedGroup = (WifiP2pPersistentGroup) preference;
            showDialog(DIALOG_DELETE_GROUP);
        }
        return super.onPreferenceTreeClick(screen, preference);
    }
    

    @Override
    public Dialog onCreateDialog(int id, Bundle args) {
        if (id == DIALOG_DISCONNECT) {
            String deviceName = TextUtils.isEmpty(mSelectedWifiPeer.device.deviceName) ?
                    mSelectedWifiPeer.device.deviceAddress :
                    mSelectedWifiPeer.device.deviceName;
            String msg;
            if (mConnectedDevices > 1) {
                msg = getString(R.string.wifi_p2p_disconnect_multiple_message,
                        deviceName, mConnectedDevices - 1);
            } else {
                msg = getString(R.string.wifi_p2p_disconnect_message, deviceName);
            }
            AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.wifi_p2p_disconnect_title)
                .setMessage(msg)
                .setPositiveButton(getString(R.string.dlg_ok), mDisconnectListener)
                .setNegativeButton(getString(R.string.dlg_cancel), null)
                .create();
            return dialog;
        } else if (id == DIALOG_CANCEL_CONNECT) {
            int stringId = R.string.wifi_p2p_cancel_connect_message;
            String deviceName = TextUtils.isEmpty(mSelectedWifiPeer.device.deviceName) ?
                    mSelectedWifiPeer.device.deviceAddress :
                    mSelectedWifiPeer.device.deviceName;

            AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.wifi_p2p_cancel_connect_title)
                .setMessage(getString(stringId, deviceName))
                .setPositiveButton(getString(R.string.dlg_ok), mCancelConnectListener)
                .setNegativeButton(getString(R.string.dlg_cancel), null)
                .create();
            return dialog;
        } else if (id == DIALOG_RENAME) {
            mDeviceNameText = new EditText(this);
            if (mSavedDeviceName != null) {
                mDeviceNameText.setText(mSavedDeviceName);
                mDeviceNameText.setSelection(mSavedDeviceName.length());
            } else if (mThisDevice != null && !TextUtils.isEmpty(mThisDevice.deviceName)) {
                mDeviceNameText.setText(mThisDevice.deviceName);
                mDeviceNameText.setSelection(0, mThisDevice.deviceName.length());
            }
            mSavedDeviceName = null;
            AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.wifi_p2p_menu_rename)
                .setView(mDeviceNameText)
                .setPositiveButton(getString(R.string.dlg_ok), mRenameListener)
                .setNegativeButton(getString(R.string.dlg_cancel), null)
                .create();
            return dialog;
        } else if (id == DIALOG_DELETE_GROUP) {
            int stringId = R.string.wifi_p2p_delete_group_message;

            AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(getString(stringId))
                .setPositiveButton(getString(R.string.dlg_ok), mDeleteGroupListener)
                .setNegativeButton(getString(R.string.dlg_cancel), null)
                .create();
            return dialog;
        }
        return super.onCreateDialog(id, null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mSelectedWifiPeer != null) {
            outState.putParcelable(SAVE_DIALOG_PEER, mSelectedWifiPeer.device);
        }
        if (mDeviceNameText != null) {
            outState.putString(SAVE_DEVICE_NAME, mDeviceNameText.getText().toString());
        }
    }

    public void onPeersAvailable(WifiP2pDeviceList peers) {
        mPeersGroup.removeAll();

        mPeers = peers;
        mConnectedDevices = 0;
        for (WifiP2pDevice peer: peers.getDeviceList()) {
            if (DBG) Log.d(TAG, " peer " + peer);
            mPeersGroup.addPreference(new WifiP2pPeer(this, peer));
            if (peer.status == WifiP2pDevice.CONNECTED) mConnectedDevices++;
        }
        if (DBG) Log.d(TAG, " mConnectedDevices " + mConnectedDevices);
    }

    public void onPersistentGroupInfoAvailable(WifiP2pGroupList groups) {
        mPersistentGroup.removeAll();

        for (WifiP2pGroup group: groups.getGroupList()) {
            if (DBG) Log.d(TAG, " group " + group);
            mPersistentGroup.addPreference(new WifiP2pPersistentGroup(this, group));
        }
    }

    public void onGroupInfoAvailable(WifiP2pGroup group) {
        if (DBG) Log.d(TAG, " group " + group);
        mConnectedGroup = group;
        updateDevicePref();
    }

    private void handleP2pStateChanged() {
        updateSearchMenu(false);
        if (mWifiP2pEnabled) {
            final PreferenceScreen preferenceScreen = getPreferenceScreen();
            preferenceScreen.removeAll();

            preferenceScreen.setOrderingAsAdded(true);
            preferenceScreen.addPreference(mThisDevicePref);

            mPeersGroup.setEnabled(true);
            preferenceScreen.addPreference(mPeersGroup);

            mPersistentGroup.setEnabled(true);
            preferenceScreen.addPreference(mPersistentGroup);

            /* Request latest set of peers */
            mWifiP2pManager.requestPeers(mChannel, WifiP2pSettings.this);
        }
    }

    private void updateSearchMenu(boolean searching) {
       mWifiP2pSearching = searching;
       invalidateOptionsMenu();
    }

    private void startSearch() {
        if (mWifiP2pManager != null && !mWifiP2pSearching) {
            mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                public void onSuccess() {
                }
                public void onFailure(int reason) {
                    if (DBG) Log.d(TAG, " discover fail " + reason);
                }
            });
        }
    }

    private void updateDevicePref() {
        if (mThisDevice != null) {
            if (TextUtils.isEmpty(mThisDevice.deviceName)) {
                mThisDevicePref.setTitle(mThisDevice.deviceAddress);
            } else {
                mThisDevicePref.setTitle(mThisDevice.deviceName);
            }

            mThisDevicePref.setPersistent(false);
            mThisDevicePref.setEnabled(true);
            mThisDevicePref.setSelectable(false);
        }
    }
}
