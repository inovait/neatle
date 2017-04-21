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

package si.inova.neatle.rx;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.HashMap;

import io.reactivex.Observable;
import si.inova.neatle.operation.CommandResult;

public class NeatleRx {

    private static HashMap<String, Observable<MonitorResult>> connectionMonitors = new HashMap<>();

    public static Observable<MonitorResult> getConnectionMonitor(@NonNull Context context, @NonNull BluetoothDevice device) {
        // TODO: Add implementation
        return null;
    }

    public static Observable<CommandResult> getSubscription(@NonNull Context context, @NonNull BluetoothDevice device) {
        // TODO: Add implementation
        return null;
    }

    public static RxOperationBuilder createOperationBuilder(@NonNull Context context) {
        return new RxOperationBuilder(context);
    }
}
