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

package si.inova.neatle;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import java.util.Arrays;
import java.util.UUID;

class SubscribeCommand extends Command {

    private static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final int SUBSCRIBE_NOTIFICATION = 1;
    public static final int SUBSCRIBE_INDICATION = 2;
    public static final int UNSUBSCRIBE = 3;

    private final UUID serviceUUID;
    private final UUID characteristicsUUID;
    private final int op;
    private Connection connection;

    public SubscribeCommand(int op, UUID serviceUUID, UUID characteristicsUUID, OperationObserver observer) {
        super(observer);
        if (op != SUBSCRIBE_INDICATION && op != SUBSCRIBE_NOTIFICATION && op != UNSUBSCRIBE) {
            throw new IllegalArgumentException();
        }

        this.serviceUUID = serviceUUID;
        this.characteristicsUUID = characteristicsUUID;
        this.op = op;
    }

    @Override
    protected void execute(Connection connection, BluetoothGatt gatt, OperationResults results) {
        this.connection = connection;
        if (op == UNSUBSCRIBE && connection.getCharacteristicsChangedListenerCount(characteristicsUUID) > 0) {
            NeatleLogger.d("Won't unsubscribe on " + characteristicsUUID + " since it has registered listeners");
            finish(CommandResult.createEmptySuccess(characteristicsUUID));
            return;
        }

        BluetoothGattService service = gatt.getService(serviceUUID);
        if (service == null) {
            finish(CommandResult.createErrorResult(characteristicsUUID, SERVICE_NOT_FOUND));
            return;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicsUUID);
        if (characteristic == null) {
            finish(CommandResult.createErrorResult(characteristicsUUID, CHARACTERISTIC_NOT_FOUND));
            return;
        }

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
        if (descriptor == null) {
            finish(CommandResult.createErrorResult(characteristicsUUID, DESCRIPTOR_NOT_FOUND));
            return;
        }

        boolean turnOn;
        byte[] valueToWrite;
        switch (op) {
            case SUBSCRIBE_INDICATION:
                NeatleLogger.d("Subscribing to indications on  " + characteristicsUUID);
                valueToWrite = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
                turnOn = true;
                break;
            case SUBSCRIBE_NOTIFICATION:
                NeatleLogger.d("Subscribing to notifications on  " + characteristicsUUID);
                valueToWrite = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                turnOn = true;
                break;
            case UNSUBSCRIBE:
                NeatleLogger.d("Unsubscribing from notifications/indications on  " + characteristicsUUID);
                valueToWrite = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                turnOn = false;
                break;
            default:
                throw new IllegalStateException();
        }

        byte[] descriptorValue = descriptor.getValue();
        if (Arrays.equals(descriptorValue, valueToWrite)) {
            NeatleLogger.d("No subscription changes needed - is at  " + valueToWrite[0] + " on " + characteristicsUUID);
            finish(CommandResult.createEmptySuccess(characteristicsUUID));
            return;
        }

        if (!gatt.setCharacteristicNotification(characteristic, turnOn)) {
            NeatleLogger.e("Failed to change characteristics notification flag on " + characteristicsUUID);
            finish(CommandResult.createErrorResult(characteristicsUUID, BluetoothGatt.GATT_FAILURE));
            return;
        }


        descriptor.setValue(valueToWrite);
        NeatleLogger.e("Writing descriptor on " + characteristicsUUID);
        if (!gatt.writeDescriptor(descriptor)) {
            NeatleLogger.e("Failed to write descriptor on " + characteristicsUUID);
            finish(CommandResult.createErrorResult(characteristicsUUID, BluetoothGatt.GATT_FAILURE));
            return;
        }
    }

    @Override
    protected void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            finish(CommandResult.createErrorResult(characteristicsUUID, status));
            return;
        }
        finish(CommandResult.createEmptySuccess(characteristicsUUID));
    }

    @Override
    protected void onError(int error) {
        finish(CommandResult.createErrorResult(characteristicsUUID, error));
    }

    @Override
    public String toString() {
        if (op == UNSUBSCRIBE) {
            return "UnsubscribeCommand[" + characteristicsUUID + "] on [" + serviceUUID + "]";
        } else if (op == SUBSCRIBE_INDICATION) {
            return "SubscribeIndication[" + characteristicsUUID + "] on [" + serviceUUID + "]";
        }
        return "SubscribeNotificationCommand[" + characteristicsUUID + "] on [" + serviceUUID + "]";

    }
}
