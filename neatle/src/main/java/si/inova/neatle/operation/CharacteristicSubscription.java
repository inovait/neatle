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

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import java.util.UUID;

import si.inova.neatle.Neatle;

/**
 * A subscription for a GATT notifications/indications.
 *
 * @see Neatle#createSubscription(Context, BluetoothDevice, UUID, UUID)
 */
public interface CharacteristicSubscription {

    /**
     * Sets the listener that will be called on characteristic changes.
     *
     * @param characteristicsChangedListener the listener
     */
    void setOnCharacteristicsChangedListener(CharacteristicsChangedListener characteristicsChangedListener);

    /**
     * Starts listening for characteristics changes. If there is no active connection and
     * the subscription is persistent than this is keep trying to connect to the device until
     * stop is called.
     * <p>
     * It's save to call start multitple times.
     */
    void start();

    /**
     * Stops listening for characteristics changes. If there is no other subscription for this
     * characteristic a "unsubscribe" command will be sent to the device, but only if there
     * is an active connection.
     */
    void stop();
}
