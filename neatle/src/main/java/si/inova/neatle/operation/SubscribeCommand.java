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
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.UUID;

import si.inova.neatle.monitor.Connection;
import si.inova.neatle.util.NeatleLogger;

class SubscribeCommand extends Command {

    private static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    @Type
    private final int type;

    SubscribeCommand(@Type int type, UUID serviceUUID, UUID characteristicUUID, CommandObserver observer) {
        super(serviceUUID, characteristicUUID, observer);
        this.type = type;
    }

    @Override
    protected void execute(Connection connection, CommandObserver commandObserver, BluetoothGatt gatt) {
        super.execute(connection, commandObserver, gatt);

        if (type == Type.UNSUBSCRIBE && connection.getCharacteristicsChangedListenerCount(characteristicUUID) > 0) {
            NeatleLogger.d("Won't unsubscribe on " + characteristicUUID + " since it has registered listeners");
            finish(CommandResult.createEmptySuccess(characteristicUUID));
            return;
        }

        BluetoothGattService service = gatt.getService(serviceUUID);
        if (service == null) {
            finish(CommandResult.createErrorResult(characteristicUUID, SERVICE_NOT_FOUND));
            return;
        }

        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
        if (characteristic == null) {
            finish(CommandResult.createErrorResult(characteristicUUID, CHARACTERISTIC_NOT_FOUND));
            return;
        }

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
        if (descriptor == null) {
            finish(CommandResult.createErrorResult(characteristicUUID, DESCRIPTOR_NOT_FOUND));
            return;
        }

        boolean turnOn;
        byte[] valueToWrite;
        switch (type) {
            case Type.SUBSCRIBE_INDICATION:
                NeatleLogger.d("Subscribing to indications on  " + characteristicUUID);
                valueToWrite = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
                turnOn = true;
                break;
            case Type.SUBSCRIBE_NOTIFICATION:
                NeatleLogger.d("Subscribing to notifications on  " + characteristicUUID);
                valueToWrite = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                turnOn = true;
                break;
            case Type.UNSUBSCRIBE:
                NeatleLogger.d("Unsubscribing from notifications/indications on  " + characteristicUUID);
                valueToWrite = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                turnOn = false;
                break;
            default:
                throw new IllegalStateException();
        }

        byte[] descriptorValue = descriptor.getValue();
        if (Arrays.equals(descriptorValue, valueToWrite)) {
            NeatleLogger.d("No subscription changes needed - is at  " + valueToWrite[0] + " on " + characteristicUUID);
            finish(CommandResult.createEmptySuccess(characteristicUUID));
            return;
        }

        if (!gatt.setCharacteristicNotification(characteristic, turnOn)) {
            NeatleLogger.e("Failed to change characteristics notification flag on " + characteristicUUID);
            finish(CommandResult.createErrorResult(characteristicUUID, BluetoothGatt.GATT_FAILURE));
            return;
        }

        descriptor.setValue(valueToWrite);
        NeatleLogger.d("Writing descriptor on " + characteristicUUID);
        if (!gatt.writeDescriptor(descriptor)) {
            NeatleLogger.e("Failed to write descriptor on " + characteristicUUID);
            finish(CommandResult.createErrorResult(characteristicUUID, BluetoothGatt.GATT_FAILURE));
        }
    }

    @Override
    protected void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            finish(CommandResult.createErrorResult(characteristicUUID, status));
        } else {
            finish(CommandResult.createEmptySuccess(characteristicUUID));
        }
    }

    @Override
    protected void onError(int error) {
        finish(CommandResult.createErrorResult(characteristicUUID, error));
    }

    @Override
    public String toString() {
        if (type == Type.UNSUBSCRIBE) {
            return "UnsubscribeCommand[" + characteristicUUID + "] on [" + serviceUUID + "]";
        } else if (type == Type.SUBSCRIBE_INDICATION) {
            return "SubscribeCommand[" + characteristicUUID + "] on [" + serviceUUID + "]";
        } else if (type == Type.SUBSCRIBE_NOTIFICATION) {
            return "SubscribeNotificationCommand[" + characteristicUUID + "] on [" + serviceUUID + "]";
        } else {
            return super.toString();
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Type.SUBSCRIBE_NOTIFICATION, Type.SUBSCRIBE_INDICATION, Type.UNSUBSCRIBE})
    @interface Type {
        int SUBSCRIBE_NOTIFICATION = 0;
        int SUBSCRIBE_INDICATION = 1;
        int UNSUBSCRIBE = 2;
    }
}
