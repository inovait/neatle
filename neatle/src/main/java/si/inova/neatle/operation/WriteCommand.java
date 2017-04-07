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

class WriteCommand extends Command {

    private final UUID serviceUUID;
    private final UUID characteristicsUUID;
    private final InputSource buffer;
    private final Handler handler;
    private BluetoothGattCharacteristic writeCharacteristic;

    private Thread readerThread;
    private BluetoothGatt gatt;
    private int writeType;

    private final Object readyToRead = new Object();
    private boolean reading = false;
    private final boolean asyncMode;

    WriteCommand(UUID serviceUUID, UUID characteristicsUUID, int writeType, InputSource buffer, OperationObserver observer) {
        super(observer);

        this.serviceUUID = serviceUUID;
        this.characteristicsUUID = characteristicsUUID;
        this.buffer = buffer;
        this.writeType = writeType;
        this.handler = new Handler();

        this.asyncMode = buffer instanceof AsyncInputSource;
    }

    @Override
    protected void execute(Connection connection, BluetoothGatt gatt, OperationResults results) {
        BluetoothGattService service = gatt.getService(serviceUUID);
        if (service == null) {
            NeatleLogger.i("Service for write not found " + serviceUUID);
            finish(CommandResult.createErrorResult(characteristicsUUID, BluetoothGatt.GATT_FAILURE));
            return;
        }

        writeCharacteristic = service.getCharacteristic(characteristicsUUID);
        if (writeCharacteristic == null) {
            NeatleLogger.i("Characteristic not found " + characteristicsUUID);
            finish(CommandResult.createErrorResult(characteristicsUUID, BluetoothGatt.GATT_FAILURE));
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
                NeatleLogger.e("Failed to get the first chunk", ex);
                finish(CommandResult.createErrorResult(characteristicsUUID, BluetoothGatt.GATT_FAILURE));
            }
        }
    }

    @Override
    protected void finish(CommandResult result) {
        super.finish(result);
        if (readerThread != null) {
            readerThread.interrupt();
        }
        NeatleLogger.d("Writing finished" + characteristicsUUID);
    }

    private void nextChunkReady(byte[] chunk) {
        if (chunk == null) {
            finish(CommandResult.createEmptySuccess(characteristicsUUID));
            return;
        }

        NeatleLogger.d("Writing " + chunk.length + " bytes onto " + writeCharacteristic.getUuid());

        writeCharacteristic.setValue(chunk);
        if (!gatt.writeCharacteristic(writeCharacteristic)) {
            NeatleLogger.d("Write returned false");
            finish(CommandResult.createErrorResult(characteristicsUUID, BluetoothGatt.GATT_FAILURE));
        }
    }

    @Override
    protected void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            NeatleLogger.i("Write on " + characteristic.getUuid() + " failed with status " + status);
            finish(CommandResult.createErrorResult(characteristicsUUID, status));
            return;
        }

        if (this.asyncMode) {
            synchronized (readyToRead) {
                readyToRead.notify();
            }
        } else {
            byte[] chunk;
            try {
                chunk = buffer.nextChunk();
            } catch (IOException ex) {
                NeatleLogger.e("Failed to get the first chunk", ex);
                finish(CommandResult.createErrorResult(characteristicsUUID, BluetoothGatt.GATT_FAILURE));
                return;
            }
            if (chunk == null) {
                finish(CommandResult.createEmptySuccess(characteristicsUUID));
                return;
            }
            nextChunkReady(chunk);
        }
    }


    @Override
    protected void onError(int error) {
        NeatleLogger.e("Unexpected error while writing ");
        finish(CommandResult.createErrorResult(characteristicsUUID, error));

        if (asyncMode && readerThread != null) {
            readerThread.interrupt();
        }
    }

    public String toString() {
        return "WriteCommand[ async:" + asyncMode + " - " + characteristicsUUID + "] on [" + serviceUUID + "]";
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
                reading = true;
                while (!Thread.interrupted()) {
                    final byte[] chunk = buffer.nextChunk();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            nextChunkReady(chunk);
                        }
                    });

                    synchronized (readyToRead) {
                        readyToRead.wait();
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
            //TODO: Handle failure
        }
    }
}
