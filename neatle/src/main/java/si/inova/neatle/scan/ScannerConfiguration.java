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

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

class ScannerConfiguration {

    /**
     * @see android.bluetooth.le.ScanSettings#SCAN_MODE_BALANCED
     */
    int SCAN_MODE_BALANCED = 1;

    /**
     * @see android.bluetooth.le.ScanSettings#SCAN_MODE_BALANCED
     */
    int SCAN_MODE_LOW_LATENCY = 2;

    /**
     * @see android.bluetooth.le.ScanSettings#SCAN_MODE_BALANCED
     */
    int SCAN_MODE_LOW_POWER = 0;

    /**
     * @see android.bluetooth.le.ScanSettings#SCAN_MODE_BALANCED
     */
    int SCAN_MODE_OPPORTUNISTIC = -1;

    private Scanner.NewDeviceFoundListener newDeviceFoundListener;
    private Scanner.ScanEventListener scanEventListener;
    private List<UUID> serviceUUIDS = new ArrayList<>();
    private Set<String> devices = new HashSet<>();

    public void setNewDeviceFoundListener(Scanner.NewDeviceFoundListener newDeviceFoundListener) {
        this.newDeviceFoundListener = newDeviceFoundListener;
    }

    public Scanner.NewDeviceFoundListener getNewDeviceFoundListener() {
        return newDeviceFoundListener;
    }

    public Scanner.ScanEventListener getScanEventListener() {
        return scanEventListener;
    }

    public void setScanEventListener(Scanner.ScanEventListener scanEventListener) {
        this.scanEventListener = scanEventListener;
    }

    public void addServiceUUID(UUID serviceUUID) {
        serviceUUIDS.add(serviceUUID);
    }

    public UUID[] getServiceUUIDs() {
        UUID[] ret = new UUID[serviceUUIDS.size()];
        return serviceUUIDS.toArray(ret);
    }

    public void addDeviceAddress(String address) {
        devices.add(address.toUpperCase());
    }

    public boolean shouldReport(BluetoothDevice device) {
        if (!devices.isEmpty()) {
            return devices.contains(device.getAddress().toUpperCase());
        }
        return true;
    }
}
