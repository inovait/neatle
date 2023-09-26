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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import androidx.annotation.RestrictTo;

import si.inova.neatle.Neatle;
import si.inova.neatle.ServicesDiscoveredListener;
import si.inova.neatle.util.NeatleLogger;

/**
 * Monitors the connection with a bluetooth device.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class ConnectionMonitorImpl implements ConnectionMonitor {

    private static final long DEFAULT_RECONNECT_TIMEOUT = 2500L;
    private static final long MAX_RECONNECT_TIMEOUT = 60 * 1000L;

    private final Context context;
    private final BluetoothDevice device;

    private Connection connection;
    private ConnectionStateListener connectionStateListener;
    private ServicesDiscoveredListener serviceDiscoveredListener;
    private boolean keepAlive;

    private final Handler handler = new Handler();
    private final ConnHandler connectionHandler = new ConnHandler();
    private final ReconnectRunnable reconnectRunnable = new ReconnectRunnable();

    private int transport = 0;

    private final IntentFilter btFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);

                if (state == BluetoothAdapter.STATE_ON) {
                    onBluetoothTurnedOn();
                }
            }
        }
    };


    public ConnectionMonitorImpl(Context context, BluetoothDevice device) {
        this.context = context.getApplicationContext();
        this.device = device;
    }

    @Override
    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    @Override
    public void setOnConnectionStateListener(ConnectionStateListener connectionStateListener) {
        this.connectionStateListener = connectionStateListener;
    }

    @Override
    public void setOnServiceDiscoveredListener(ServicesDiscoveredListener serviceDiscoveredListener) {
        this.serviceDiscoveredListener = serviceDiscoveredListener;
    }

    @Override
    public void start() {
        if (connection != null) {
            return;
        }

        context.registerReceiver(btReceiver, btFilter);

        connection = Neatle.getConnection(context, device);
        connection.addConnectionHandler(connectionHandler);
        connection.addConnectionStateListener(connectionHandler);
        connection.addServicesDiscoveredListener(connectionHandler);
        connection.setTransport(transport);

        if (keepAlive) {
            connection.connect();
        }
    }

    @Override
    public void stop() {
        if (connection == null) {
            return;
        }

        context.unregisterReceiver(btReceiver);
        handler.removeCallbacks(reconnectRunnable);
        if (connection != null) {
            connection.removeConnectionStateListener(connectionHandler);
            connection.removeConnectionHandler(connectionHandler);
            connection.removeServicesDiscoveredListener(connectionHandler);
            connection = null;
        }
    }

    @Override
    public BluetoothDevice getDevice() {
        return device;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void setTransport(int transport) {
        this.transport = transport;

        if (this.connection != null) {
            this.connection.setTransport(transport);
        }
    }

    private void onBluetoothTurnedOn() {
        if (keepAlive && connection != null) {
            connection.connect();
        }
    }

    private class ConnHandler implements ConnectionHandler, ConnectionStateListener, ServicesDiscoveredListener {

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
                NeatleLogger.d("Will try to reconnect to " + connection.getDevice().getAddress() + " after " + (reconnectTimeout / 1000) + " seconds");

                handler.removeCallbacks(reconnectRunnable);
                handler.postDelayed(reconnectRunnable, reconnectTimeout);
                reconnectTimeout = Math.min(reconnectTimeout * 2, MAX_RECONNECT_TIMEOUT);
            } else if (newState == BluetoothAdapter.STATE_CONNECTED) {
                reconnectTimeout = DEFAULT_RECONNECT_TIMEOUT;
            }
        }

        @Override
        public void onServicesDiscovered(Connection connection) {
            if (serviceDiscoveredListener != null) {
                serviceDiscoveredListener.onServicesDiscovered(connection);
            }
        }
    }

    private class ReconnectRunnable implements Runnable {
        public void run() {
            if (keepAlive && connection != null) {
                NeatleLogger.d("Reconnecting to " + connection.getDevice().getAddress());
                connection.connect();
            }
        }
    }
}
