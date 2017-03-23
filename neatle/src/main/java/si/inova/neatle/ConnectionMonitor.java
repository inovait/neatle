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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

/**
 * Monitors the connection with a bluetooth device.
 */
public class ConnectionMonitor {

    private static final long DEFAULT_RECONNECT_TIMEOUT = 2500L;
    private static final long MAX_RECONNECT_TIMEOUT = 60 * 1000L;

    private final BluetoothDevice device;
    private final Handler handler;
    private boolean keepAlive;
    private Connection connection;
    private ConnHandler connectionHandler = new ConnHandler();
    private ConnectionStateListener connectionStateListener;
    private ReconnectRunnable reconnectRunnable = new ReconnectRunnable();
    private Context context;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.STATE_OFF);

                if (state == BluetoothAdapter.STATE_ON) {
                    onBluetoothTurnedOn();
                }
            }
        }
    };

    public ConnectionMonitor(Context context, BluetoothDevice device) {
        if (device == null) {
            throw new IllegalArgumentException("Device cannot be null");
        } else if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        this.context = context.getApplicationContext();
        this.device = device;
        this.handler = new Handler();
    }

    /**
     * When true, the connection will stay alive even if there is no active subscription or any
     * pending commands.
     *
     * @param keepAlive {@code true} to keep the connection alive, {@code false} to
     *                  disconnect when the connection is idle (and no other connection monitor
     *                  is set to keep alive)
     */
    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    /**
     * Adds a connection state listener to this monitor. The listener will be triggered on any
     * changes to the connection (e.g. connect / disconnect).
     *
     * @param connectionStateListener the lsitener.
     */
    public void setOnConnectionStateListener(ConnectionStateListener connectionStateListener) {
        this.connectionStateListener = connectionStateListener;
    }

    /**
     * Starts this connection monitor, if it's not already running.
     */
    public void start() {
        if (connection != null) {
            return;
        }

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(mReceiver, filter);

        connection = Neatle.getConnection(context, device);
        connection.addConnectionHandler(connectionHandler);
        connection.addConnectionStateListener(connectionHandler);

        if (keepAlive) {
            connection.connect();
        }
    }

    /**
     * Stops this connection monitor, if it's running.
     */
    public void stop() {
        if (connection == null) {
            return;
        }

        context.unregisterReceiver(mReceiver);
        handler.removeCallbacks(reconnectRunnable);
        if (connection != null) {
            connection.removeConnectionStateListener(connectionHandler);
            connection.removeConnectionHandler(connectionHandler);
            connection = null;
        }
    }

    /**
     * Returns the device this connection monitor is listening for.
     *
     * @return the bluetooth device.
     */
    public BluetoothDevice getDevice() {
        return device;
    }

    private void onBluetoothTurnedOn() {
        if (keepAlive && connection != null) {
            connection.connect();
        }
    }

    private class ConnHandler implements ConnectionHandler, ConnectionStateListener {
        private long reconnectTimeout = DEFAULT_RECONNECT_TIMEOUT;

        @Override
        public int onConnectionIdle(Connection connection) {
            return keepAlive ? ON_IDLE_KEEP_ALIVE : ON_IDLE_DISCONNECT;
        }

        @Override
        public void onConnectionStateChanged(Connection connection, int newState) {
            if (connectionStateListener != null) {
                connectionStateListener.onConnectionStateChanged(connection, newState);
            }

            if (newState == BluetoothAdapter.STATE_DISCONNECTED) {
                handler.removeCallbacks(reconnectRunnable);
                NeatleLogger.d("Will try to reconnect after " + (reconnectTimeout / 1000) + " seconds");
                handler.postDelayed(reconnectRunnable, reconnectTimeout);
                reconnectTimeout = Math.min(reconnectTimeout * 2, MAX_RECONNECT_TIMEOUT);

            } else if (newState == BluetoothAdapter.STATE_CONNECTED) {
                reconnectTimeout = DEFAULT_RECONNECT_TIMEOUT;
            }
        }
    }

    private class ReconnectRunnable implements Runnable {
        public void run() {
            if (keepAlive && connection != null) {
                NeatleLogger.d("Reconnecting");
                connection.connect();
            }
        }
    }
}
