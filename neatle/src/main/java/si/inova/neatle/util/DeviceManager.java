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

package si.inova.neatle.util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.support.annotation.RestrictTo;

import java.util.HashMap;

import si.inova.neatle.Device;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class DeviceManager {

    private final Context context;
    private final HashMap<String, Device> devices = new HashMap<>();

    // Using application context, so no chance for leak.
    @SuppressLint("StaticFieldLeak")
    private static DeviceManager sharedInstance;

    public synchronized static DeviceManager getInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new DeviceManager(context.getApplicationContext());
        }
        return sharedInstance;
    }

    private DeviceManager(Context context) {
        this.context = context;
    }

    public synchronized Device getDevice(BluetoothDevice device) {
        NeatleLogger.d("Getting connection object for " + device.getAddress());
        Device dev = devices.get(device.getAddress());
        if (dev == null) {
            BluetoothManager mng = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            dev = new Device(context, device, mng.getAdapter());
            devices.put(device.getAddress(), dev);
        }
        return dev;
    }
}
