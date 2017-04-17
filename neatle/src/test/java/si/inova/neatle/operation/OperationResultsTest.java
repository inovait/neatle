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

import android.bluetooth.BluetoothGatt;
import android.os.Build;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import si.inova.neatle.BuildConfig;
import si.inova.neatle.Neatle;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.N_MR1)
public class OperationResultsTest {

    private OperationResults operationResults;

    @Before
    public void setUp() {
        operationResults = new OperationResults();
    }

    @Test
    public void testAddGet() {
        CommandResult result1 = new CommandResult(Neatle.createUUID(1), null, BluetoothGatt.GATT_SUCCESS, 0);
        CommandResult result2 = new CommandResult(Neatle.createUUID(2), null, BluetoothGatt.GATT_SUCCESS, 0);
        CommandResult result3 = new CommandResult(Neatle.createUUID(3), null, BluetoothGatt.GATT_SUCCESS, 0);

        operationResults.addResult(result1);
        operationResults.addResult(result2);
        operationResults.addResult(result3);

        assertEquals(result1, operationResults.getResult(Neatle.createUUID(1)));
        assertEquals(result2, operationResults.getResult(Neatle.createUUID(2)));
        assertEquals(result3, operationResults.getResult(Neatle.createUUID(3)));
        assertNull(operationResults.getResult(Neatle.createUUID(4)));
    }

    @Test
    public void testSuccessful() {
        CommandResult result1 = new CommandResult(Neatle.createUUID(1), null, BluetoothGatt.GATT_SUCCESS, 0);
        CommandResult result2 = new CommandResult(Neatle.createUUID(2), null, BluetoothGatt.GATT_SUCCESS, 0);
        CommandResult result3 = new CommandResult(Neatle.createUUID(3), null, BluetoothGatt.GATT_FAILURE, 0);

        operationResults.addResult(result1);
        assertTrue(operationResults.wasSuccessful());
        operationResults.addResult(result2);
        assertTrue(operationResults.wasSuccessful());
        operationResults.addResult(result3);
        assertFalse(operationResults.wasSuccessful());
    }

}
