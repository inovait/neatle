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

import java.util.UUID;

import si.inova.neatle.monitor.Connection;
import si.inova.neatle.util.NeatleLogger;

class ReadCommand extends SingleCharacteristicsCommand {

    ReadCommand(UUID serviceUUID, UUID characteristicUUID, CommandObserver observer) {
        super(serviceUUID, characteristicUUID, observer);
    }

    @Override
    protected void start(Connection connection, BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(serviceUUID);
        if (service != null) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
            if (characteristic != null) {
                NeatleLogger.d("Reading characteristics " + characteristicUUID);
                if (gatt.readCharacteristic(characteristic)) {
                    return;
                }
                NeatleLogger.d("Read failed" + characteristicUUID);
            } else {
                NeatleLogger.e("Could not find characteristics " + characteristicUUID);
            }
        } else {
            NeatleLogger.e("Could not find service " + serviceUUID);
        }

        finish(CommandResult.createErrorResult(characteristicUUID, BluetoothGatt.GATT_FAILURE));
    }

    @Override
    protected void onError(int error) {
        NeatleLogger.e("Unexpected error while reading [" + error + "]");
        finish(CommandResult.createErrorResult(characteristicUUID, error));
    }

    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (!characteristic.getUuid().equals(characteristicUUID)) {
            NeatleLogger.e("Got a read request for a unknown characteristic");
            return;
        }

        CommandResult result = CommandResult.createCharacteristicRead(characteristic, status);
        NeatleLogger.d("Read: " + result);
        finish(result);
    }

    @Override
    public String toString() {
        return "ReadCommand[" + characteristicUUID + "] on [" + serviceUUID + "]";
    }
}
