/*
 * MIT License
 *
 * Copyright (c) 2017 Inova IT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package si.inova.neatle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import si.inova.neatle.monitor.Connection;
import si.inova.neatle.monitor.ConnectionHandler;
import si.inova.neatle.monitor.ConnectionStateListener;
import si.inova.neatle.operation.CharacteristicsChangedListener;
import si.inova.neatle.operation.CommandResult;
import si.inova.neatle.util.NeatleLogger;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class Device implements Connection {

    private static final long DISCOVER_DEVICE_TIMEOUT = 60 * 1000;
    private static BluetoothGattCallback DO_NOTHING_CALLBACK = new BluetoothGattCallback() {
    };

    private final BluetoothDevice device;
    public final Handler handler = new Handler();
    private final GattCallback callback = new GattCallback();
    private final Object lock = new Object();

    private int state;
    private Context context;
    private final BluetoothAdapter adapter;
    private final LinkedList<BluetoothGattCallback> queue = new LinkedList<>();
    private BluetoothGattCallback currentCallback = DO_NOTHING_CALLBACK;
    private boolean serviceDiscovered;
    private BluetoothGatt gatt;

    private final CopyOnWriteArrayList<ConnectionHandler> connectionHandlers = new CopyOnWriteArrayList<>();
    private final HashMap<UUID, CopyOnWriteArrayList<CharacteristicsChangedListener>> changeListeners = new HashMap<>();
    private final CopyOnWriteArrayList<ConnectionStateListener> connectionStateListeners = new CopyOnWriteArrayList<>();

    private BluetoothAdapter.LeScanCallback discoverCallback = new ScanForDeviceCallback();
    private Runnable discoverWatchdog = new ScanForDeviceTimeout();

    public Device(Context context, BluetoothDevice device, BluetoothAdapter adapter) {
        this.device = device;
        this.context = context.getApplicationContext();
        this.adapter = adapter;
    }

    @Override
    public void addConnectionHandler(ConnectionHandler handler) {
        connectionHandlers.add(handler);
    }

    @Override
    public void removeConnectionHandler(ConnectionHandler handler) {
        connectionHandlers.remove(handler);
        //check if we now can disconnect on idle
        disconnectOnIdle();
    }

    @Override
    public void addConnectionStateListener(ConnectionStateListener connectionStateListener) {
        connectionStateListeners.add(connectionStateListener);
    }

    @Override
    public void removeConnectionStateListener(ConnectionStateListener connectionStateListener) {
        connectionStateListeners.remove(connectionStateListener);
    }

    @Override
    public BluetoothDevice getDevice() {
        return device;
    }

    @Override
    public BluetoothGattService getService(UUID serviceUUID) {
        synchronized (lock) {
            return gatt == null ? null : gatt.getService(serviceUUID);
        }
    }

    @Override
    public List<BluetoothGattService> getServices() {
        synchronized (lock) {
            return gatt == null ? null : gatt.getServices();
        }
    }

    @Override
    public int getState() {
        synchronized (lock) {
            return state;
        }
    }

    @Override
    public void addCharacteristicsChangedListener(UUID characteristicsUUID, CharacteristicsChangedListener listener) {
        synchronized (lock) {
            CopyOnWriteArrayList<CharacteristicsChangedListener> list = changeListeners.get(characteristicsUUID);
            if (list == null) {
                list = new CopyOnWriteArrayList<>();
                list.add(listener);
                changeListeners.put(characteristicsUUID, list);
            } else if (!list.contains(listener)) {
                list.add(listener);
            }
        }
    }

    @Override
    public int getCharacteristicsChangedListenerCount(UUID characteristicsUUID) {
        synchronized (lock) {
            CopyOnWriteArrayList<CharacteristicsChangedListener> list = changeListeners.get(characteristicsUUID);
            return list == null ? 0 : list.size();

        }
    }

    @Override
    public void removeCharacteristicsChangedListener(UUID characteristicsUUID, CharacteristicsChangedListener listener) {
        boolean checkIdle;
        synchronized (lock) {
            CopyOnWriteArrayList<CharacteristicsChangedListener> list = changeListeners.get(characteristicsUUID);
            if (list != null) {
                list.remove(listener);
                if (list.isEmpty()) {
                    changeListeners.remove(characteristicsUUID);
                }
            }
            checkIdle = currentCallback == DO_NOTHING_CALLBACK && queue.isEmpty() && queue.isEmpty();
        }
        if (checkIdle) {
            disconnectOnIdle();
        }
    }

    private void notifyCharacteristicChange(final CommandResult change) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                CopyOnWriteArrayList<CharacteristicsChangedListener> list;
                synchronized (lock) {
                    list = changeListeners.get(change.getUUID());
                }
                if (list == null) {
                    NeatleLogger.d("Got a characteristic change, but nobody is interested");
                    return;
                }
                for (CharacteristicsChangedListener listener : list) {
                    listener.onCharacteristicChanged(change);
                }
            }
        });
    }

    public void execute(BluetoothGattCallback callback) {
        NeatleLogger.d("Execute " + callback);
        boolean wasIdle;
        synchronized (lock) {
            wasIdle = currentCallback == DO_NOTHING_CALLBACK;
            if (currentCallback == callback || queue.contains(callback)) {
                NeatleLogger.d("Restarting " + callback);
            } else {
                NeatleLogger.d("Queueing up " + callback);
                queue.add(callback);
            }
        }
        if (wasIdle && areServicesDiscovered()) {
            resume();
        } else {
            connect();
        }
    }

    private void disconnectOnIdle() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                boolean keepAlive = false;
                for (ConnectionHandler handler : connectionHandlers) {
                    int chRet = handler.onConnectionIdle(Device.this);
                    keepAlive = keepAlive || chRet == ConnectionHandler.ON_IDLE_KEEP_ALIVE;
                }

                synchronized (lock) {
                    if (!changeListeners.isEmpty()) {
                        NeatleLogger.i("Idle, but subscriptions are keeping the connection alive - listening for notifications/indications");
                        return;
                    }
                    //check again, in case some scheduled a new operation in the mean time
                    if (currentCallback != DO_NOTHING_CALLBACK || !queue.isEmpty()) {
                        return;
                    }
                    if (keepAlive) {
                        NeatleLogger.i("Idle, but keeping the connection alive - keep alive set");
                        return;
                    }
                    if (gatt != null) {
                        NeatleLogger.i("Disconnecting on idle");
                        disconnect();
                    }
                }
            }
        });
    }

    private void resume() {
        BluetoothGattCallback target;
        BluetoothGatt targetGatt;
        boolean doResume;

        synchronized (lock) {
            if (currentCallback == DO_NOTHING_CALLBACK) {
                BluetoothGattCallback newCallback = queue.poll();
                if (newCallback == null) {
                    if (changeListeners.isEmpty()) {
                        disconnectOnIdle();
                    }
                    return;
                }
                currentCallback = newCallback;
            }
            target = currentCallback;
            doResume = areServicesDiscovered();
            targetGatt = this.gatt;
        }

        if (doResume) {
            NeatleLogger.i("Resuming with " + target);
            currentCallback.onServicesDiscovered(targetGatt, BluetoothGatt.GATT_SUCCESS);
        } else {
            NeatleLogger.i("Will resume after services are discovered with " + target);
            connect();
        }
    }

    public boolean areServicesDiscovered() {
        synchronized (lock) {
            return serviceDiscovered && state == BluetoothGatt.STATE_CONNECTED;
        }
    }

    @Override
    public void disconnect() {
        NeatleLogger.i("Disconnecting");
        stopDiscovery();
        BluetoothGatt target;
        int oldState;

        synchronized (lock) {
            target = gatt;
            gatt = null;
            this.serviceDiscovered = false;
            oldState = state;
            state = BluetoothGatt.STATE_DISCONNECTED;
        }
        if (target != null) {
            target.disconnect();
        }
        notifyConnectionStateChange(oldState, BluetoothGatt.STATE_DISCONNECTED);
    }

    private void discoverDevice() {
        boolean isScanning = false;

        do {
            if (adapter == null || adapter.getState() != BluetoothAdapter.STATE_ON) {
                isScanning = false;
                break;
            }

            //FIXME: Switch to non-deprecated method.
            if (!adapter.startLeScan(discoverCallback)) {
                isScanning = true;
                break;
            }
        } while (false);

        if (isScanning) {
            NeatleLogger.e("Failed to start device discovery. Failing connection attempt");
            connectionFailed(BluetoothGatt.GATT_FAILURE);
        } else {
            handler.postDelayed(discoverWatchdog, DISCOVER_DEVICE_TIMEOUT);
        }
    }

    private void deviceDiscovered(BluetoothDevice device) {
        stopDiscovery();

        int state = getState();
//        if (device.getType() != BluetoothDevice.DEVICE_TYPE_UNKNOWN) {
        if (state == BluetoothGatt.STATE_CONNECTING) {
            NeatleLogger.i("Device discovered. Continuing with connecting");
            connectWithGatt();
        } else {
            NeatleLogger.e("Device discovered but no longer connecting");
        }
//        } else {
//            NeatleLogger.e("Device discovered but is of unknown type.");
//            connectionFailed(BluetoothGatt.GATT_FAILURE);
//        }
    }

    private void stopDiscovery() {
        if (adapter != null) {
            adapter.stopLeScan(discoverCallback);
        }
        handler.removeCallbacks(discoverWatchdog);
    }

    public void executeFinished(BluetoothGattCallback callback) {
        synchronized (lock) {
            if (callback == currentCallback) {
                this.currentCallback = DO_NOTHING_CALLBACK;
                NeatleLogger.d("Finished " + callback);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        resume();
                    }
                });
            } else {
                this.queue.remove(callback);
                NeatleLogger.d("Removed from queue " + callback);
            }
        }
    }

    @Override
    public void connect() {
        int oldState;
        int newState;
        boolean doConnectGatt = false;
        boolean doDiscovery = false;
        boolean adapterEnabled = adapter != null && adapter.isEnabled();

        synchronized (lock) {
            if (isConnected() || isConnecting()) {
                return;
            }

            oldState = state;
            if (!adapterEnabled) {
                newState = BluetoothAdapter.STATE_OFF;
                NeatleLogger.d("BT off. Won't connect to " + device.getName() + "[" + device.getAddress() + "]");
            } else {
                newState = BluetoothGatt.STATE_CONNECTING;
                if (device.getType() == BluetoothDevice.DEVICE_TYPE_UNKNOWN) {
                    doDiscovery = true;
                } else {
                    doConnectGatt = true;
                }
            }
        }
        //call these methods outside of the lock, to prevent deadlocks
        if (doConnectGatt) {
            connectWithGatt();
            return;
        }
        synchronized (lock) {
            state = newState;
        }

        notifyConnectionStateChange(oldState, newState);

        if (doDiscovery) {
            discoverDevice();
        }
    }

    private void connectWithGatt() {
        int oldState;
        int newState = BluetoothGatt.STATE_CONNECTING;

        if (this.gatt != null) {
            gatt.connect();
            return;
        }

        synchronized (lock) {
            oldState = state;
            state = BluetoothGatt.STATE_CONNECTING;
        }

        NeatleLogger.d("Connecting with " + device.getName() + "[" + device.getAddress() + "]");
        BluetoothGatt gatt = device.connectGatt(context, false, callback);

        synchronized (lock) {
            this.gatt = gatt;
            if (gatt == null) {
                state = BluetoothGatt.STATE_DISCONNECTED;
                newState = BluetoothGatt.STATE_DISCONNECTED;
            }
        }

        notifyConnectionStateChange(oldState, newState);
    }

    private void notifyConnectionStateChange(final int oldState, final int newState) {
        NeatleLogger.d("notifyConnectionStateChange from " + oldState + " to " + newState);
        if (oldState == newState) {
            return;
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (Device.this.lock) {
                    //state has changed after we scheduled this runnable
                    if (newState != state) {
                        NeatleLogger.d("notifyConnectionStateChange expired. Was " + oldState + " to " + newState + " but its " + state + " now");
                        return;
                    }
                }
                for (ConnectionStateListener l : connectionStateListeners) {
                    l.onConnectionStateChanged(Device.this, newState);
                }
            }
        });
    }

    private void connectionFailed(int status) {
        connectionFailed(status, true);
    }

    private void connectionFailed(int status, boolean disconnect) {
        BluetoothGattCallback current;
        int oldState;
        int newState;
        LinkedList<BluetoothGattCallback> queueCopy;


        synchronized (lock) {
            oldState = state;
            state = BluetoothGatt.STATE_DISCONNECTED;
            newState = state;
            serviceDiscovered = false;
            current = currentCallback;
            queueCopy = new LinkedList<>(queue);
        }

        NeatleLogger.i("Connection attempt failed. Notifying all pending operations");

        current.onConnectionStateChange(this.gatt, status, BluetoothGatt.STATE_DISCONNECTED);

        for (BluetoothGattCallback cb : queueCopy) {
            cb.onConnectionStateChange(gatt, status, BluetoothGatt.STATE_DISCONNECTED);
        }

        if (disconnect) {
            synchronized (lock) {
                this.gatt = null;
            }
        }

        notifyConnectionStateChange(oldState, newState);
    }

    private void connectionSuccess() {
        int oldState;
        int newState;
        synchronized (lock) {
            serviceDiscovered = false;
            oldState = state;
            state = BluetoothGatt.STATE_CONNECTED;
            newState = state;
        }

        notifyConnectionStateChange(oldState, newState);
    }


    public boolean isConnecting() {
        synchronized (lock) {
            return state == BluetoothGatt.STATE_CONNECTING;
        }
    }

    public boolean isConnected() {
        synchronized (lock) {
            return state == BluetoothGatt.STATE_CONNECTED;
        }
    }

    /*public void yield(BluetoothGattCallback from, BluetoothGattCallback to) {
        synchronized (lock) {
            if (currentCallback == from) {
                queue.add(0, currentCallback);
                currentCallback = to;
                NeatleLogger.d("Yielded to " + to);
            } else {
                throw new IllegalArgumentException("Cannot yield. Operation not in progress");
            }
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                resume();
            }
        });

    }*/

    private class GattCallback extends BluetoothGattCallback {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            boolean didConnect = false;
            NeatleLogger.d("onConnectionStateChange status: " + status + " newState:" + newState);
            if (newState == BluetoothGatt.STATE_CONNECTED && status == BluetoothGatt.GATT_SUCCESS) {
                didConnect = gatt.discoverServices();
            }

            if (!didConnect) {
                // status code 19 should not be handled like a true disconnect
                if (status != 19) {
                  gatt.close();
                }

                connectionFailed(status, status != 19);
            } else {
                connectionSuccess();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                connectionFailed(status);
                return;
            }

            synchronized (lock) {
                serviceDiscovered = true;
            }
            resume();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            NeatleLogger.d("createCharacteristicRead");
            BluetoothGattCallback target;
            synchronized (lock) {
                target = currentCallback;
            }
            target.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            NeatleLogger.d("onCharacteristicWrite " + status);
            BluetoothGattCallback target;
            synchronized (lock) {
                target = currentCallback;
            }
            target.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            NeatleLogger.d("onReliableWriteCompleted");
            BluetoothGattCallback target;
            synchronized (lock) {
                target = currentCallback;
            }
            target.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            BluetoothGattCallback target;
            synchronized (lock) {
                target = currentCallback;
            }
            target.onCharacteristicChanged(gatt, characteristic);

            notifyCharacteristicChange(CommandResult.createCharacteristicChanged(characteristic));
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            BluetoothGattCallback target;
            synchronized (lock) {
                target = currentCallback;
            }
            target.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            BluetoothGattCallback target;
            synchronized (lock) {
                target = currentCallback;
            }
            target.onDescriptorWrite(gatt, descriptor, status);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            BluetoothGattCallback target;
            synchronized (lock) {
                target = currentCallback;
            }
            target.onMtuChanged(gatt, mtu, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            BluetoothGattCallback target;
            synchronized (lock) {
                target = currentCallback;
            }
            target.onReadRemoteRssi(gatt, rssi, status);
        }
    }

    private class ScanForDeviceCallback implements BluetoothAdapter.LeScanCallback {
        @Override
        public void onLeScan(BluetoothDevice found, int rssi, byte[] scanRecord) {
            if (found.getAddress().equals(device.getAddress())) {
                deviceDiscovered(found);
            }
        }
    }

    private class ScanForDeviceTimeout implements Runnable {
        public void run() {
            stopDiscovery();

            int state = getState();
            if (state == BluetoothGatt.STATE_CONNECTING) {
                NeatleLogger.e("Device no discovered failing connection attempt.");
                connectionFailed(BluetoothGatt.GATT_FAILURE);
            } else {
                NeatleLogger.e("Discover timeout but we are not connecting anymore.");
            }
        }
    }

}
