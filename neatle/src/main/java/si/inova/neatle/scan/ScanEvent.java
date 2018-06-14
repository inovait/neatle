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

/**
 * This class represents a {@link Scanner} event and holds information associted with it. For
 * example RSSI, the scan record.
 */
public final class ScanEvent {

    private final BluetoothDevice device;
    private final int rssi;
    private final ScanRecord scanRecord;
    private final long when;

    public ScanEvent (BluetoothDevice device, int rssi, byte[] scanRecord) {
        this.device = device;
        this.rssi = rssi;
        if (scanRecord != null && scanRecord.length > 0) {
            this.scanRecord = ScanRecord.createFromBytes(scanRecord);
        } else {
            this.scanRecord = null;
        }

        this.when = System.currentTimeMillis();
    }

    /**
     * @return the device that emitted this events.
     */
    public BluetoothDevice getDevice() {
        return device;
    }

    /**
     * Gets the "received strength indication".
     *
     * @return events RSSI value.
     */
    public int getRssi() {
        return rssi;
    }

    /**
     * Gets the advertisement data.
     *
     * @return the ScanRecord or null if data is not available.
     */
    public ScanRecord getScanRecord() {
        return scanRecord;
    }


    /**
     * @return the timestamp when this event was reported.
     */
    public long getWhen() {
        return when;
    }

    @Override
    public String toString() {
        return getDevice() + ", rssi: " + getRssi() + ", advertisment " + scanRecord;
    }


}
