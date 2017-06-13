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
package si.inova.neatle.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import si.inova.neatle.Neatle;
import si.inova.neatle.util.NeatleLogger;

class LolipopLEScanner implements Scanner {
    private ScanConfiguration scanConfiguration = new ScanConfiguration();

    private boolean scanning = false;
    private ScanCallbackHandler callback = new ScanCallbackHandler();

    private Map<BluetoothDevice, ScanEvent> seenDevices = new HashMap<>();

    LolipopLEScanner(ScanConfiguration settings) {
        this.scanConfiguration = settings;
    }


    @Override
    public void startScanning() {
        if (scanning) {
            return;
        }
        scanning = doStart();
    }

    private boolean doStart() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            NeatleLogger.d("Bluetooth LE scan failed to start. No bluetooth adapter found");
            return false;
        }
        boolean ret;
        UUID uuids[] = scanConfiguration.getServiceUUIDs();
        if (uuids.length > 0) {
            ret = adapter.startLeScan(uuids, callback);
        } else {
            ret = adapter.startLeScan(callback);
        }

        if (ret) {
            NeatleLogger.d("Bluetooth LE scan started.");
        } else {
            NeatleLogger.i("Bluetooth LE scan failed to start. State = " + adapter.getState());
        }
        return ret;
    }

    protected void onScanEvent(ScanEvent e) {
        ScanEvent old = seenDevices.put(e.getDevice(), e);
        if (old == null && scanConfiguration.getNewDeviceFoundListener() != null) {
            NewDeviceFoundListener listener = scanConfiguration.getNewDeviceFoundListener();
            listener.onNewDeviceFound(e);
        }

        ScanEventListener scanListener = scanConfiguration.getScanEventListener();
        if (scanListener != null) {
            scanListener.onScanEvent(e);
        }
    }

    @Override
    public void stopScanning() {
        scanning = false;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            adapter.stopLeScan(callback);
            NeatleLogger.d("Bluetooth LE scan stopped");
        }
    }

    private class ScanCallbackHandler implements BluetoothAdapter.LeScanCallback {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (!scanConfiguration.shouldReport(device)) {
//                NeatleLogger.d("Not interested in device " + device.getAddress() + ", ignoring");
                return;
            }
            ScanEvent se = new ScanEvent(device, rssi, scanRecord);

            onScanEvent(se);
        }
    }

}
