package si.inova.neatle.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import si.inova.neatle.util.NeatleLogger;

/**
 * Created by tomazs on 17. 05. 2017.
 */
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
            ScanEvent se = new ScanEvent(device, rssi, scanRecord);
            onScanEvent(se);
        }
    }

}
