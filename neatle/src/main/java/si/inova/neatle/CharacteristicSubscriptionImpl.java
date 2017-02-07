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
import android.content.Context;
import android.os.Handler;

import java.util.UUID;

/**
 * Created by tomazs on 10/4/2016.
 */
class CharacteristicSubscriptionImpl implements CharacteristicSubscription {

    private static final long RECONNECT_TIMEOUT = 5000;

    private final BluetoothDevice device;
    private final UUID serviceUUID;
    private final UUID characteristicsUUID;

    private final Operation subscribeOp;
    private final Operation unsubscribeOp;
    private final Handler handler;

    private CharacteristicsChangedListener listener;
    private boolean persistent = true;
    private Context context;

    private ConnectionStateListener connectionStateHandler = new ConnectionStateListener() {
        @Override
        public void onConnectionStateChanged(Connection connection, int newState) {
            if (newState == BluetoothAdapter.STATE_CONNECTED) {
                subscribeOnDevice(connection);
            }
        }
    };

    private CharacteristicsChangedListener changeHandler = new ChangeHandler();

    CharacteristicSubscriptionImpl(BluetoothDevice device, UUID serviceUUID, UUID characteristicsUUID) {
        if (device == null) {
            throw new IllegalArgumentException("Device cannot be null");
        }
        this.device = device;
        this.serviceUUID = serviceUUID;
        this.characteristicsUUID = characteristicsUUID;

        OperationBuilder builderSub = new OperationBuilder();
        builderSub.subscribeNotification(serviceUUID, characteristicsUUID, null);

        OperationBuilder builderUnSub = new OperationBuilder();
        builderUnSub.unsubscribeNotification(serviceUUID, characteristicsUUID, null);

        subscribeOp = builderSub.build(device);
        unsubscribeOp = builderUnSub.build(device);

        this.handler = new Handler();
    }

    @Override
    public void setOnCharacteristicsChangedListener(CharacteristicsChangedListener listener) {
        this.listener = listener;
    }


    @Override
    public void start(Context context) {
        if (this.context != null) {
            //already started
            return;
        }
        this.context = context.getApplicationContext();
        unsubscribeOp.cancel();
        Connection connection = Neatle.getConnection(context, device);

        connection.addConnectionStateListener(connectionStateHandler);
        connection.addCharacteristicsChangedListener(characteristicsUUID, changeHandler);

        if (connection.isConnected()) {
            subscribeOnDevice(connection);
        } else {
            connection.connect();
        }
    }

    @Override
    public void stop() {
        if (context == null) {
            return;
        }
        subscribeOp.cancel();
        Connection connection = Neatle.getConnection(context, device);
        connection.removeConnectionStateListener(connectionStateHandler);
        connection.removeCharacteristicsChangedListener(characteristicsUUID, changeHandler);

        if (connection.getState() == BluetoothAdapter.STATE_CONNECTED) {
            unsubscribeOnDevice(connection);
        }

        this.context = null;
    }

    private void subscribeOnDevice(Connection connection) {
        unsubscribeOp.cancel();
        subscribeOp.execute(context);
    }

    private void unsubscribeOnDevice(Connection connection) {
        subscribeOp.cancel();
        unsubscribeOp.execute(context);
    }


    private class ChangeHandler implements CharacteristicsChangedListener {
        @Override
        public void onCharacteristicChanged(CommandResult change) {
            if (listener != null) {
                listener.onCharacteristicChanged(change);
            }
        }
    }
}
