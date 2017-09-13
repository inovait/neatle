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

import si.inova.neatle.monitor.Connection;

public abstract class Command {

    public static final int SERVICE_NOT_FOUND = BluetoothGatt.GATT_FAILURE + 10;
    public static final int CHARACTERISTIC_NOT_FOUND = SERVICE_NOT_FOUND + 1;
    public static final int DESCRIPTOR_NOT_FOUND = CHARACTERISTIC_NOT_FOUND + 1;

    private final Object lock = new Object();

    private final CommandObserver observer;
    private CommandObserver operationCommandObserver;

    protected Command(CommandObserver observer) {
        this.observer = observer;
    }

    @CallSuper
    protected final void execute(Connection connection, CommandObserver operationCommandObserver, BluetoothGatt gatt) {
        synchronized (lock) {
            this.operationCommandObserver = operationCommandObserver;
        }
        start(connection, gatt);
    }

    /**
     * Called when the command should start executing. When a command
     * is done with processing, it must call finish to release
     * control of the "gatt", so other commands that need it can
     * execute.
     *
     * When the method is called, the connection will be already established and all services
     * will be already discovered.
     *
     * @param connection the connection on which the command will operate
     * @param gatt bluettoth gat
     */
    abstract protected void start(Connection connection, BluetoothGatt gatt);

    @CallSuper
    protected final void finish(CommandResult result) {
        CommandObserver oco;
        synchronized (lock) {
            oco = operationCommandObserver;
        }

        onFinished(result);

        oco.finished(this, result);
        if (observer != null) {
            observer.finished(this, result);
        }
    }

    /**
     * Called when the command finished, either successfully or not but.
     *
     * @param result the results of the command
     */
    protected void onFinished(CommandResult result) {
        //does nothing on it's own
    }

    protected abstract void onError(int error);

    protected void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (status != BluetoothGatt.GATT_SUCCESS || newState != BluetoothGatt.STATE_CONNECTED) {
            onError(BluetoothGatt.GATT_FAILURE);
        }
    }

    /**
     * @see android.bluetooth.BluetoothGattCallback#onCharacteristicRead(BluetoothGatt, BluetoothGattCharacteristic, int)
     */
    protected void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        //do nothing by default
    }

    /**
     * @see android.bluetooth.BluetoothGattCallback#onCharacteristicWrite(BluetoothGatt, BluetoothGattCharacteristic, int)
     */
    protected void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        //do nothing by default
    }

    /**
     * @see android.bluetooth.BluetoothGattCallback#onDescriptorRead(BluetoothGatt, BluetoothGattDescriptor, int)
     */
    protected void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        //do nothing by default
    }

    /**
     * @see android.bluetooth.BluetoothGattCallback#onDescriptorWrite(BluetoothGatt, BluetoothGattDescriptor, int)
     */
    protected void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        //do nothing by default
    }

    /**
     * @see android.bluetooth.BluetoothGattCallback#onCharacteristicChanged(BluetoothGatt, BluetoothGattCharacteristic)
     */
    protected void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        //do nothing by default
    }

    protected void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
    }

    protected void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {

    }

    protected void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {

    }
}
