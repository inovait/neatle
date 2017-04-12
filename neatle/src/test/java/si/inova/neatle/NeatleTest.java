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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19)
public class NeatleTest {

    @Test
    public void testCreateUUID() {
        assertEquals("0000180f-0000-1000-8000-00805f9b34fb", Neatle.createUUID(0x180f).toString());
        assertEquals("00000000-0000-1000-8000-00805f9b34fb", Neatle.createUUID(0).toString());
        assertEquals("0000ffff-0000-1000-8000-00805f9b34fb", Neatle.createUUID(-1).toString());
    }

    @Test
    public void testCreateSubscription() {
        BluetoothDevice bluetoothDevice = Mockito.mock(BluetoothDevice.class);
        Mockito.when(bluetoothDevice.getAddress()).thenReturn("00:11:22:33:44:55");

        assertNotNull(Neatle.createSubscription(RuntimeEnvironment.application, bluetoothDevice, Neatle.createUUID(0), Neatle.createUUID(0)));
    }

    @Test
    public void testCreateConnectionMonitor() {
        BluetoothDevice bluetoothDevice = Mockito.mock(BluetoothDevice.class);
        Mockito.when(bluetoothDevice.getAddress()).thenReturn("00:11:22:33:44:55");

        assertNotNull(Neatle.createConnectionMonitor(RuntimeEnvironment.application, bluetoothDevice));
    }

    @Test
    public void testCreateOperationBuilder() {
        assertNotNull(Neatle.createOperationBuilder(RuntimeEnvironment.application));
    }

    @Test
    public void testGetConnection() {
        BluetoothDevice bluetoothDevice = Mockito.mock(BluetoothDevice.class);
        Mockito.when(bluetoothDevice.getAddress()).thenReturn("00:11:22:33:44:55");

        assertNotNull(Neatle.getConnection(RuntimeEnvironment.application, bluetoothDevice));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetDeviceException() {
        Neatle.getDevice("no:ta:ma:c!");
    }

    @Test
    public void testIsMacValid() {
        assertTrue(Neatle.isMacValid("00:11:22:33:44:55"));
        assertTrue(Neatle.isMacValid("00:11:22:33:44:55"));
        assertTrue(Neatle.isMacValid("aa:bb:cc:dd:ee:ff"));
        assertTrue(Neatle.isMacValid("00:22:bb:cd:ef:12"));
        assertFalse(Neatle.isMacValid(""));
        assertFalse(Neatle.isMacValid("loremipsup"));
        assertFalse(Neatle.isMacValid("00:22:bb:cd:ef:gg"));
        assertFalse(Neatle.isMacValid(":::::"));
    }
}
