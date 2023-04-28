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

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import androidx.annotation.CheckResult;
import androidx.annotation.RestrictTo;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.Callable;

import si.inova.neatle.source.CalllableInputSource;
import si.inova.neatle.source.InputSource;

public class OperationBuilder {

    private Context context;

    private LinkedList<Command> commands = new LinkedList<>();
    private OperationObserver masterObserver;
    private int retryCount;

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public OperationBuilder(Context context) {
        this.context = context.getApplicationContext();
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
    public OperationBuilder read(UUID serviceUUID, UUID characteristicsUUID, CommandObserver observer) {
        ReadCommand cmd = new ReadCommand(serviceUUID, characteristicsUUID, observer);
        commands.add(cmd);
        return this;
    }

    /**
     * Writes data to a characteristic of a service.
     *
     * @param serviceUUID         the UUID of the service
     * @param characteristicsUUID the UUID of the characteristic.
     * @param source              the source of data for the write command
     * @return this object
     */
    public OperationBuilder write(UUID serviceUUID, UUID characteristicsUUID, InputSource source) {
        return write(serviceUUID, characteristicsUUID, source, null);
    }

    /**
     * Writes data provided by callable. The callable will be evaluated when it is time to
     * write. The callable will be re-evaluated if the operation is retried.
     *
     * @param serviceUUID the UUID of the service
     * @param characteristicsUUID the UUID of the characteristic.
     * @param callable the callable implantation that will be evaluated when it's time to write the value.
     *
     * @return this object
     */
    public OperationBuilder write(UUID serviceUUID, UUID characteristicsUUID, Callable<ByteBuffer> callable) {
        return write(serviceUUID, characteristicsUUID, new CalllableInputSource(callable));
    }

    /**
     * Writes data to a characteristic of a service.
     *
     * @param serviceUUID         the UUID of the service
     * @param characteristicsUUID the UUID of the characteristic.
     * @param source              the source of data for the write command
     * @param observer            the operation observer - callback
     * @return this object
     */
    public OperationBuilder write(UUID serviceUUID, UUID characteristicsUUID, InputSource source, CommandObserver observer) {
        WriteCommand cmd = new WriteCommand(serviceUUID, characteristicsUUID, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT, source, observer);
        commands.add(cmd);
        return this;
    }

    /**
     * Writes data to a characteristic of a service, but does not require a response from the
     * BTLE device.
     *
     * @param serviceUUID         the UUID of the service
     * @param characteristicsUUID the UUID of the characteristic.
     * @param source              the source of data for the write command
     * @return this object
     */
    public OperationBuilder writeNoResponse(UUID serviceUUID, UUID characteristicsUUID, InputSource source) {
        return writeNoResponse(serviceUUID, characteristicsUUID, source, null);
    }

    /**
     * Writes data to a characteristic of a service, but does not require a response from the BTLE
     * device.
     *
     * @param serviceUUID         the UUID of the service
     * @param characteristicsUUID the UUID of the characteristic.
     * @param source              the source of data for the write command
     * @param observer            the operation observer - callback
     * @return this object
     */
    public OperationBuilder writeNoResponse(UUID serviceUUID, UUID characteristicsUUID, InputSource source, CommandObserver observer) {
        WriteCommand cmd = new WriteCommand(serviceUUID, characteristicsUUID, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE, source, observer);
        commands.add(cmd);
        return this;
    }

    /**
     * Executes the given custom command.
     *
     * @param cmd a custom command implementation.
     *
     * @return this object
     */
    public OperationBuilder executeCommand(Command cmd) {
        commands.add(cmd);
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

    protected OperationBuilder subscribeNotification(UUID serviceUUID, UUID characteristicsUUID, CommandObserver observer) {
        SubscribeCommand cmd = new SubscribeCommand(SubscribeCommand.Type.SUBSCRIBE_NOTIFICATION, serviceUUID, characteristicsUUID, observer);
        commands.add(cmd);
        return this;
    }

    protected OperationBuilder unsubscribeNotification(UUID serviceUUID, UUID characteristicsUUID, CommandObserver observer) {
        SubscribeCommand cmd = new SubscribeCommand(SubscribeCommand.Type.UNSUBSCRIBE, serviceUUID, characteristicsUUID, observer);
        commands.add(cmd);
        return this;
    }

    /**
     * Creates the operation. Note that you still need to call {@link Operation#execute()} to start
     * the operation.
     *
     * @param device the device on which this operation will run
     * @return the created operation
     */
    @CheckResult
    public Operation build(BluetoothDevice device) {
        if (device == null) {
            throw new IllegalArgumentException("Device cannot be null");
        }

        return new OperationImpl(context, device, commands, retryCount, masterObserver);
    }
}
