package si.inova.neatle.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class LolipopLEScannerTest {
    private static final UUID TEST_UUID = UUID.fromString("74271566-68aa-11e8-adc0-fa7ae01bbebc");
    private static final UUID INVALID_UUID = UUID.fromString("74271566-97aa-11e8-adc0-fa7ae01bbebc");
    private ScannerConfiguration TEST_CONFIGURATION;

    @Before
    public void prepare() {
        TEST_CONFIGURATION = new ScannerConfiguration();
        TEST_CONFIGURATION.addServiceUUID(TEST_UUID);
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.LOLLIPOP)
    public void startScanWithUuidsOnLollipop() {
        BluetoothAdapter testAdapter = Mockito.mock(BluetoothAdapter.class);
        Mockito.when(testAdapter.startLeScan(Mockito.<UUID[]>any(),
                Mockito.<BluetoothAdapter.LeScanCallback>any())).thenReturn(true);

        LolipopLEScanner scanner = new LolipopLEScanner(TEST_CONFIGURATION);

        scanner.onStart(testAdapter, 0);

        Mockito.verify(testAdapter, Mockito.only()).startLeScan(Mockito.argThat(new ArgumentMatcher<UUID[]>() {
            @Override
            public boolean matches(UUID[] uuids) {
                return uuids.length == 1 && TEST_UUID.equals(uuids[0]);
            }
        }), Mockito.<BluetoothAdapter.LeScanCallback>any());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.KITKAT)
    public void startScanWithUuidsOnKitkat() {
        BluetoothAdapter testAdapter = Mockito.mock(BluetoothAdapter.class);
        Mockito.when(testAdapter.startLeScan(Mockito.<BluetoothAdapter.LeScanCallback>any())).thenReturn(true);
        LolipopLEScanner scanner = new LolipopLEScanner(TEST_CONFIGURATION);

        scanner.onStart(testAdapter, 0);

        Mockito.verify(testAdapter, Mockito.only()).startLeScan(Mockito.<BluetoothAdapter.LeScanCallback>any());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.KITKAT)
    public void filterUuidsOnKitkat() {
        ByteBuffer mockScanRecordBuffer = ByteBuffer.allocate(100).order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 17)
                .put((byte) 0x07)
                .putLong(INVALID_UUID.getLeastSignificantBits())
                .putLong(INVALID_UUID.getMostSignificantBits());

        byte[] mockScanRecord = Arrays.copyOf(mockScanRecordBuffer.array(),
                mockScanRecordBuffer.position());

        Scanner.NewDeviceFoundListener listener = Mockito.mock(Scanner.NewDeviceFoundListener.class);
        TEST_CONFIGURATION.setNewDeviceFoundListener(listener);

        ArgumentCaptor<BluetoothAdapter.LeScanCallback> callbackCaptor
                = ArgumentCaptor.forClass(BluetoothAdapter.LeScanCallback.class);

        BluetoothAdapter testAdapter = Mockito.mock(BluetoothAdapter.class);
        LolipopLEScanner scanner = new LolipopLEScanner(TEST_CONFIGURATION);

        scanner.onStart(testAdapter, 0);
        Mockito.verify(testAdapter).startLeScan(callbackCaptor.capture());

        callbackCaptor.getValue().onLeScan(Mockito.mock(BluetoothDevice.class), -10, mockScanRecord);
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.KITKAT)
    public void allowProperUuidsOnKitkat() {
        ByteBuffer mockScanRecordBuffer = ByteBuffer.allocate(100).order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 17)
                .put((byte) 0x07)
                .putLong(TEST_UUID.getLeastSignificantBits())
                .putLong(TEST_UUID.getMostSignificantBits());

        byte[] mockScanRecord = Arrays.copyOf(mockScanRecordBuffer.array(),
                mockScanRecordBuffer.position());

        Scanner.NewDeviceFoundListener listener = Mockito.mock(Scanner.NewDeviceFoundListener.class);
        ScannerConfiguration configuration = new ScannerConfiguration();
        configuration.setNewDeviceFoundListener(listener);

        ArgumentCaptor<BluetoothAdapter.LeScanCallback> callbackCaptor
                = ArgumentCaptor.forClass(BluetoothAdapter.LeScanCallback.class);

        BluetoothAdapter testAdapter = Mockito.mock(BluetoothAdapter.class);
        LolipopLEScanner scanner = new LolipopLEScanner(configuration);

        scanner.onStart(testAdapter, 0);
        Mockito.verify(testAdapter).startLeScan(callbackCaptor.capture());

        callbackCaptor.getValue().onLeScan(Mockito.mock(BluetoothDevice.class), -10, mockScanRecord);

        Mockito.verify(listener).onNewDeviceFound(Mockito.<ScanEvent>any());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.KITKAT)
    public void doNotFilterUuidsOnKitkatWithEmptyServiceList() {
        ByteBuffer mockScanRecordBuffer = ByteBuffer.allocate(100).order(ByteOrder.LITTLE_ENDIAN)
                .put((byte) 17)
                .put((byte) 0x07)
                .putLong(INVALID_UUID.getLeastSignificantBits())
                .putLong(INVALID_UUID.getMostSignificantBits());

        byte[] mockScanRecord = Arrays.copyOf(mockScanRecordBuffer.array(),
                mockScanRecordBuffer.position());

        Scanner.NewDeviceFoundListener listener = Mockito.mock(Scanner.NewDeviceFoundListener.class);
        ScannerConfiguration configuration = new ScannerConfiguration();
        configuration.setNewDeviceFoundListener(listener);

        ArgumentCaptor<BluetoothAdapter.LeScanCallback> callbackCaptor
                = ArgumentCaptor.forClass(BluetoothAdapter.LeScanCallback.class);

        BluetoothAdapter testAdapter = Mockito.mock(BluetoothAdapter.class);
        LolipopLEScanner scanner = new LolipopLEScanner(configuration);

        scanner.onStart(testAdapter, 0);
        Mockito.verify(testAdapter).startLeScan(callbackCaptor.capture());

        callbackCaptor.getValue().onLeScan(Mockito.mock(BluetoothDevice.class), -10, mockScanRecord);

        Mockito.verify(listener).onNewDeviceFound(Mockito.<ScanEvent>any());
    }
}