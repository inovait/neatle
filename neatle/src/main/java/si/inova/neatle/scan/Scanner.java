package si.inova.neatle.scan;

/**
 * Created by tomazs on 25. 05. 2017.
 */
public interface Scanner {

    /**
     * Starts scanning until stop scan is invoked.
     */
    void startScanning();

    /**
     * Stops scanning.
     */
    void stopScanning();

    interface NewDeviceFoundListener {

        /**
         * Invoked the first that the scanner sees the device.
         *
         * @param e
         */
        void onNewDeviceFound(ScanEvent e);
    }

    interface ScanEventListener {

        /**
         * Invoked on every scan event.
         *
         * @param e
         */
        void onScanEvent(ScanEvent e);
    }
}
