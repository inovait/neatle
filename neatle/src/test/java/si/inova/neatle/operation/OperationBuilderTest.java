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
import android.os.Build;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.LinkedList;

import si.inova.neatle.BuildConfig;
import si.inova.neatle.Neatle;
import si.inova.neatle.source.InputSource;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.N_MR1)
public class OperationBuilderTest {

    @Mock
    InputSource inputSource;
    @Mock
    CommandObserver commandObserver;
    @Mock
    BluetoothDevice bluetoothDevice;
    @Mock
    OperationObserver operationObserver;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testBuilder() {
        OperationBuilder builder = new OperationBuilder(RuntimeEnvironment.application)
                .write(Neatle.createUUID(0), Neatle.createUUID(1), inputSource)
                .write(Neatle.createUUID(2), Neatle.createUUID(3), inputSource, commandObserver)
                .writeNoResponse(Neatle.createUUID(4), Neatle.createUUID(5), inputSource)
                .writeNoResponse(Neatle.createUUID(6), Neatle.createUUID(7), inputSource, commandObserver)
                .read(Neatle.createUUID(8), Neatle.createUUID(9))
                .read(Neatle.createUUID(10), Neatle.createUUID(11), commandObserver)
                .subscribeNotification(Neatle.createUUID(12), Neatle.createUUID(13), commandObserver)
                .unsubscribeNotification(Neatle.createUUID(14), Neatle.createUUID(15), commandObserver)
                .retryCount(16)
                .onFinished(operationObserver);

        OperationImpl operation = (OperationImpl) builder.build(bluetoothDevice);

        assertEquals(16, operation.getRetryCount());
        assertEquals(operationObserver, operation.getOperationObserver());
        assertEquals(bluetoothDevice, operation.getDevice());

        LinkedList<Command> commands = operation.getCommands();
        assertEquals(8, commands.size());

        assertWriteCommand(commands.get(0), 0, 1);
        assertWriteCommand(commands.get(1), 2, 3);
        assertWriteCommand(commands.get(2), 4, 5);
        assertWriteCommand(commands.get(3), 6, 7);

        assertReadCommand(commands.get(4), 8, 9);
        assertReadCommand(commands.get(5), 10, 11);

        assertSubscribeCommand(commands.get(6), 12, 13);
        assertSubscribeCommand(commands.get(7), 14, 15);
    }

    @SuppressWarnings("CheckResult")
    @Test(expected = IllegalArgumentException.class)
    public void testBuilderException() {
        new OperationBuilder(RuntimeEnvironment.application).build(null);
    }

    private void assertSubscribeCommand(Command command, int serUuid, int chUuid) {
        assertTrue(command instanceof SubscribeCommand);
        assertUuids(command, serUuid, chUuid);
    }

    private void assertReadCommand(Command command, int serUuid, int chUuid) {
        assertTrue(command instanceof ReadCommand);
        assertUuids(command, serUuid, chUuid);
    }

    private void assertWriteCommand(Command command, int serUuid, int chUuid) {
        assertTrue(command instanceof WriteCommand);
        assertUuids(command, serUuid, chUuid);
    }

    private void assertUuids(Command command, int serUuid, int chUuid) {
        SingleCharacteristicsCommand cmd = (SingleCharacteristicsCommand) command;
        assertEquals(Neatle.createUUID(serUuid), cmd.getServiceUUID());
        assertEquals(Neatle.createUUID(chUuid), cmd.getCharacteristicUUID());
    }
}
