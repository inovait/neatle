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
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import si.inova.neatle.BuildConfig;
import si.inova.neatle.Device;
import si.inova.neatle.Neatle;
import si.inova.neatle.source.AsyncInputSource;
import si.inova.neatle.source.InputSource;
import si.inova.neatle.source.StringInputSource;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.N_MR1)
public class WriteCommandTest {

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

    private WriteCommand writeCommand;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        InputSource inputSource = new StringInputSource("lorem ipsum dolor sit amet");
        writeCommand = new WriteCommand(
                serviceUUID,
                characteristicUUID,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
                inputSource,
                commandObserver);
    }

    @Test
    public void testServiceNotFound() {
        when(gatt.getService(eq(serviceUUID))).thenReturn(null);

        writeCommand.execute(device, operationCommandObserver, gatt);
        verifyCommandFail();
    }

    @Test
    public void testCharacteristicNotFound() {
        when(gatt.getService(eq(serviceUUID))).thenReturn(gattService);

        writeCommand.execute(device, operationCommandObserver, gatt);
        verifyCommandFail();
    }

    @Test
    public void testWriteFirstChunkFailed() throws IOException {
        InputSource inputSource = Mockito.mock(InputSource.class);
        doThrow(IOException.class).when(inputSource).open();

        when(gatt.getService(eq(serviceUUID))).thenReturn(gattService);
        when(gattService.getCharacteristic(characteristicUUID)).thenReturn(gattCharacteristic);

        WriteCommand writeCommand = new WriteCommand(
                serviceUUID,
                characteristicUUID,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
                inputSource,
                commandObserver);

        writeCommand.execute(device, operationCommandObserver, gatt);
        CommandResult result = CommandResult.createErrorResult(characteristicUUID, BluetoothGatt.GATT_FAILURE);
        verify(commandObserver, times(1)).finished(eq(writeCommand), refEq(result, "timestamp"));
        verify(operationCommandObserver, times(1)).finished(eq(writeCommand), refEq(result, "timestamp"));
        Mockito.reset(commandObserver, operationCommandObserver, inputSource);

        when(inputSource.nextChunk()).thenThrow(IOException.class);
        writeCommand.execute(device, operationCommandObserver, gatt);
        verify(commandObserver, times(1)).finished(eq(writeCommand), refEq(result, "timestamp"));
        verify(operationCommandObserver, times(1)).finished(eq(writeCommand), refEq(result, "timestamp"));
    }

    @Test
    public void testWriteSuccessEmpty() throws IOException {
        when(gatt.getService(eq(serviceUUID))).thenReturn(gattService);
        when(gattService.getCharacteristic(characteristicUUID)).thenReturn(gattCharacteristic);
        when(gatt.readCharacteristic(eq(gattCharacteristic))).thenReturn(true);
        InputSource inputSource = Mockito.mock(InputSource.class);
        when(inputSource.nextChunk()).thenReturn(null);

        writeCommand = new WriteCommand(
                serviceUUID,
                characteristicUUID,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
                inputSource,
                commandObserver);

        writeCommand.execute(device, operationCommandObserver, gatt);
        CommandResult result = CommandResult.createEmptySuccess(characteristicUUID);
        verify(commandObserver, times(1)).finished(eq(writeCommand), refEq(result, "timestamp"));
        verify(operationCommandObserver, times(1)).finished(eq(writeCommand), refEq(result, "timestamp"));
    }

    @Test
    public void testWriteSuccess() throws IOException {
        when(gatt.getService(eq(serviceUUID))).thenReturn(gattService);
        when(gattService.getCharacteristic(characteristicUUID)).thenReturn(gattCharacteristic);
        when(gatt.writeCharacteristic(eq(gattCharacteristic))).thenReturn(true);
        InputSource inputSource = Mockito.mock(InputSource.class);
        when(inputSource.nextChunk()).thenReturn(new byte[]{21, 22});

        writeCommand = new WriteCommand(
                serviceUUID,
                characteristicUUID,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
                inputSource,
                commandObserver);

        writeCommand.execute(device, operationCommandObserver, gatt);
        verify(commandObserver, times(0)).finished(any(Command.class), any(CommandResult.class));
        verify(operationCommandObserver, times(0)).finished(any(Command.class), any(CommandResult.class));
        reset(commandObserver, operationCommandObserver);

        when(gatt.writeCharacteristic(eq(gattCharacteristic))).thenReturn(false);
        writeCommand.execute(device, operationCommandObserver, gatt);
        verifyCommandFail();
    }

    @Test
    public void testOnCharacteristicWriteFail() throws IOException {
        when(gatt.getService(eq(serviceUUID))).thenReturn(gattService);
        when(gattService.getCharacteristic(characteristicUUID)).thenReturn(gattCharacteristic);
        when(gatt.writeCharacteristic(eq(gattCharacteristic))).thenReturn(true);
        InputSource inputSource = Mockito.mock(InputSource.class);
        when(inputSource.nextChunk()).thenReturn(new byte[]{21, 22});

        writeCommand = new WriteCommand(
                serviceUUID,
                characteristicUUID,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
                inputSource,
                commandObserver);

        writeCommand.execute(device, operationCommandObserver, gatt);

        writeCommand.onCharacteristicWrite(gatt, gattCharacteristic, BluetoothGatt.GATT_FAILURE);
        verifyCommandFail();
    }

    @Test
    public void testOnCharacteristicWriteFai2() throws IOException {
        when(gatt.getService(eq(serviceUUID))).thenReturn(gattService);
        when(gattService.getCharacteristic(characteristicUUID)).thenReturn(gattCharacteristic);
        when(gatt.writeCharacteristic(eq(gattCharacteristic))).thenReturn(true);
        InputSource inputSource = Mockito.mock(InputSource.class);
        when(inputSource.nextChunk()).thenReturn(new byte[]{21, 22});

        writeCommand = new WriteCommand(
                serviceUUID,
                characteristicUUID,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
                inputSource,
                commandObserver);

        writeCommand.execute(device, operationCommandObserver, gatt);

        doThrow(IOException.class).when(inputSource).nextChunk();
        writeCommand.onCharacteristicWrite(gatt, gattCharacteristic, BluetoothGatt.GATT_SUCCESS);
        verifyCommandFail();
    }

    @Test
    public void testOnCharacteristicWriteFinished() throws IOException {
        when(gatt.getService(eq(serviceUUID))).thenReturn(gattService);
        when(gattService.getCharacteristic(characteristicUUID)).thenReturn(gattCharacteristic);
        when(gatt.writeCharacteristic(eq(gattCharacteristic))).thenReturn(true);
        InputSource inputSource = Mockito.mock(InputSource.class);
        when(inputSource.nextChunk()).thenReturn(new byte[]{12, 21});

        writeCommand = new WriteCommand(
                serviceUUID,
                characteristicUUID,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
                inputSource,
                commandObserver);

        writeCommand.execute(device, operationCommandObserver, gatt);

        when(inputSource.nextChunk()).thenReturn(null);
        writeCommand.onCharacteristicWrite(gatt, gattCharacteristic, BluetoothGatt.GATT_SUCCESS);
        CommandResult result = CommandResult.createEmptySuccess(characteristicUUID);
        verify(commandObserver, times(1)).finished(eq(writeCommand), refEq(result, "timestamp"));
        verify(operationCommandObserver, times(1)).finished(eq(writeCommand), refEq(result, "timestamp"));
        verify(inputSource).close();
    }

    @Test(timeout = 1000)
    public void testOnCharacteristicWriteFinishedAsyncSource() throws IOException {
        when(gatt.getService(eq(serviceUUID))).thenReturn(gattService);
        when(gattService.getCharacteristic(characteristicUUID)).thenReturn(gattCharacteristic);
        when(gatt.writeCharacteristic(eq(gattCharacteristic))).thenReturn(true);
        AsyncInputSource inputSource = Mockito.mock(AsyncInputSource.class);
        when(inputSource.nextChunk()).thenReturn(new byte[]{12, 21});

        writeCommand = new WriteCommand(
                serviceUUID,
                characteristicUUID,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
                inputSource,
                commandObserver);

        writeCommand.execute(device, operationCommandObserver, gatt);

        when(inputSource.nextChunk()).thenReturn(null);
        writeCommand.onCharacteristicWrite(gatt, gattCharacteristic, BluetoothGatt.GATT_SUCCESS);

        while (writeCommand.readerThread.isAlive()) {
            Robolectric.getForegroundThreadScheduler().advanceBy(0, TimeUnit.MILLISECONDS);
            Thread.yield();
        }

        Robolectric.getForegroundThreadScheduler().advanceBy(0, TimeUnit.MILLISECONDS);

        CommandResult result = CommandResult.createEmptySuccess(characteristicUUID);
        verify(commandObserver, only()).finished(eq(writeCommand), refEq(result, "timestamp"));
        verify(operationCommandObserver, only()).finished(eq(writeCommand), refEq(result, "timestamp"));
        verify(inputSource).close();
    }

    @Test
    public void testInputSourceClosesOnError() throws IOException {
        when(gatt.getService(eq(serviceUUID))).thenReturn(gattService);
        when(gattService.getCharacteristic(characteristicUUID)).thenReturn(gattCharacteristic);
        when(gatt.writeCharacteristic(eq(gattCharacteristic))).thenReturn(true);
        InputSource inputSource = Mockito.mock(InputSource.class);
        when(inputSource.nextChunk()).thenThrow(new IOException());

        writeCommand = new WriteCommand(
                serviceUUID,
                characteristicUUID,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
                inputSource,
                commandObserver);

        writeCommand.execute(device, operationCommandObserver, gatt);
        writeCommand.onCharacteristicWrite(gatt, gattCharacteristic, BluetoothGatt.GATT_SUCCESS);

        verify(inputSource).close();
    }

    @Test(timeout = 1000)
    public void testAsyncInputSourceClosesOnError() throws IOException {
        when(gatt.getService(eq(serviceUUID))).thenReturn(gattService);
        when(gattService.getCharacteristic(characteristicUUID)).thenReturn(gattCharacteristic);
        when(gatt.writeCharacteristic(eq(gattCharacteristic))).thenReturn(true);
        AsyncInputSource inputSource = Mockito.mock(AsyncInputSource.class);
        when(inputSource.nextChunk()).thenThrow(new IOException());

        writeCommand = new WriteCommand(
                serviceUUID,
                characteristicUUID,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
                inputSource,
                commandObserver);

        writeCommand.execute(device, operationCommandObserver, gatt);
        writeCommand.onCharacteristicWrite(gatt, gattCharacteristic, BluetoothGatt.GATT_SUCCESS);

        while (writeCommand.readerThread.isAlive()) {
            Robolectric.getForegroundThreadScheduler().advanceBy(0, TimeUnit.MILLISECONDS);
            Thread.yield();
        }
        Robolectric.getForegroundThreadScheduler().advanceBy(0, TimeUnit.MILLISECONDS);

        verify(inputSource).close();
    }

    @Test
    public void testOnCharacteristicWriteNextChunk() throws IOException {
        when(gatt.getService(eq(serviceUUID))).thenReturn(gattService);
        when(gattService.getCharacteristic(characteristicUUID)).thenReturn(gattCharacteristic);
        when(gatt.writeCharacteristic(eq(gattCharacteristic))).thenReturn(true);
        InputSource inputSource = Mockito.mock(InputSource.class);
        when(inputSource.nextChunk()).thenReturn(new byte[]{12, 21});

        writeCommand = new WriteCommand(
                serviceUUID,
                characteristicUUID,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
                inputSource,
                commandObserver);

        writeCommand.execute(device, operationCommandObserver, gatt);

        writeCommand.onCharacteristicWrite(gatt, gattCharacteristic, BluetoothGatt.GATT_SUCCESS);
        verify(commandObserver, times(0)).finished(any(Command.class), any(CommandResult.class));
        verify(operationCommandObserver, times(0)).finished(any(Command.class), any(CommandResult.class));
    }

    @Test
    public void testOnError() {
        writeCommand.execute(device, operationCommandObserver, gatt);
        reset(operationCommandObserver, commandObserver);

        writeCommand.onError(BluetoothGatt.GATT_FAILURE);
        verifyCommandFail();
    }

    @Test
    public void testToStringBecauseWhyNot() {
        assertNotNull(writeCommand.toString());
    }

    private void verifyCommandFail() {
        CommandResult result = CommandResult.createErrorResult(characteristicUUID, BluetoothGatt.GATT_FAILURE);
        verify(commandObserver, times(1)).finished(eq(writeCommand), refEq(result, "timestamp"));
        verify(operationCommandObserver, times(1)).finished(eq(writeCommand), refEq(result, "timestamp"));
    }
}
