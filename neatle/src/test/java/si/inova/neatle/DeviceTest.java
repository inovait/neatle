package si.inova.neatle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class DeviceTest {
    @Mock
    private Context context;

    @Mock
    private BluetoothDevice btDevice;

    @Mock
    private BluetoothAdapter adapter;

    private Device device;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        device = new Device(context, btDevice, adapter);
    }

    @Test
    public void connectAfterConnectWithGattFailWhileConnecting() {
        Mockito.when(btDevice.connectGatt(
                Mockito.<Context>any(),
                Mockito.anyBoolean(),
                Mockito.<BluetoothGattCallback>any())
        ).thenAnswer(new Answer<BluetoothGatt>() {
            @Override
            public BluetoothGatt answer(InvocationOnMock invocationOnMock) {
                BluetoothGatt gatt = Mockito.mock(BluetoothGatt.class);
                BluetoothGattCallback callback = invocationOnMock.getArgument(2);
                callback.onConnectionStateChange(gatt, 0, BluetoothGatt.STATE_DISCONNECTED);
                return gatt;
            }
        });

        // 1. Connect with gatt
        // 1.1 While connecting, gatt will throw disconnect error
        device.connectWithGatt();

        // 2. Immediatelly try to connect again
        device.connect();

        // We should not get any exceptions
    }
}