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

    /**
     * Characteristic value format type uint8
     */
    public static final int FORMAT_UINT8 = 0x11;

    /**
     * Characteristic value format type uint16
     */
    public static final int FORMAT_UINT16 = 0x12;

    /**
     * Characteristic value format type uint32
     */
    public static final int FORMAT_UINT32 = 0x14;

    /**
     * Characteristic value format type sint8
     */
    public static final int FORMAT_SINT8 = 0x21;

    /**
     * Characteristic value format type sint16
     */
    public static final int FORMAT_SINT16 = 0x22;

    /**
     * Characteristic value format type sint32
     */
    public static final int FORMAT_SINT32 = 0x24;

    private final UUID uuid;
    private final byte[] data;
    private final int status;
    private final long timestamp;
    private final BluetoothGattCharacteristic characteristic;

    CommandResult(UUID uuid, byte[] data, int status, long timestamp, BluetoothGattCharacteristic characteristic) {
        this.uuid = uuid;
        this.data = data;
        this.status = status;
        this.timestamp = timestamp;
        this.characteristic = characteristic;
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
     * Return the stored value of this characteristic.
     * <p>
     * <p>The formatType parameter determines how the characteristic value
     * is to be interpreted. For example, settting formatType to
     * {@link BluetoothGattCharacteristic#FORMAT_UINT16} specifies that the first two bytes of the
     * characteristic value at the given offset are interpreted to generate the
     * return value.
     *
     * @param formatType The format type used to interpret the characteristic
     *                   value.
     * @param offset     Offset at which the integer value can be found.
     * @return Cached value of the characteristic or null of offset exceeds
     * value size.
     */
    public Integer getFormattedIntValue(int formatType, int offset) {
        if ((offset + formatType & 0xF) > data.length) return null;

        switch (formatType) {
            case FORMAT_UINT8:
                return unsignedByteToInt(data[offset]);
            case FORMAT_UINT16:
                return unsignedBytesToInt(data[offset], data[offset + 1]);
            case FORMAT_UINT32:
                return unsignedBytesToInt(data[offset], data[offset + 1],
                        data[offset + 2], data[offset + 3]);
            case FORMAT_SINT8:
                return unsignedToSigned(unsignedByteToInt(data[offset]), 8);
            case FORMAT_SINT16:
                return unsignedToSigned(unsignedBytesToInt(data[offset],
                        data[offset + 1]), 16);
            case FORMAT_SINT32:
                return unsignedToSigned(unsignedBytesToInt(data[offset],
                        data[offset + 1], data[offset + 2], data[offset + 3]), 32);
        }

        return null;
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
     * Returns the properties of the characteristic
     *
     * @return the properties
     */
    public int getProperties() {
        return characteristic.getProperties();
    }

    /**
     * Returns the int value stored in the characteristic. See
     * {@link BluetoothGattCharacteristic#getIntValue(int, int)} for further information.
     * @param formatType
     * @param offset
     * @return
     */
    public int getIntValue(int formatType, int offset) {
        return characteristic.getIntValue(formatType, offset);
    }

    /**
     * Returns the float value stored in the characteristic. See
     * {@link BluetoothGattCharacteristic#getFloatValue(int, int)} for further information.
     * @param formatType
     * @param offset
     * @return
     */
    public float getFloatValue(int formatType, int offset) {
        return characteristic.getFloatValue(formatType, offset);
    }

    /**
     * Returns the string value stored in the characteristic. See
     * {@link BluetoothGattCharacteristic#getStringValue(int)} for further information.
     * @param offset
     * @return
     */
    public String getStringValue(int offset) {
        return characteristic.getStringValue(offset);
    }

    /**
     * Checks if this command was successful.
     *
     * @return true if the command was succesful, false otherwise
     */
    public boolean wasSuccessful() {
        return status == BluetoothGatt.GATT_SUCCESS;
    }

    /**
     * Convert signed bytes to a 16-bit unsigned int.
     */
    private int unsignedBytesToInt(byte b0, byte b1) {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8));
    }

    /**
     * Convert a signed byte to an unsigned int.
     */
    private int unsignedByteToInt(byte b) {
        return b & 0xFF;
    }

    /**
     * Convert an unsigned integer value to a two's-complement encoded
     * signed value.
     */
    private int unsignedToSigned(int unsigned, int size) {
        if ((unsigned & (1 << size - 1)) != 0) {
            unsigned = -1 * ((1 << size - 1) - (unsigned & ((1 << size - 1) - 1)));
        }
        return unsigned;
    }

    /**
     * Convert signed bytes to a 32-bit unsigned int.
     */
    private int unsignedBytesToInt(byte b0, byte b1, byte b2, byte b3) {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8))
                + (unsignedByteToInt(b2) << 16) + (unsignedByteToInt(b3) << 24);
    }

    @Override
    public String toString() {
        return "CommandResult[status: " + status + ", uuid:" + uuid + ", data:" + (data == null ? "null" : data.length) + "]";
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static CommandResult createCharacteristicRead(BluetoothGattCharacteristic characteristic, int status) {
        long when = System.currentTimeMillis();
        return new CommandResult(characteristic.getUuid(), characteristic.getValue(), status, when, characteristic);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static CommandResult createErrorResult(UUID characteristicUUID, int error) {
        long when = System.currentTimeMillis();
        return new CommandResult(characteristicUUID, null, error, when, null);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static CommandResult createEmptySuccess(UUID characteristicUUID) {
        long when = System.currentTimeMillis();
        return new CommandResult(characteristicUUID, null, BluetoothGatt.GATT_SUCCESS, when, null);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static CommandResult createCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
        long when = System.currentTimeMillis();
        return new CommandResult(characteristic.getUuid(), characteristic.getValue(), BluetoothGatt.GATT_SUCCESS, when, characteristic);
    }
}
