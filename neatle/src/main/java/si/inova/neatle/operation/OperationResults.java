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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OperationResults {
    private final BluetoothDevice device;
    private Map<UUID, CommandResult> results = new HashMap<>();
    private boolean success = false;

    OperationResults(BluetoothDevice device) {
        this.device = device;
    }

    public boolean wasSuccessful() {
        return success;
    }

    public CommandResult getResult(UUID uuid) {
        return results.get(uuid);
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    synchronized void addCommandResult(CommandResult result) {
        //first result sets the result flag
        if (results.isEmpty()) {
            success = result.wasSuccessful();
        } else {
            success = success && result.wasSuccessful();
        }
        results.put(result.getUUID(), result);
    }

    public synchronized String getString(UUID uuid) {
        CommandResult res = getResult(uuid);
        if (res != null) {
            return res.getValueAsString();
        }
        return null;
    }
}
