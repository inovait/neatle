package si.inova.neatle.scan;

import android.bluetooth.BluetoothDevice;
import android.provider.Settings;

/**
 * Created by tomazs on 17. 05. 2017.
 */

public class ScanEvent {

    private final BluetoothDevice device;
    private final int rssi;
    private final byte[] scanRecord;
    private final long when;

    public ScanEvent (BluetoothDevice device, int rssi, byte[] scanRecord) {
        this.device = device;
        this.rssi = rssi;
        this.scanRecord = scanRecord;
        this.when = System.currentTimeMillis();
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public int getRssi() {
        return rssi;
    }

    public long getWhen() {
        return when;
    }

    @Override
    public String toString() {
        return getDevice() + ", rssi: " + getRssi() + ", advertisment " + scanRecord;
    }
}
