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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.RestrictTo;

import java.util.UUID;

import si.inova.neatle.Neatle;
import si.inova.neatle.monitor.Connection;
import si.inova.neatle.monitor.ConnectionStateListener;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class CharacteristicSubscriptionImpl implements CharacteristicSubscription {

    private final BluetoothDevice device;
    private final UUID characteristicsUUID;

    private final Operation subscribeOp;
    private final Operation unsubscribeOp;

    private CharacteristicsChangedListener listener;
    private Context context;

    private boolean started = false;

    private ConnectionStateListener connectionStateHandler = new ConnectionStateListener() {
        @Override
        public void onConnectionStateChanged(Connection connection, int newState) {
            if (newState == BluetoothAdapter.STATE_CONNECTED) {
                subscribeOnDevice();
            }
        }
    };

    private CharacteristicsChangedListener changeHandler = new CharacteristicsChangedListener() {
        @Override
        public void onCharacteristicChanged(CommandResult change) {
            if (listener != null) {
                listener.onCharacteristicChanged(change);
            }
        }
    };

    public CharacteristicSubscriptionImpl(Context context, BluetoothDevice device, UUID serviceUUID, UUID characteristicsUUID) {
        this.context = context.getApplicationContext();
        this.device = device;
        this.characteristicsUUID = characteristicsUUID;

        subscribeOp = new OperationBuilder(this.context).subscribeNotification(serviceUUID, characteristicsUUID, null).build(device);
        unsubscribeOp = new OperationBuilder(this.context).unsubscribeNotification(serviceUUID, characteristicsUUID, null).build(device);
    }

    @Override
    public void setOnCharacteristicsChangedListener(CharacteristicsChangedListener listener) {
        this.listener = listener;
    }

    @Override
    public void start() {
        if (started) {
            return;
        }

        unsubscribeOp.cancel();
        Connection connection = Neatle.getConnection(context, device);

        connection.addConnectionStateListener(connectionStateHandler);
        connection.addCharacteristicsChangedListener(characteristicsUUID, changeHandler);

        if (connection.isConnected()) {
            subscribeOnDevice();
        } else {
            connection.connect();
        }

        started = true;
    }

    @Override
    public void stop() {
        if (!started) {
            return;
        }

        subscribeOp.cancel();
        Connection connection = Neatle.getConnection(context, device);
        connection.removeConnectionStateListener(connectionStateHandler);
        connection.removeCharacteristicsChangedListener(characteristicsUUID, changeHandler);

        if (connection.isConnected()) {
            unsubscribeOnDevice();
        }

        started = false;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    private void subscribeOnDevice() {
        unsubscribeOp.cancel();
        subscribeOp.execute();
    }

    private void unsubscribeOnDevice() {
        subscribeOp.cancel();
        unsubscribeOp.execute();
    }
}
