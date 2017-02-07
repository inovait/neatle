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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Created by tomazs on 9/23/2016.
 */
public class CommandResult {

    private final UUID uuid;
    private final byte[] data;
    private final int status;


    protected CommandResult(UUID uuid, byte[] data, int status) {
        this.uuid = uuid;
        this.data = data;
        this.status = status;
    }


    public byte[] getValue() {
        return data;
    }

    public String getValueAsString() {
        if (data == null) {
            return null;
        } else if (data.length == 0) {
            return "";
        } else {
            try {
                return new String(data, "UTF8");
            } catch (UnsupportedEncodingException uee) {
                throw new IllegalStateException("Data is no encoded with UTF8");
            }
        }
    }

    public UUID getUUID() {
        return uuid;
    }

    public int getStatus() {
        return status;
    }

    public boolean wasSuccessful() {
        return status == BluetoothGatt.GATT_SUCCESS;
    }

    @Override
    public String toString() {
        return "CommandResult[status: " + status + ", uuid:" + uuid + ", data:" + (data == null ? "null" : data.length) + "]";
    }

    public static CommandResult onCharacteristicRead(BluetoothGattCharacteristic characteristic, int status) {
        return new CommandResult(characteristic.getUuid(), characteristic.getValue(), status);
    }

    public static CommandResult createErrorResult(UUID characteristicUUID, int error) {
        return new CommandResult(characteristicUUID, null, error);
    }

    public static CommandResult createEmptySuccess(UUID characteristicUUID) {
        return new CommandResult(characteristicUUID, null, BluetoothGatt.GATT_SUCCESS);
    }

    public static CommandResult onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
        return new CommandResult(characteristic.getUuid(), characteristic.getValue(), BluetoothGatt.GATT_SUCCESS);
    }

    public int getValueAsInt8() {
        return ByteBuffer.wrap(data).get(0);
    }
}
