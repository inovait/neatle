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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The combined results of an operation.
 */
public class OperationResults {

    private final Map<UUID, CommandResult> results = new HashMap<>();
    private boolean successful = false;

    OperationResults() {
    }

    /**
     * Indicates whether or not all of the commands in the operation were successful.
     *
     * @return true if all commands were successful, false otherwise
     */
    public synchronized boolean wasSuccessful() {
        return successful;
    }

    /**
     * Gets the result of a command that was executed on a specific UUAD.
     *
     * @param uuid the UUAD of the characteristic
     * @return the command result
     */
    public synchronized CommandResult getResult(UUID uuid) {
        return results.get(uuid);
    }

    synchronized void addResult(CommandResult result) {
        if (results.isEmpty()) {
            successful = result.wasSuccessful();
        } else {
            successful = successful && result.wasSuccessful();
        }
        results.put(result.getUUID(), result);
    }
}
