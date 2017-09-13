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
import android.os.Handler;

import java.io.IOException;
import java.util.UUID;

import si.inova.neatle.monitor.Connection;
import si.inova.neatle.source.AsyncInputSource;
import si.inova.neatle.source.InputSource;
import si.inova.neatle.util.NeatleLogger;

class WriteCommand extends SingleCharacteristicsCommand {

    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic writeCharacteristic;
    private final int writeType;

    private final InputSource buffer;

    private final Handler handler = new Handler();
    private final Object bufferReadLock = new Object();
    private final boolean asyncMode;
    private Thread readerThread;

    WriteCommand(UUID serviceUUID, UUID characteristicsUUID, int writeType, InputSource buffer, CommandObserver observer) {
        super(serviceUUID, characteristicsUUID, observer);
        this.buffer = buffer;
        this.writeType = writeType;
        this.asyncMode = buffer instanceof AsyncInputSource;
    }

    @Override
    protected void start(Connection connection, BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(serviceUUID);
        if (service == null) {
            NeatleLogger.i("Service for write not found [" + serviceUUID + "]");
            finish(CommandResult.createErrorResult(characteristicUUID, BluetoothGatt.GATT_FAILURE));
            return;
        }

        writeCharacteristic = service.getCharacteristic(characteristicUUID);
        if (writeCharacteristic == null) {
            NeatleLogger.i("Characteristic not found [" + characteristicUUID + "]");
            finish(CommandResult.createErrorResult(characteristicUUID, BluetoothGatt.GATT_FAILURE));
            return;
        }
        writeCharacteristic.setWriteType(writeType);

        this.gatt = gatt;
        if (asyncMode) {
            readerThread = new Thread(new AsyncReader());
            readerThread.start();
        } else {
            try {
                buffer.open();
                nextChunkReady(buffer.nextChunk());
            } catch (IOException ex) {
                NeatleLogger.e("Failed to read from the input source", ex);
                finish(CommandResult.createErrorResult(characteristicUUID, BluetoothGatt.GATT_FAILURE));
            }
        }
    }

    @Override
    protected void onFinished(CommandResult result) {
        super.onFinished(result);
        if (readerThread != null) {
            readerThread.interrupt();
        }
        NeatleLogger.d("Writing finished [" + characteristicUUID + "]");
    }

    private void nextChunkReady(byte[] chunk) {
        if (chunk == null) {
            finish(CommandResult.createEmptySuccess(characteristicUUID));
            return;
        }

        NeatleLogger.d("Writing " + chunk.length + " bytes onto " + writeCharacteristic.getUuid());
        writeCharacteristic.setValue(chunk);
        if (!gatt.writeCharacteristic(writeCharacteristic)) {
            NeatleLogger.d("Write returned false");
            finish(CommandResult.createErrorResult(characteristicUUID, BluetoothGatt.GATT_FAILURE));
        }
    }

    @Override
    protected void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            NeatleLogger.i("Write on " + characteristic.getUuid() + " failed with status " + status);
            finish(CommandResult.createErrorResult(characteristicUUID, status));
            return;
        }

        if (asyncMode) {
            synchronized (bufferReadLock) {
                bufferReadLock.notify();
            }
        } else {
            byte[] chunk;
            try {
                chunk = buffer.nextChunk();
            } catch (IOException ex) {
                NeatleLogger.e("Failed to get the first chunk", ex);
                finish(CommandResult.createErrorResult(characteristicUUID, BluetoothGatt.GATT_FAILURE));
                return;
            }
            if (chunk == null) {
                finish(CommandResult.createEmptySuccess(characteristicUUID));
                return;
            }
            nextChunkReady(chunk);
        }
    }

    @Override
    protected void onError(int error) {
        NeatleLogger.e("Unexpected error while writing [" + error + "]");
        finish(CommandResult.createErrorResult(characteristicUUID, error));

        if (asyncMode && readerThread != null) {
            readerThread.interrupt();
        }
    }

    @Override
    public String toString() {
        return "WriteCommand[async:" + asyncMode + " - " + characteristicUUID + "] on [" + serviceUUID + "]";
    }

    private class AsyncReader implements Runnable {
        public void run() {
            try {
                buffer.open();
            } catch (IOException io) {
                fail(io);
                return;
            }
            try {
                while (!Thread.interrupted()) {
                    final byte[] chunk = buffer.nextChunk();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            nextChunkReady(chunk);
                        }
                    });

                    synchronized (bufferReadLock) {
                        bufferReadLock.wait();
                    }

                    if (chunk == null) {
                        break;
                    }
                }
            } catch (IOException | InterruptedException ex) {
                fail(ex);
            }
        }

        private void fail(Exception ex) {
            NeatleLogger.e("Failed to read", ex);
            try {
              buffer.close();
            } catch (IOException closeEx) {
                NeatleLogger.e("Failed to close input source", closeEx);
            } finally {
                finish(CommandResult.createErrorResult(characteristicUUID, BluetoothGatt.GATT_FAILURE));
            }
        }
    }
}
