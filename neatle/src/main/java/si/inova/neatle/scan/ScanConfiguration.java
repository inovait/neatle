package si.inova.neatle.scan;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by tomazs on 17. 05. 2017.
 */

class ScanConfiguration {

    private Scanner.NewDeviceFoundListener newDeviceFoundListener;
    private Scanner.ScanEventListener scanEventListener;
    private List<UUID> serviceUUIDS = new ArrayList<>();

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
}
