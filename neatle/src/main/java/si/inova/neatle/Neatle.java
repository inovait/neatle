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
import android.content.Context;

import java.util.UUID;

/**
 * The starting point of the NeatLE library.
 */
public class Neatle {

    public static Connection getConnection(Context context, BluetoothDevice device) {
        Device actualDevice = DeviceManager.getInstance(context).getDevice(device);
        return actualDevice;
    }

    /**
     * Creates a subscription for listening to characteristics changes. To listen for changes
     * the subscription needs to be started.
     *
     * @param device              the device on which the subscribe
     * @param serviceUUID         the service UUID under which the characteristic is located.
     * @param characteristicsUUID the UUID of the characteristic on which the subsbscription will be
     *                            made
     * @return an un-started subscription.
     */
    public static CharacteristicSubscription createSubscription(BluetoothDevice device,
                                                                UUID serviceUUID, UUID characteristicsUUID) {
        return new CharacteristicSubscriptionImpl(device, serviceUUID, characteristicsUUID);
    }


    public static ConnectionMonitor createConnectionMonitor(Context context, BluetoothDevice device) {
        return new ConnectionMonitor(device);
    }

    public static OperationBuilder create() {
        return new OperationBuilder();
    }

    public static UUID createUUID(int uuid) {
        final String base = "-0000-1000-8000-00805F9B34FB";
        return UUID.fromString(String.format("%08X", uuid & 0xFFFF) + base);
    }
}
