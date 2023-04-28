/*
 * MIT License
 *
 * Copyright (c) 2017 Inova IT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package si.inova.neatle.operation;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import si.inova.neatle.BuildConfig;
import si.inova.neatle.Device;
import si.inova.neatle.Neatle;
import si.inova.neatle.util.DeviceManager;

import java.util.UUID;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.N_MR1)
public class CharacteristicSubscriptionTest {

    private static final UUID SERVICE_UUID = Neatle.createUUID(1);
    private static final UUID CHARACTERISTIC_UUID = Neatle.createUUID(2);
    private static final String MAC = "00:11:22:33:44:55";

    @Mock
    private BluetoothDevice bluetoothDevice;
    @Mock
    private Context context;
    @Mock
    private BluetoothManager bluetoothManager;
    @Mock
    private Device device;
    @Mock
    private CharacteristicsChangedListener changedListener;

    private CharacteristicSubscriptionImpl subscription;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(bluetoothDevice.getAddress()).thenReturn(MAC);
        DeviceManager.getInstance(RuntimeEnvironment.application).putDevice(device, MAC);

        subscription = new CharacteristicSubscriptionImpl(
                RuntimeEnvironment.application, bluetoothDevice, SERVICE_UUID, CHARACTERISTIC_UUID);
        subscription.setOnCharacteristicsChangedListener(changedListener);
    }

    @After
    public void tearDown() {
        DeviceManager.getInstance(RuntimeEnvironment.application).clearDevices();
    }

    @Test
    public void testStartStop() {
        subscription.start();
        assertTrue(subscription.isStarted());
        subscription.start();
        assertTrue(subscription.isStarted());
        subscription.stop();
        assertFalse(subscription.isStarted());
        subscription.stop();
        assertFalse(subscription.isStarted());

        when(device.isConnected()).thenReturn(true);
        subscription.start();
        assertTrue(subscription.isStarted());
        subscription.stop();
        assertFalse(subscription.isStarted());
    }

    @Test
    public void testStopWhileStillStarting() {
        final BluetoothGatt bluetoothGatt = Mockito.mock(BluetoothGatt.class, Answers.RETURNS_DEEP_STUBS);

        when(device.isConnected()).thenReturn(true);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) {
                BluetoothGattCallback callback = invocationOnMock.getArgument(0);
                callback.onServicesDiscovered(bluetoothGatt, BluetoothGatt.GATT_SUCCESS);

                return null;
            }
        }).when(device).execute(Mockito.<BluetoothGattCallback>any());

        subscription.start();

        subscription.stop();
        subscription.start();

        assertTrue(subscription.isStarted());
    }
}
