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

import java.util.UUID;

public class ScanBuilder {
    private ScanConfiguration scanConfiguration = new ScanConfiguration();
    private Scanner.ScanEventListener scanEventListener;

    public Scanner build() {
        return new LolipopLEScanner(scanConfiguration);
    }

    /**
     * Adds a service UUID used for service discovery. When used, only
     * devices advertising the service will be reported.
     *
     * @see https://www.bluetooth.com/specifications/assigned-numbers/service-discovery
     *
     * @param serviceUUID the serviceUUID that will
     */
    public void addServiceUUID(UUID serviceUUID) {
        scanConfiguration.addServiceUUID(serviceUUID);
    }

    public ScanBuilder setNewDeviceFoundListener(Scanner.NewDeviceFoundListener listener) {
        scanConfiguration.setNewDeviceFoundListener(listener);
        return this;
    }

    public ScanBuilder setScanEventListener(Scanner.ScanEventListener scanEventListener) {
        scanConfiguration.setScanEventListener(scanEventListener);
        return this;
    }

    public ScanBuilder addDeviceAddress(String address) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            throw new IllegalArgumentException("Invalid address:" + address);
        }
        scanConfiguration.addDeviceAddress(address);
        return null;
    }
}
