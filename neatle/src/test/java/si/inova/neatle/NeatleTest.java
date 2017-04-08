package si.inova.neatle;

import android.bluetooth.BluetoothDevice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19)
public class NeatleTest {

    @Test
    public void testCreateUUID() {
        assertEquals("0000180f-0000-1000-8000-00805f9b34fb", Neatle.createUUID(0x180f).toString());
        assertEquals("00000000-0000-1000-8000-00805f9b34fb", Neatle.createUUID(0).toString());
        assertEquals("0000ffff-0000-1000-8000-00805f9b34fb", Neatle.createUUID(-1).toString());
    }

    @Test
    public void testCreateSubscription() {
        BluetoothDevice bluetoothDevice = Mockito.mock(BluetoothDevice.class);
        Mockito.when(bluetoothDevice.getAddress()).thenReturn("00:11:22:33:44:55");

        assertNotNull(Neatle.createSubscription(RuntimeEnvironment.application, bluetoothDevice, Neatle.createUUID(0), Neatle.createUUID(0)));
    }

    @Test
    public void testCreateConnectionMonitor() {
        BluetoothDevice bluetoothDevice = Mockito.mock(BluetoothDevice.class);
        Mockito.when(bluetoothDevice.getAddress()).thenReturn("00:11:22:33:44:55");

        assertNotNull(Neatle.createConnectionMonitor(RuntimeEnvironment.application, bluetoothDevice));
    }

    @Test
    public void testCreateOperationBuilder() {
        assertNotNull(Neatle.createOperationBuilder(RuntimeEnvironment.application));
    }

    @Test
    public void testGetConnection() {
        BluetoothDevice bluetoothDevice = Mockito.mock(BluetoothDevice.class);
        Mockito.when(bluetoothDevice.getAddress()).thenReturn("00:11:22:33:44:55");

        assertNotNull(Neatle.getConnection(RuntimeEnvironment.application, bluetoothDevice));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetDeviceException() {
        Neatle.getDevice("no:ta:ma:c!");
    }
}
