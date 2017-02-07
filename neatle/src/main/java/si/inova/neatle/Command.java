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

/**
 * Created by tomazs on 9/20/2016.
 */

abstract class Command {
    static final int SERVICE_NOT_FOUND = BluetoothGatt.GATT_FAILURE + 10;
    static final int CHARACTERISTIC_NOT_FOUND = SERVICE_NOT_FOUND + 1;
    static final int DESCRIPTOR_NOT_FOUND = CHARACTERISTIC_NOT_FOUND + 1;

    private final OperationObserver operationObserver;
    private Object lock = new Object();

    private OperationResults results;
    private CommandObserver observer;

    public Command(OperationObserver operationObserver) {
        this.operationObserver = operationObserver;
    }

    public OperationObserver getOperationObserver() {
        return operationObserver;
    }

    protected final void execute(Connection connection, CommandObserver observer, BluetoothGatt gatt, OperationResults results) {
        synchronized (lock) {
            this.observer = observer;
            this.results = results;
        }
        execute(connection, gatt, results);
    }


    protected abstract void execute(Connection connection, BluetoothGatt gatt, OperationResults results);

    protected abstract void onError(int error);

    protected void finish(CommandResult result) {
        CommandObserver oc;
        synchronized (lock) {
            oc = observer;
        }
        oc.finished(this, result);

    }


    protected void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (status != BluetoothGatt.GATT_SUCCESS || newState != BluetoothGatt.STATE_CONNECTED) {
            onError(BluetoothGatt.GATT_FAILURE);
        }
    }

    protected void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
    }

    protected void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
    }

    protected void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
    }

    protected void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
    }

    protected void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
    }

    protected void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
    }

    protected void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
    }

    protected void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
    }


}
