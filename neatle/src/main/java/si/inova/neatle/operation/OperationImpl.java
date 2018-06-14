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
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.RestrictTo;

import java.util.Collection;
import java.util.LinkedList;

import si.inova.neatle.Device;
import si.inova.neatle.monitor.Connection;
import si.inova.neatle.util.DeviceManager;
import si.inova.neatle.util.NeatleLogger;

class OperationImpl implements Operation {

    private static Command EMPTY_COMMAND = new EmptyCommand();

    private final Context context;

    private final LinkedList<Command> commands;
    private LinkedList<Command> commandQueue;
    private Command currentCommand = EMPTY_COMMAND;
    private CommandResult lastResult;

    private final OperationObserver operationObserver;
    private OperationResults results;

    private final BluetoothDevice device;
    private Device connection;

    private final CommandHandler commandHandler = new CommandHandler();
    private final Handler handler = new Handler();
    private final GattCallback callback = new GattCallback();
    private BluetoothGatt gatt;

    private final int retryCount;
    private int retriedCount = 0;

    private boolean yielded;
    private boolean canceled = false;

    OperationImpl(Context context, BluetoothDevice device, Collection<Command> commands, int retryCount, OperationObserver operationObserver) {
        this.context = context;
        this.device = device;
        this.commands = new LinkedList<>(commands);
        this.commandQueue = new LinkedList<>(commands);
        this.retryCount = retryCount;
        this.operationObserver = operationObserver;
    }

    @Override
    public void execute() {
        if (connection != null) {
            return;
        }

        Device conn;
        synchronized (this) {
            this.connection = conn = DeviceManager.getInstance(context).getDevice(device);
            this.results = new OperationResults();
            this.commandQueue = new LinkedList<>(commands);
            this.currentCommand = EMPTY_COMMAND;
            this.retriedCount = 0;
            this.canceled = false;
            this.lastResult = null;
        }
        conn.execute(callback);
    }

    private void retry() {
        Device conn;
        synchronized (this) {
            retriedCount++;
            NeatleLogger.i("Retrying operation, attempt:" + retriedCount);
            results = new OperationResults();
            commandQueue = new LinkedList<>(commands);
            conn = connection;
            lastResult = null;
        }
        conn.execute(callback);
    }

    @Override
    public void cancel() {
        if (connection == null) {
            return;
        }

        synchronized (this) {
            canceled = true;
        }

        done();
    }

    @Override
    public synchronized boolean isCanceled() {
        return canceled;
    }

    private void done() {
        Device conn;
        boolean wasExecuting;
        synchronized (this) {
            wasExecuting = this.connection != null;
            conn = this.connection;
            this.connection = null;
        }

        if (wasExecuting) {
            conn.executeFinished(callback);
            NeatleLogger.d("Operation finished, success: " + results.wasSuccessful() + ", cancel:" + isCanceled());

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (isCanceled()) {
                        return;
                    }
                    if (operationObserver != null) {
                        operationObserver.onOperationFinished(OperationImpl.this, results);
                    }
                }
            });
        }
    }

    private void executeNext() {
        Command cmd;
        Device targetDevice;
        synchronized (this) {
            targetDevice = connection;
            if (yielded) {
                return;
            }
            if (lastResult != null && !lastResult.wasSuccessful()) {
                if (retryCount == -1 || retriedCount + 1 <= retryCount) {
                    retry();
                    return;
                }
                NeatleLogger.i("Command failed. Aborting operation. Error: " + lastResult.getStatus());
                done();
                return;
            }

            Command old = currentCommand;
            currentCommand = commandQueue.poll();
            NeatleLogger.d("Continuing with " + currentCommand + " after " + old + " with " + lastResult);
            if (currentCommand == null) {
                currentCommand = EMPTY_COMMAND;
                done();
                return;
            }
            cmd = currentCommand;
        }
        NeatleLogger.d("Executing command: " + currentCommand);
        if (operationObserver != null) {
            operationObserver.onCommandStarted(this, cmd);
        }

        cmd.execute(targetDevice, commandHandler, gatt);
    }

    private void scheduleNext() {
        NeatleLogger.d("Scheduling next command after : " + currentCommand);
        handler.post(new Runnable() {
            @Override
            public void run() {
                executeNext();
            }
        });
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    LinkedList<Command> getCommands() {
        return commands;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    OperationObserver getOperationObserver() {
        return operationObserver;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    int getRetryCount() {
        return retryCount;
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    BluetoothDevice getDevice() {
        return device;
    }

    @Override
    public String toString() {
        return "Operation[retryCount: " + retryCount + ", attempts: " + retriedCount + ", commands:" + this.commands + "]";
    }

    private static class EmptyCommand extends Command {

        private EmptyCommand() {
            super(null);
        }

        @Override
        protected void start(Connection connection, BluetoothGatt gatt) {
            //finish();
        }

        @Override
        protected void onError(int error) {
            //do nothing
        }
    }

    private class CommandHandler implements CommandObserver {
        @Override
        public void finished(final Command command, final CommandResult result) {
            synchronized (OperationImpl.this) {
                lastResult = result;
                results.addResult(result);
                //once the command is finished, don't forward any more events
                currentCommand = EMPTY_COMMAND;
            }

            NeatleLogger.d("Command finished, status: " + result.getStatus() + ", command:" + command + ", on: " + device.getAddress());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (operationObserver != null) {
                        if (result.wasSuccessful()) {
                            operationObserver.onCommandSuccess(OperationImpl.this, command, result);
                        } else {
                            operationObserver.onCommandError(OperationImpl.this, command, result.getStatus());
                        }
                    }
                }
            });

            scheduleNext();
        }
    }

    private class GattCallback extends BluetoothGattCallback {

        @Override
        @SuppressWarnings("PMD.CompareObjectsWithEquals")
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            NeatleLogger.i("OperationImpl: onConnectionStateChange, state:" + status + ", newState: " + newState);

            Command cur;
            synchronized (OperationImpl.this) {
                cur = currentCommand;
                if (newState != BluetoothGatt.STATE_CONNECTED && cur == EMPTY_COMMAND &&
                        (lastResult == null || lastResult.wasSuccessful())) {
                    lastResult = CommandResult.createErrorResult(null, BluetoothGatt.GATT_FAILURE);
                    scheduleNext();
                    return;
                }
            }
            cur.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                synchronized (OperationImpl.this) {
                    yielded = false;
                    OperationImpl.this.gatt = gatt;
                }
                scheduleNext();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            currentCommand.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            currentCommand.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            currentCommand.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            currentCommand.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            currentCommand.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            currentCommand.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            currentCommand.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            currentCommand.onMtuChanged(gatt, mtu, status);
        }

        @Override
        public String toString() {
            return "Callback[" + OperationImpl.this.toString() + "]";
        }
    }
}
