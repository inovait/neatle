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
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;

import java.util.List;
import java.util.UUID;

import si.inova.neatle.Neatle;
import si.inova.neatle.ServicesDiscoveredListener;
import si.inova.neatle.operation.CharacteristicsChangedListener;

public interface Connection {

    /**
     * Is this connection established.
     *
     * @return true when the connection is established, false otherwise.
     */
    boolean isConnected();

    /**
     * Is this connection being established.
     *
     * @return true when connecting false otherwise.
     */
    boolean isConnecting();


    /**
     * Determinates if gatt services haven been discovered.
     *
     * @return true when connected and gatt services have been discovered.
     */
    boolean areServicesDiscovered();

    void addConnectionHandler(ConnectionHandler handler);

    void removeConnectionHandler(ConnectionHandler handler);

    /**
     * Disconnects and aborts any pending commends. Generally you don't want to
     * manually disconnect - just make sure all operations are canceled and any
     * persistent subscriptions are stopped.
     * <p>
     * Note: If there are any persistant subscriptions they try to re-establish the
     * connection after this disconnect.
     */
    void disconnect();

    /**
     * Try to establish this connection. If the connection is active or already connecting
     * this method does nothing.
     */
    void connect();

    /**
     * Add a change listener that will be notified when the devices notifies a characteristic change.
     * <p>
     * <b>Note:</b> This method by itself won't subscribe to notifcations/indications, to do
     * so use {@link Neatle#createSubscription(android.content.Context, BluetoothDevice, UUID, UUID)}
     *
     * @param characteristicsUUID the UUUID of the desired characteristics
     * @param listener            the listener to add
     */
    void addCharacteristicsChangedListener(UUID characteristicsUUID, CharacteristicsChangedListener listener);

    /**
     * Removes the change listener privously added by {@link #addCharacteristicsChangedListener}
     *
     * @param characteristicsUUID characteristics UUID
     * @param listener            the listener to remove
     */
    void removeCharacteristicsChangedListener(UUID characteristicsUUID, CharacteristicsChangedListener listener);

    /**
     * Gets the number of change listeners on the given characteristic.
     *
     * @param characteristicsUUID the UUID of the characteristic
     * @return the number of listeners or 0 is there are none
     */
    int getCharacteristicsChangedListenerCount(UUID characteristicsUUID);

    /**
     * Returns the state of this connection - {@link android.bluetooth.BluetoothAdapter#STATE_CONNECTED},
     * {@link android.bluetooth.BluetoothAdapter#STATE_CONNECTING},
     * {@link android.bluetooth.BluetoothAdapter#STATE_DISCONNECTED} or {@link android.bluetooth.BluetoothAdapter#STATE_OFF}
     *
     * @return the state of this connection.
     */
    int getState();

    /**
     * Adds a state listener for this connection
     *
     * @param connectionStateListener the state listener to add
     */
    void addConnectionStateListener(ConnectionStateListener connectionStateListener);

    /**
     * Removes a state listener from this connection
     *
     * @param connectionStateListener the state listener to remove
     */
    void removeConnectionStateListener(ConnectionStateListener connectionStateListener);

    /**
     * Returns the BluetoothDevice associated with this connection.
     *
     * @return the associated BluetoothDevice
     */
    BluetoothDevice getDevice();

    /**
     * Returns a {@link BluetoothGattService}, if the requested UUID is
     * supported by the remote device.
     *
     * @param serviceUUID UUID of the requested service
     * @return BluetoothGattService if supported, or null if the requested
     * service is not offered by the remote device.
     * @see BluetoothGatt#getService(UUID)
     */
    BluetoothGattService getService(UUID serviceUUID);

    /**
     * Returns a list of GATT services offered by the remote device.
     *
     * @return List of services on the remote device. Returns an empty list
     * if service discovery has not yet been performed.
     * @see BluetoothGatt#getServices()
     */
    List<BluetoothGattService> getServices();

    void addServicesDiscoveredListener(ServicesDiscoveredListener listener);

    void removeServicesDiscoveredListener(ServicesDiscoveredListener listener);

    /**
     * Set which transport will be used to connect to this device.
     * Defaults to {@link BluetoothDevice#TRANSPORT_AUTO}.
     *
     * Only works on API Level 23 and higher.
     *
     * @param transport Either {@link BluetoothDevice#TRANSPORT_AUTO},
     * {@link BluetoothDevice#TRANSPORT_BREDR} or
     * {@link BluetoothDevice#TRANSPORT_LE}
     */
    public void setTransport(int transport);
}
