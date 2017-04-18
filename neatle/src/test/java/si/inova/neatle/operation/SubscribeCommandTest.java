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
import android.bluetooth.BluetoothGattDescriptor;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.N_MR1)
public class SubscribeCommandTest {

    private UUID serviceUUID = Neatle.createUUID(1);
    private UUID characteristicUUID = Neatle.createUUID(2);
    private UUID clientCharacteristicConfig = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

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
    @Mock
    private BluetoothGattDescriptor gattDescriptor;

    private SubscribeCommand subscribeCommand;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        subscribeCommand = new SubscribeCommand(
                SubscribeCommand.Type.SUBSCRIBE_NOTIFICATION,
                serviceUUID,
                characteristicUUID,
                commandObserver);
    }

    @Test
    public void testServiceNotFound() {
        when(gatt.getService(eq(serviceUUID))).thenReturn(null);
        subscribeCommand.execute(device, operationCommandObserver, gatt);
        verifyCommandFail(subscribeCommand, Command.SERVICE_NOT_FOUND);
    }

    @Test
    public void testCharacteristicNotFound() {
        when(gatt.getService(eq(serviceUUID))).thenReturn(gattService);

        subscribeCommand.execute(device, operationCommandObserver, gatt);
        verifyCommandFail(subscribeCommand, Command.CHARACTERISTIC_NOT_FOUND);
    }

    @Test
    public void testDescriptorNotFound() {
        when(gatt.getService(eq(serviceUUID))).thenReturn(gattService);
        when(gattService.getCharacteristic(characteristicUUID)).thenReturn(gattCharacteristic);
        when(gattCharacteristic.getDescriptor(eq(clientCharacteristicConfig))).thenReturn(null);

        subscribeCommand.execute(device, operationCommandObserver, gatt);
        verifyCommandFail(subscribeCommand, Command.DESCRIPTOR_NOT_FOUND);
    }

    @Test
    public void testSubscribeNotificationSuccess() {
        when(gattDescriptor.getValue()).thenReturn(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        checkCommandSuccess(SubscribeCommand.Type.SUBSCRIBE_NOTIFICATION);
    }

    @Test
    public void testSubscribeIndicationSuccess() {
        when(gattDescriptor.getValue()).thenReturn(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        checkCommandSuccess(SubscribeCommand.Type.SUBSCRIBE_INDICATION);
    }

    @Test
    public void testUnsubscribeSuccess() {
        when(gattDescriptor.getValue()).thenReturn(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        checkCommandSuccess(SubscribeCommand.Type.UNSUBSCRIBE);
    }

    @Test
    public void testCharacteristicNotificationFail() {
        when(gatt.getService(eq(serviceUUID))).thenReturn(gattService);
        when(gattService.getCharacteristic(characteristicUUID)).thenReturn(gattCharacteristic);
        when(gattCharacteristic.getDescriptor(eq(clientCharacteristicConfig))).thenReturn(gattDescriptor);
        when(gatt.setCharacteristicNotification(Mockito.eq(gattCharacteristic), Mockito.anyBoolean())).thenReturn(false);

        subscribeCommand.execute(device, operationCommandObserver, gatt);
        verifyCommandFail(subscribeCommand, BluetoothGatt.GATT_FAILURE);
    }

    @Test
    public void testUnsubscribeWithListeners() {
        when(device.getCharacteristicsChangedListenerCount(characteristicUUID)).thenReturn(1);

        subscribeCommand = new SubscribeCommand(
                SubscribeCommand.Type.UNSUBSCRIBE,
                serviceUUID,
                characteristicUUID,
                commandObserver);

        subscribeCommand.execute(device, operationCommandObserver, gatt);
        CommandResult result = CommandResult.createEmptySuccess(characteristicUUID);
        verify(commandObserver, times(1)).finished(eq(subscribeCommand), refEq(result, "timestamp"));
        verify(operationCommandObserver, times(1)).finished(eq(subscribeCommand), refEq(result, "timestamp"));
    }

    @Test
    public void testWriteDescriptorFail() {
        when(gatt.getService(eq(serviceUUID))).thenReturn(gattService);
        when(gattService.getCharacteristic(characteristicUUID)).thenReturn(gattCharacteristic);
        when(gattCharacteristic.getDescriptor(eq(clientCharacteristicConfig))).thenReturn(gattDescriptor);
        when(gatt.setCharacteristicNotification(Mockito.eq(gattCharacteristic), Mockito.anyBoolean())).thenReturn(true);
        when(gatt.writeDescriptor(gattDescriptor)).thenReturn(false);

        subscribeCommand.execute(device, operationCommandObserver, gatt);
        verifyCommandFail(subscribeCommand, BluetoothGatt.GATT_FAILURE);
    }

    @Test
    public void testWriteDescriptorSuccess() {
        setupDescriptorSuccess();

        subscribeCommand.execute(device, operationCommandObserver, gatt);
        verify(commandObserver, times(0)).finished(Mockito.any(Command.class), Mockito.any(CommandResult.class));
        verify(operationCommandObserver, times(0)).finished(Mockito.any(Command.class), Mockito.any(CommandResult.class));
    }

    @Test
    public void testOnDescriptorWriteSuccess() {
        setupDescriptorSuccess();

        subscribeCommand.execute(device, operationCommandObserver, gatt);
        subscribeCommand.onDescriptorWrite(gatt, gattDescriptor, BluetoothGatt.GATT_SUCCESS);

        CommandResult result = CommandResult.createEmptySuccess(characteristicUUID);
        verify(commandObserver, times(1)).finished(eq(subscribeCommand), refEq(result, "timestamp"));
        verify(operationCommandObserver, times(1)).finished(eq(subscribeCommand), refEq(result, "timestamp"));
    }

    @Test
    public void testOnDescriptorWriteFail() {
        setupDescriptorSuccess();

        subscribeCommand.execute(device, operationCommandObserver, gatt);
        subscribeCommand.onDescriptorWrite(gatt, gattDescriptor, BluetoothGatt.GATT_FAILURE);
        verifyCommandFail(subscribeCommand, BluetoothGatt.GATT_FAILURE);
    }

    @Test
    public void testOnError() {
        setupDescriptorSuccess();
        subscribeCommand.execute(device, operationCommandObserver, gatt);

        subscribeCommand.onError(BluetoothGatt.GATT_FAILURE);
        verifyCommandFail(subscribeCommand, BluetoothGatt.GATT_FAILURE);
    }

    @Test
    public void testOnStringBecauseWhyNot() {
        SubscribeCommand subNotif = new SubscribeCommand(
                SubscribeCommand.Type.SUBSCRIBE_NOTIFICATION,
                serviceUUID,
                characteristicUUID,
                commandObserver);

        SubscribeCommand subIndi = new SubscribeCommand(
                SubscribeCommand.Type.SUBSCRIBE_INDICATION,
                serviceUUID,
                characteristicUUID,
                commandObserver);

        SubscribeCommand unsub = new SubscribeCommand(
                SubscribeCommand.Type.UNSUBSCRIBE,
                serviceUUID,
                characteristicUUID,
                commandObserver);


        assertNotNull(subNotif.toString());
        assertNotNull(subIndi.toString());
        assertNotNull(unsub.toString());
    }

    public void checkCommandSuccess(@SubscribeCommand.Type int type) {
        subscribeCommand.execute(device, operationCommandObserver, gatt);
        when(gatt.getService(eq(serviceUUID))).thenReturn(gattService);
        when(gattService.getCharacteristic(characteristicUUID)).thenReturn(gattCharacteristic);
        when(gattCharacteristic.getDescriptor(eq(clientCharacteristicConfig))).thenReturn(gattDescriptor);

        subscribeCommand = new SubscribeCommand(
                type,
                serviceUUID,
                characteristicUUID,
                commandObserver);

        subscribeCommand.execute(device, operationCommandObserver, gatt);

        CommandResult result = CommandResult.createEmptySuccess(characteristicUUID);
        verify(commandObserver, times(1)).finished(eq(subscribeCommand), refEq(result, "timestamp"));
        verify(operationCommandObserver, times(1)).finished(eq(subscribeCommand), refEq(result, "timestamp"));
    }

    private void setupDescriptorSuccess() {
        when(gatt.getService(eq(serviceUUID))).thenReturn(gattService);
        when(gattService.getCharacteristic(characteristicUUID)).thenReturn(gattCharacteristic);
        when(gattCharacteristic.getDescriptor(eq(clientCharacteristicConfig))).thenReturn(gattDescriptor);
        when(gatt.setCharacteristicNotification(Mockito.eq(gattCharacteristic), Mockito.anyBoolean())).thenReturn(true);
        when(gatt.writeDescriptor(gattDescriptor)).thenReturn(true);
    }

    private void verifyCommandFail(SubscribeCommand subscribeCommand, int failCode) {
        CommandResult result = CommandResult.createErrorResult(characteristicUUID, failCode);
        verify(commandObserver, times(1)).finished(eq(subscribeCommand), refEq(result, "timestamp"));
        verify(operationCommandObserver, times(1)).finished(eq(subscribeCommand), refEq(result, "timestamp"));
    }
}
