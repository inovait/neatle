package si.inova.neatle;

import android.bluetooth.BluetoothDevice;
import android.os.Build;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import si.inova.neatle.util.DeviceManager;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.N_MR1)
public class DeviceManagerTest {

    private DeviceManager deviceManager;

    @Before
    public void setUp() {
        deviceManager = DeviceManager.getInstance(RuntimeEnvironment.application);
    }

    @Test
    public void instanceTest() {
        assertNotNull(DeviceManager.getInstance(RuntimeEnvironment.application));
    }

    @Test
    public void getDeviceTest() {
        BluetoothDevice bluetoothDevice = Mockito.mock(BluetoothDevice.class);
        Mockito.when(bluetoothDevice.getAddress()).thenReturn("00:11:22:33:44:55");

        assertNotNull(deviceManager.getDevice(bluetoothDevice));
        assertNotNull(deviceManager.getDevice(bluetoothDevice));
    }
}
