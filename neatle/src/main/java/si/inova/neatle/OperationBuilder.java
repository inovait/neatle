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

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import java.util.LinkedList;
import java.util.UUID;

public class OperationBuilder {

    private LinkedList<Command> cmds = new LinkedList<>();
    private OperationObserver masterObserver;
    private int retryCount;
    private Context context;

    public OperationBuilder(Context context) {
        this.context = context.getApplicationContext();
    }

    protected OperationBuilder subscribeNotification(UUID serviceUUID, UUID characteristicsUUID, OperationObserver observer) {
        SubscribeCommand cmd = new SubscribeCommand(SubscribeCommand.SUBSCRIBE_NOTIFICATION,
                serviceUUID, characteristicsUUID, observer);
        cmds.add(cmd);
        return this;
    }

    protected OperationBuilder unsubscribeNotification(UUID serviceUUID, UUID characteristicsUUID, OperationObserver observer) {
        SubscribeCommand cmd = new SubscribeCommand(SubscribeCommand.UNSUBSCRIBE,
                serviceUUID, characteristicsUUID, observer);
        cmds.add(cmd);
        return this;
    }

    /**
     * Reads the the value of a characteristic from a service.
     *
     * @param serviceUUID         the UUID of the service
     * @param characteristicsUUID the UUID of the characteristic.
     * @return this object
     */
    public OperationBuilder read(UUID serviceUUID, UUID characteristicsUUID) {
        return read(serviceUUID, characteristicsUUID, null);
    }

    /**
     * Reads the the value of a characteristic from a service.
     *
     * @param serviceUUID         the UUID of the service
     * @param characteristicsUUID the UUID of the characteristic.
     * @param observer            the observer for this specific command
     * @return this object
     */
    public OperationBuilder read(UUID serviceUUID, UUID characteristicsUUID, OperationObserver observer) {
        ReadCommand cmd = new ReadCommand(serviceUUID, characteristicsUUID, observer);
        cmds.add(cmd);
        return this;
    }

    /**
     * Writes data to a characteristic of a service.
     *
     * @param serviceUUID         the UUID of the service
     * @param characteristicsUUID the UUID of the characteristic.
     * @param buffer              the source of data for the write command
     * @return this object
     */
    public OperationBuilder write(UUID serviceUUID, UUID characteristicsUUID, InputSource buffer) {
        return write(serviceUUID, characteristicsUUID, buffer, null);
    }

    /**
     * Writes data to a characteristic of a service.
     *
     * @param serviceUUID         the UUID of the service
     * @param characteristicsUUID the UUID of the characteristic.
     * @param buffer              the source of data for the write command
     * @return this object
     */
    public OperationBuilder write(UUID serviceUUID, UUID characteristicsUUID, InputSource buffer, OperationObserver operationObserver) {
        WriteCommand cmd = new WriteCommand(serviceUUID, characteristicsUUID,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT, buffer, operationObserver);
        cmds.add(cmd);
        return this;
    }

    public OperationBuilder writeNoResponse(UUID serviceUUID, UUID characteristicsUUID, byte[] data) {
        return writeNoResponse(serviceUUID, characteristicsUUID, data, null);
    }

    public OperationBuilder writeNoResponse(UUID serviceUUID, UUID characteristicsUUID, byte[] data, OperationObserver operationObserver) {
        return writeNoResponse(serviceUUID, characteristicsUUID, new ByteArrayInputSource(data), operationObserver);
    }

    public OperationBuilder writeNoResponse(UUID serviceUUID, UUID characteristicsUUID, InputSource source) {
        return writeNoResponse(serviceUUID, characteristicsUUID, source, null);
    }

    public OperationBuilder writeNoResponse(UUID serviceUUID, UUID characteristicsUUID, InputSource source, OperationObserver operationObserver) {
        WriteCommand cmd = new WriteCommand(serviceUUID, characteristicsUUID,
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE, source, operationObserver);
        cmds.add(cmd);
        return this;
    }

    /**
     * Sets an {@link OperationObserver} that is triggered when all operations have been executed.
     *
     * @param operationObserver the operation observer
     * @return this object
     */
    public OperationBuilder onFinished(OperationObserver operationObserver) {
        masterObserver = operationObserver;
        return this;
    }

    /**
     * Set how many times the operation should retry in case of an error.
     *
     * @param count the number of times to retry. The default is 0. Use -1 to retry indefinitely.
     * @return this builder instance.
     */
    public OperationBuilder retryCount(int count) {
        this.retryCount = count;
        return this;
    }

    public Operation build(BluetoothDevice device) {
        return new OperationImpl(context, device, cmds, masterObserver, retryCount);
    }
}
