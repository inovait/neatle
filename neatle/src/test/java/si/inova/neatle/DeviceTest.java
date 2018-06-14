package si.inova.neatle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
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

        // This test checks for the exception. If we got so far, test passed
        Assert.assertTrue(true);
    }

    @Test
    public void connectAfterConnectionFailed() throws Exception {
        final ArgumentCaptor<BluetoothGattCallback> deviceCallback =
                ArgumentCaptor.forClass(BluetoothGattCallback.class);

        BluetoothGattCallback externalCallback = Mockito.mock(BluetoothGattCallback.class);
        Mockito.when(btDevice.connectGatt(
                Mockito.<Context>any(),
                Mockito.anyBoolean(),
                deviceCallback.capture())
        ).thenReturn(Mockito.mock(BluetoothGatt.class));

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) {
                try {
                    //noinspection deprecation
                    Thread.currentThread().stop();
                } catch (Exception ignored) {
                }

                return null;
            }
        }).when(externalCallback).onConnectionStateChange(
                Mockito.<BluetoothGatt>any(),
                Mockito.eq(8000),
                Mockito.eq(BluetoothGatt.STATE_DISCONNECTED)
        );

        // 1. Set external callback
        device.execute(externalCallback);

        // 2. Connect on background thread (simulate multithreaded BT stack on some devices)
        // 2.1 This thread will get stuck in the middle of processing callbacks
        // (to simulate later connect() being executed before connectionFailed() finishes)
        Thread thread = new Thread() {
            @Override
            public void run() {
                device.connectWithGatt();
                deviceCallback.getValue().onConnectionStateChange(
                        Mockito.mock(BluetoothGatt.class),
                        8000,
                        BluetoothGatt.STATE_DISCONNECTED
                );
            }
        };
        thread.setDaemon(true);
        thread.start();

        Thread.sleep(50);

        // 3. Immediatelly try to connect again
        device.connect();

        // This test checks for the exception. If we got so far, test passed
        Assert.assertTrue(true);
    }
}