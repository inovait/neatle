package si.inova.neatle.scan;

import java.util.UUID;

/**
 * Created by tomazs on 25. 05. 2017.
 */

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
}
