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

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Build;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.UUID;

import si.inova.neatle.BuildConfig;
import si.inova.neatle.Device;
import si.inova.neatle.Neatle;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.N_MR1)
public class ReadCommandTest {

    private UUID serviceUUID = Neatle.createUUID(1);
    private UUID characteristicUUID = Neatle.createUUID(2);

    @Mock
    private CommandObserver commandObserver;
    @Mock
    private CommandObserver operationCommandObserver;
    @Mock
    private BluetoothGatt gatt;
    @Mock
    private Device device;
    @Mock
    private BluetoothGattService gattService;
    @Mock
    private BluetoothGattCharacteristic gattCharacteristic;

    private ReadCommand readCommand;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        readCommand = new ReadCommand(
                serviceUUID,
                characteristicUUID,
                commandObserver);
    }

    @Test
    public void testServiceNotFound() {
        when(gatt.getService(eq(serviceUUID))).thenReturn(null);

        readCommand.execute(device, operationCommandObserver, gatt);
        verifyCommandFail();
    }

    @Test
    public void testCharacteristicNotFound() {
        when(gatt.getService(eq(serviceUUID))).thenReturn(gattService);

        readCommand.execute(device, operationCommandObserver, gatt);
        verifyCommandFail();
    }

    @Test
    public void testReadFailed() {
        when(gatt.getService(eq(serviceUUID))).thenReturn(gattService);
        when(gattService.getCharacteristic(characteristicUUID)).thenReturn(gattCharacteristic);
        when(gatt.readCharacteristic(eq(gattCharacteristic))).thenReturn(false);

        readCommand.execute(device, operationCommandObserver, gatt);
        verifyCommandFail();
    }

    @Test
    public void testReadSuccess() {
        when(gatt.getService(eq(serviceUUID))).thenReturn(gattService);
        when(gattService.getCharacteristic(characteristicUUID)).thenReturn(gattCharacteristic);
        when(gatt.readCharacteristic(eq(gattCharacteristic))).thenReturn(true);

        readCommand.execute(device, operationCommandObserver, gatt);
        verify(commandObserver, times(0)).finished(any(Command.class), any(CommandResult.class));
        verify(operationCommandObserver, times(0)).finished(any(Command.class), any(CommandResult.class));
    }

    @Test
    public void testOnCharacteristicRead() {
        BluetoothGattCharacteristic otherCharacteristic = Mockito.mock(BluetoothGattCharacteristic.class);
        when(otherCharacteristic.getUuid()).thenReturn(Neatle.createUUID(123));

        when(gatt.getService(eq(serviceUUID))).thenReturn(gattService);
        when(gattService.getCharacteristic(characteristicUUID)).thenReturn(gattCharacteristic);
        when(gatt.readCharacteristic(eq(gattCharacteristic))).thenReturn(true);
        when(gattCharacteristic.getValue()).thenReturn(new byte[]{21, 22});
        when(gattCharacteristic.getUuid()).thenReturn(characteristicUUID);

        readCommand.execute(device, operationCommandObserver, gatt);

        // Unknown characteristic
        readCommand.onCharacteristicRead(gatt, otherCharacteristic, BluetoothGatt.GATT_SUCCESS);
        verify(commandObserver, times(0)).finished(any(Command.class), any(CommandResult.class));
        verify(operationCommandObserver, times(0)).finished(any(Command.class), any(CommandResult.class));
        Mockito.reset(commandObserver, operationCommandObserver);

        // Known characteristic
        readCommand.onCharacteristicRead(gatt, gattCharacteristic, BluetoothGatt.GATT_SUCCESS);
        CommandResult result = CommandResult.createCharacteristicRead(gattCharacteristic, BluetoothGatt.GATT_SUCCESS);
        verify(commandObserver, times(1)).finished(eq(readCommand), refEq(result, "timestamp"));
        verify(operationCommandObserver, times(1)).finished(eq(readCommand), refEq(result, "timestamp"));
    }

    @Test
    public void testOnError() {
        when(gatt.getService(eq(serviceUUID))).thenReturn(gattService);
        when(gattService.getCharacteristic(characteristicUUID)).thenReturn(gattCharacteristic);
        when(gatt.readCharacteristic(eq(gattCharacteristic))).thenReturn(true);

        readCommand.execute(device, operationCommandObserver, gatt);

        readCommand.onError(BluetoothGatt.GATT_FAILURE);
        verifyCommandFail();
    }

    @Test
    public void testToStringBecauseWhyNot() {
        ReadCommand readCommand = new ReadCommand(
                serviceUUID,
                characteristicUUID,
                commandObserver);

        assertNotNull(readCommand.toString());
    }

    private void verifyCommandFail() {
        CommandResult result = CommandResult.createErrorResult(characteristicUUID, BluetoothGatt.GATT_FAILURE);
        verify(commandObserver, times(1)).finished(eq(readCommand), refEq(result, "timestamp"));
        verify(operationCommandObserver, times(1)).finished(eq(readCommand), refEq(result, "timestamp"));
    }
}
