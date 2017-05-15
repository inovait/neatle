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
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.nio.charset.Charset;

import si.inova.neatle.BuildConfig;
import si.inova.neatle.Neatle;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.N_MR1)
public class CommandResultTest {

    private BluetoothGattCharacteristic characteristic;

    @Before
    public void setUp() throws Exception {
        characteristic = mock(BluetoothGattCharacteristic.class);
    }

    @Test
    public void testGeneral() {
        CommandResult commandResult = new CommandResult(Neatle.createUUID(1), new byte[]{22, 21}, BluetoothGatt.GATT_SUCCESS, 1234556, characteristic);

        assertEquals(BluetoothGatt.GATT_SUCCESS, commandResult.getStatus());
        assertEquals(1234556, commandResult.getTimestamp());
        assertEquals(Neatle.createUUID(1), commandResult.getUUID());
        assertTrue(commandResult.wasSuccessful());
        assertNotNull(commandResult.toString());

        CommandResult commandResult2 = new CommandResult(Neatle.createUUID(1), new byte[]{22, 21}, BluetoothGatt.GATT_FAILURE, 1234556, characteristic);
        assertFalse(commandResult2.wasSuccessful());
    }

    @Test
    public void testIntValues() {
        CommandResult commandResult = new CommandResult(Neatle.createUUID(1), new byte[]{22, 21}, BluetoothGatt.GATT_SUCCESS, 1, characteristic);
        assertArrayEquals(new byte[]{22, 21}, commandResult.getValue());

        CommandResult commandResult1 = new CommandResult(Neatle.createUUID(1), new byte[]{22}, BluetoothGatt.GATT_SUCCESS, 1, characteristic);
        CommandResult commandResult2 = new CommandResult(Neatle.createUUID(1), new byte[]{22, 21}, BluetoothGatt.GATT_SUCCESS, 1, characteristic);
        CommandResult commandResult3 = new CommandResult(Neatle.createUUID(1), new byte[]{22, 21, 20}, BluetoothGatt.GATT_SUCCESS, 1, characteristic);
        CommandResult commandResult4 = new CommandResult(Neatle.createUUID(1), new byte[]{22, 21, 20, 19}, BluetoothGatt.GATT_SUCCESS, 1, characteristic);

        assertEquals(22, commandResult1.getValueAsInt());
        assertEquals(5653, commandResult2.getValueAsInt());
        assertEquals(1447188, commandResult3.getValueAsInt());
        assertEquals(370480147, commandResult4.getValueAsInt());
    }

    @Test(expected = IllegalStateException.class)
    public void testIntException() {
        CommandResult commandResult = new CommandResult(Neatle.createUUID(1), new byte[]{22, 21, 20, 19, 18}, BluetoothGatt.GATT_SUCCESS, 1, characteristic);
        commandResult.getValueAsInt();
    }

    @Test
    public void testStringValues() {
        String dataStr = "lorem ipsum dolor sit amet";
        CommandResult commandResult = new CommandResult(Neatle.createUUID(1), dataStr.getBytes(Charset.forName("UTF8")), BluetoothGatt.GATT_SUCCESS, 1, characteristic);
        assertEquals(dataStr, commandResult.getValueAsString());

        CommandResult commandResult2 = new CommandResult(Neatle.createUUID(1), null, BluetoothGatt.GATT_SUCCESS, 1, characteristic);
        assertNull(commandResult2.getValueAsString());

        CommandResult commandResult3 = new CommandResult(Neatle.createUUID(1), new byte[]{}, BluetoothGatt.GATT_SUCCESS, 1, characteristic);
        assertEquals("", commandResult3.getValueAsString());
    }

    @Test
    public void delegatesGetPropertiesToCharacteristic() throws Exception {
        CommandResult commandResult = new CommandResult(Neatle.createUUID(1), new byte[]{}, BluetoothGatt.GATT_SUCCESS, 1, characteristic);

        commandResult.getProperties();

        verify(characteristic).getProperties();
    }

