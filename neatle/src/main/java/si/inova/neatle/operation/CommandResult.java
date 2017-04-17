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
import android.support.annotation.RestrictTo;

import java.nio.charset.Charset;
import java.util.UUID;

/**
 * A result of a single command.
 */
public class CommandResult {

    private final UUID uuid;
    private final byte[] data;
    private final int status;
    private final long timestamp;

    CommandResult(UUID uuid, byte[] data, int status, long timestamp) {
        this.uuid = uuid;
        this.data = data;
        this.status = status;
        this.timestamp = timestamp;
    }

    /**
     * Returns the raw response of a command, in bytes.
     *
     * @return the response in bytes
     */
    public byte[] getValue() {
        return data;
    }

    /**
     * Returns the string representation of the command response (in UTF8 encoding). If the data
     * received is not a string, it throws an {@link IllegalStateException}.
     *
     * @return the string representation of the command response.
     * @throws IllegalStateException if the command data is not a UTF8 string
     */
    public String getValueAsString() {
        if (data == null) {
            return null;
        } else if (data.length == 0) {
            return "";
        } else {
            return new String(data, Charset.forName("UTF8"));
        }
    }

    /**
     * Returns the value of the command result represented as an int32 (int). It will thro a
     * {@link java.nio.BufferUnderflowException} if the data has less than 4 bytes (e.g. is not an int).
     *
     * @return the value of the data as an int.
     */
    public int getValueAsInt() {
        if (data.length > 4) {
            throw new IllegalStateException("Data has more than 4 bytes and cannot be converted to an integer");
        }

        int ret = 0;
        switch (data.length) {
            case 4:
                ret |= (data[3] & 0xFF) << ((data.length - 4) * 8);
            case 3:
                ret |= (data[2] & 0xFF) << ((data.length - 3) * 8);
            case 2:
                ret |= (data[1] & 0xFF) << ((data.length - 2) * 8);
            case 1:
                ret |= (data[0] & 0xFF) << ((data.length - 1) * 8);
        }

        return ret;
    }

    /**
     * Returns the UUID of the characteristic this data was read from.
     *
     * @return the uuid
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Returns the status of the command execution. For instance, {@link BluetoothGatt#GATT_SUCCESS}
     * if the command was successful.
     *
     * @return the status of the command execution.
     */
    public int getStatus() {
        return status;
    }

    /**
     * Returns the timestamp of this command.
     *
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Checks if this command was successful.
     *
     * @return true if the command was succesful, false otherwise
     */
    public boolean wasSuccessful() {
        return status == BluetoothGatt.GATT_SUCCESS;
    }

    @Override
    public String toString() {
        return "CommandResult[status: " + status + ", uuid:" + uuid + ", data:" + (data == null ? "null" : data.length) + "]";
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static CommandResult createCharacteristicRead(BluetoothGattCharacteristic characteristic, int status) {
        long when = System.currentTimeMillis();
        return new CommandResult(characteristic.getUuid(), characteristic.getValue(), status, when);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static CommandResult createErrorResult(UUID characteristicUUID, int error) {
        long when = System.currentTimeMillis();
        return new CommandResult(characteristicUUID, null, error, when);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static CommandResult createEmptySuccess(UUID characteristicUUID) {
        long when = System.currentTimeMillis();
        return new CommandResult(characteristicUUID, null, BluetoothGatt.GATT_SUCCESS, when);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static CommandResult createCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
        long when = System.currentTimeMillis();
        return new CommandResult(characteristic.getUuid(), characteristic.getValue(), BluetoothGatt.GATT_SUCCESS, when);
    }
}
