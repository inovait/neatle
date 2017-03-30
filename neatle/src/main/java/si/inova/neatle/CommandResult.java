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

public class CommandResult {

    private final UUID uuid;
    private final byte[] data;
    private final int status;
    private final long when;

    protected CommandResult(UUID uuid, byte[] data, int status, long when) {
        this.uuid = uuid;
        this.data = data;
        this.status = status;
        this.when = when;
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

    public long getWhen() {
        return when;
    }

    public boolean wasSuccessful() {
        return status == BluetoothGatt.GATT_SUCCESS;
    }

    @Override
    public String toString() {
        return "CommandResult[status: " + status + ", uuid:" + uuid + ", data:" + (data == null ? "null" : data.length) + "]";
    }

    public static CommandResult onCharacteristicRead(BluetoothGattCharacteristic characteristic, int status) {
        long when = System.currentTimeMillis();
        return new CommandResult(characteristic.getUuid(), characteristic.getValue(), status, when);
    }

    public static CommandResult createErrorResult(UUID characteristicUUID, int error) {
        long when = System.currentTimeMillis();
        return new CommandResult(characteristicUUID, null, error, when);
    }

    public static CommandResult createEmptySuccess(UUID characteristicUUID) {
        long when = System.currentTimeMillis();
        return new CommandResult(characteristicUUID, null, BluetoothGatt.GATT_SUCCESS, when);
    }

    public static CommandResult onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
        long when = System.currentTimeMillis();
        return new CommandResult(characteristic.getUuid(), characteristic.getValue(), BluetoothGatt.GATT_SUCCESS, when);
    }

    public int getValueAsInt8() {
        return ByteBuffer.wrap(data).get(0);
    }

    public int getValueAsInt32() {
        return ByteBuffer.wrap(data).getInt();
    }
}