    @Test
    public void delegatesGetIntValueToCharacteristic() throws Exception {
        CommandResult commandResult = new CommandResult(Neatle.createUUID(1), new byte[]{}, BluetoothGatt.GATT_SUCCESS, 1, characteristic);

        commandResult.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1);

        verify(characteristic).getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 1);
    }

    @Test
    public void delegatesGetFloatValueToCharacteristic() throws Exception {
        CommandResult commandResult = new CommandResult(Neatle.createUUID(1), new byte[]{}, BluetoothGatt.GATT_SUCCESS, 1, characteristic);

        commandResult.getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 2);

        verify(characteristic).getFloatValue(BluetoothGattCharacteristic.FORMAT_FLOAT, 2);
    }

    @Test
    public void delegatesGetStringValueToCharacteristic() throws Exception {
        CommandResult commandResult = new CommandResult(Neatle.createUUID(1), new byte[]{}, BluetoothGatt.GATT_SUCCESS, 1, characteristic);

        commandResult.getStringValue(3);

        verify(characteristic).getStringValue(3);
    }

    @Test
    public void testFactoryMethods() {
        BluetoothGattCharacteristic gattCharacteristic = mock(BluetoothGattCharacteristic.class);
        Mockito.when(gattCharacteristic.getValue()).thenReturn(new byte[]{20, 11, 22});
        Mockito.when(gattCharacteristic.getUuid()).thenReturn(Neatle.createUUID(1));

        CommandResult result = CommandResult.createCharacteristicRead(gattCharacteristic, BluetoothGatt.GATT_SUCCESS);
        assertArrayEquals(new byte[]{20, 11, 22}, result.getValue());
        assertEquals(Neatle.createUUID(1), result.getUUID());
        assertEquals(BluetoothGatt.GATT_SUCCESS, result.getStatus());

        CommandResult result2 = CommandResult.createCharacteristicChanged(gattCharacteristic);
        assertArrayEquals(new byte[]{20, 11, 22}, result2.getValue());
        assertEquals(Neatle.createUUID(1), result2.getUUID());
        assertEquals(BluetoothGatt.GATT_SUCCESS, result2.getStatus());

        CommandResult result3 = CommandResult.createErrorResult(Neatle.createUUID(2), BluetoothGatt.GATT_FAILURE);
        assertNull(result3.getValue());
        assertEquals(Neatle.createUUID(2), result3.getUUID());
        assertEquals(BluetoothGatt.GATT_FAILURE, result3.getStatus());

        CommandResult result4 = CommandResult.createEmptySuccess(Neatle.createUUID(3));
        assertNull(result4.getValue());
        assertEquals(Neatle.createUUID(3), result4.getUUID());
        assertTrue(result4.wasSuccessful());
    }

    @Test
    public void testFormattedIntValue() {
        BluetoothGattCharacteristic gattCharacteristic = Mockito.mock(BluetoothGattCharacteristic.class);
        Mockito.when(gattCharacteristic.getValue()).thenReturn(new byte[]{-1, 11, 22, 12, 44});
        Mockito.when(gattCharacteristic.getUuid()).thenReturn(Neatle.createUUID(1));

        CommandResult result = CommandResult.createCharacteristicRead(gattCharacteristic, BluetoothGatt.GATT_SUCCESS);
        assertEquals(255, (int) result.getFormattedIntValue(CommandResult.FORMAT_UINT8, 0));
        assertEquals(3071, (int) result.getFormattedIntValue(CommandResult.FORMAT_UINT16, 0));
        assertEquals(202771455, (int) result.getFormattedIntValue(CommandResult.FORMAT_UINT32, 0));
        assertEquals(-1, (int) result.getFormattedIntValue(CommandResult.FORMAT_SINT8, 0));
        assertEquals(3071, (int) result.getFormattedIntValue(CommandResult.FORMAT_SINT16, 0));
        assertEquals(202771455, (int) result.getFormattedIntValue(CommandResult.FORMAT_SINT32, 0));
    }
}
