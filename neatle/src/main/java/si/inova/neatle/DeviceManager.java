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

import java.util.HashMap;

/**
 * Created by tomazs on 9/20/2016.
 */
class DeviceManager {
    private final Context context;
    private HashMap<String, Device> devices = new HashMap<>();

    private static DeviceManager sharedInstance;

    DeviceManager(Context context) {
        this.context = context;
    }

    public synchronized static DeviceManager getInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new DeviceManager(context.getApplicationContext());
        }
        return sharedInstance;
    }

    public synchronized Device getDevice(BluetoothDevice device) {
        Device dev = devices.get(device.getAddress());
        if (dev == null) {
            dev = new Device(context, device);
            devices.put(device.getAddress(), dev);
        }
        return dev;
    }
}
