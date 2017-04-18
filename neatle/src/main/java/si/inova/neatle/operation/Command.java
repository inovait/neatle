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
import android.support.annotation.CallSuper;
import android.support.annotation.RestrictTo;

import java.util.UUID;

import si.inova.neatle.monitor.Connection;

abstract class Command {

    static final int SERVICE_NOT_FOUND = BluetoothGatt.GATT_FAILURE + 10;
    static final int CHARACTERISTIC_NOT_FOUND = SERVICE_NOT_FOUND + 1;
    static final int DESCRIPTOR_NOT_FOUND = CHARACTERISTIC_NOT_FOUND + 1;

    private final Object lock = new Object();

    protected final UUID serviceUUID;
    protected final UUID characteristicUUID;

    private final CommandObserver observer;
    private CommandObserver operationCommandObserver;

    Command(UUID serviceUUID, UUID characteristicUUID, CommandObserver observer) {
        this.serviceUUID = serviceUUID;
        this.characteristicUUID = characteristicUUID;
        this.observer = observer;
    }

    @CallSuper
    protected void execute(Connection connection, CommandObserver operationCommandObserver, BluetoothGatt gatt) {
        synchronized (lock) {
            this.operationCommandObserver = operationCommandObserver;
        }
    }

    protected abstract void onError(int error);

    @CallSuper
    protected void finish(CommandResult result) {
        CommandObserver oco;
        synchronized (lock) {
            oco = operationCommandObserver;
        }

        oco.finished(this, result);
        if (observer != null) {
            observer.finished(this, result);
        }
    }

    protected void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (status != BluetoothGatt.GATT_SUCCESS || newState != BluetoothGatt.STATE_CONNECTED) {
            onError(BluetoothGatt.GATT_FAILURE);
        }
    }

    protected void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
    }

    protected void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
    }

    protected void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
    }

    protected void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    UUID getServiceUUID() {
        return serviceUUID;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    UUID getCharacteristicUUID() {
        return characteristicUUID;
    }
}
