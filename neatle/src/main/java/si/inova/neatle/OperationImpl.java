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
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.os.Handler;

import java.util.Collection;
import java.util.LinkedList;

class OperationImpl implements Operation {

    private static Command NO_COMMAND = new NoCommand();

    private GattCallback callback = new GattCallback();
    private final OperationObserver operationObserver;
    private final int retryCount;

    private OperationResults results;
    private CommandHandler commandHandler = new CommandHandler();
    private BluetoothGatt gatt;
    private Handler handler;

    private int retriedCount = 0;

    private boolean yielded;
    private boolean canceled = false;

    private Command current = NO_COMMAND;
    private LinkedList<Command> commands;
    private LinkedList<Command> commandQueue;

    private CommandResult lastResult;

    private Device connection;
    private final BluetoothDevice device;

    private Context context;

    OperationImpl(Context context, BluetoothDevice device, Collection<Command> cmds, OperationObserver operationObserver, int retryCount) {
        if (device == null) {
            throw new IllegalArgumentException("Device cannot be null");
        }

        this.device = device;
        this.commands = new LinkedList<>(cmds);
        this.commandQueue = new LinkedList<>(cmds);
        this.operationObserver = operationObserver;
        this.retryCount = retryCount;
        this.context = context;
        handler = new Handler();
    }

    @Override
    public void execute() {
        //FIXME what happens if this operation is in the middle of being executed
        Device conn;
        synchronized (this) {
            this.connection = DeviceManager.getInstance(context).getDevice(device);
            conn = this.connection;
            results = new OperationResults(device);
            this.commandQueue = new LinkedList<>(commands);
            this.current = NO_COMMAND;
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
            results = new OperationResults(device);
            commandQueue = new LinkedList<>(commands);
            conn = connection;
            lastResult = null;
        }
        conn.execute(callback);
    }

    @Override
    public void cancel() {
        synchronized (this) {
            canceled = true;
        }
        done(BluetoothGatt.GATT_SUCCESS);
    }

    @Override
    public synchronized boolean isCanceled() {
        return canceled;
    }

    private void done(int status) {
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

    private synchronized Command current() {
        return current;
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
                done(lastResult.getStatus());
                return;
            }

            Command old = current;
            current = commandQueue.poll();
            NeatleLogger.d("Continuing with " + current + " after " + old + " with " + lastResult);
            if (current == null) {
                current = NO_COMMAND;
                done(BluetoothGatt.GATT_SUCCESS);
                return;
            }
            cmd = current;
        }
        NeatleLogger.d("Executing command: " + current);
        if (cmd.getOperationObserver() != null) {
            cmd.getOperationObserver().onCommandExecuting(this, results);
        }

        cmd.execute(targetDevice, commandHandler, gatt, results);
    }

    private void scheduleNext() {
        NeatleLogger.d("Scheduling next command after : " + current());
        handler.post(new Runnable() {
            @Override
            public void run() {
                executeNext();
            }
        });
    }

    private boolean failIfError(int status) {
        return status != BluetoothGatt.GATT_SUCCESS;
    }

    @Override
    public String toString() {
        return "Operation[retryCount: " + retryCount + ", attempts: " + retriedCount + ", commands:" + this.commands + "]";
    }

    private class CommandHandler implements CommandObserver {
        @Override
        public void finished(final Command command, final CommandResult result) {
            synchronized (OperationImpl.this) {
                lastResult = result;
                results.addCommandResult(result);
                //once the command is finished, don't forward any more events
                current = NO_COMMAND;
            }

            NeatleLogger.d("Command finished, status: " + result.getStatus() + ", command:" + command);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    OperationObserver opObserver = command.getOperationObserver();
                    if (opObserver != null) {
                        if (result.wasSuccessful()) {
                            opObserver.onCommandSuccess(OperationImpl.this, result, results);
                        }
                        opObserver.onCommandFinished(OperationImpl.this, result, results);
                    }
                }
            });

            scheduleNext();
        }
    }

    private class GattCallback extends BluetoothGattCallback {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            NeatleLogger.i("OperationImpl: onConnectionStateChange, state:" + status + ", newState: " + newState);
            Command cur;
            synchronized (OperationImpl.this) {
                cur = current();
                if (newState != BluetoothGatt.STATE_CONNECTED && cur == NO_COMMAND
                        && (lastResult == null || lastResult.wasSuccessful())) {
                    lastResult = CommandResult.createErrorResult(null, BluetoothGatt.GATT_FAILURE);
                    scheduleNext();
                    return;
                }
            }
            cur.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (!failIfError(status)) {
                synchronized (OperationImpl.this) {
                    yielded = false;
                    OperationImpl.this.gatt = gatt;
                }
                scheduleNext();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            current().onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            current().onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            current().onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            current().onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            current().onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            current().onMtuChanged(gatt, mtu, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            current().onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            current().onReliableWriteCompleted(gatt, status);
        }

        @Override
        public String toString() {
            return "Callback[" + OperationImpl.this.toString() + "]";
        }
    }

    private static class NoCommand extends Command {
        private NoCommand() {
            super(null);
        }

        @Override
        protected void execute(Connection connection, BluetoothGatt gatt, OperationResults results) {
            throw new IllegalStateException("Should no be called");
        }

        @Override
        protected void onError(int error) {
        }
    }
}
