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
import android.support.annotation.NonNull;

import java.util.UUID;

import si.inova.neatle.monitor.Connection;
import si.inova.neatle.monitor.ConnectionMonitor;
import si.inova.neatle.monitor.ConnectionMonitorImpl;
import si.inova.neatle.operation.CharacteristicSubscription;
import si.inova.neatle.operation.CharacteristicSubscriptionImpl;
import si.inova.neatle.operation.OperationBuilder;
import si.inova.neatle.util.DeviceManager;

/**
 * The starting point of the NeatLE library.
 */
public class Neatle {

    /**
     * If you think you need an instance of this class, you're wrong.
     */
    private Neatle() {
    }

    /**
     * Creates a subscription for listening to characteristics changes. To listen for changes
     * the subscription needs to be started.
     *
     * @param context             the current context
     * @param device              the device on which the subscribe
     * @param serviceUUID         the service UUID under which the characteristic is located.
     * @param characteristicsUUID the UUID of the characteristic on which the subsbscription will be
     *                            made
     * @return an un-started subscription.
     */
    public static CharacteristicSubscription createSubscription(@NonNull Context context, @NonNull BluetoothDevice device,
                                                                @NonNull UUID serviceUUID, @NonNull UUID characteristicsUUID) {
        return new CharacteristicSubscriptionImpl(context, device, serviceUUID, characteristicsUUID);
    }

    /**
     * Creates a connection monitor that tries to connect to a bluetooth device, and notifies us of
     * changes to the connection. To listen for changes the monitor needs to be started.
     *
     * @param context the current context
     * @param device  the device to connect to
     * @return the connection monitor
     */
    public static ConnectionMonitor createConnectionMonitor(@NonNull Context context, @NonNull BluetoothDevice device) {
        return new ConnectionMonitorImpl(context, device);
    }

    /**
     * Creates a new operation builder, that can be used to build a system of sequential read / write
     * operations to a device.
     *
     * @param context the current context
     * @return the operation builder
     */
    public static OperationBuilder createOperationBuilder(@NonNull Context context) {
        return new OperationBuilder(context);
    }

    /**
     * Creates a standardized UUID by using the rightmost byte of the provided integer.
     *
     * @param uuid the id
     * @return the created UUID
     */
    public static UUID createUUID(int uuid) {
        final String base = "-0000-1000-8000-00805F9B34FB";
        return UUID.fromString(String.format("%08X", uuid & 0xFFFF) + base);
    }

    /**
     * Returns a connection to a device that has been added to the NeatLE library. The connection
     * may not not be active.
     *
     * @param context the current context
     * @param device  the device to search for
     * @return the connection, or null if none has been found.
     */
    public static Connection getConnection(@NonNull Context context, @NonNull BluetoothDevice device) {
        return DeviceManager.getInstance(context).getDevice(device);
    }

    /**
     * Validates a MAC address.
     *
     * @param mac the mac address to validate
     * @return true if it's a valid address, otherwise false
     */
    public static boolean isMacValid(String mac) {
        return BluetoothAdapter.checkBluetoothAddress(mac.toUpperCase());
    }

    /**
     * Returns a {@link BluetoothDevice} based on the provided MAC address.
     *
     * @param mac the MAC address of the BTLE device
     * @return the created BT device.
     */
    public static BluetoothDevice getDevice(@NonNull String mac) {
        mac = mac.toUpperCase();
        if (isMacValid(mac)) {
            return BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mac);
        }
        throw new UnsupportedOperationException("Device mac not recognized.");
    }
}
