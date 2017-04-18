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

package si.inova.neatle.monitor;

import android.bluetooth.BluetoothDevice;

/**
 * Monitors the state of a connection with a bluetooth device.
 */
public interface ConnectionMonitor {

    /**
     * When true, the connection will stay alive even if there is no active subscription or any
     * pending commands.
     *
     * @param keepAlive {@code true} to keep the connection alive, {@code false} to
     *                  disconnect when the connection is idle (and no other connection monitor
     *                  is set to keep alive)
     */
    void setKeepAlive(boolean keepAlive);

    /**
     * Adds a connection state listener to this monitor. The listener will be triggered on any
     * changes to the connection (e.g. connect / disconnect).
     *
     * @param connectionStateListener the lsitener.
     */
    void setOnConnectionStateListener(ConnectionStateListener connectionStateListener);

    /**
     * Starts this connection monitor, if it's not already running.
     */
    void start();

    /**
     * Stops this connection monitor, if it's running.
     */
    void stop();

    /**
     * Returns the device this connection monitor is listening for.
     *
     * @return the bluetooth device.
     */
    BluetoothDevice getDevice();
}
