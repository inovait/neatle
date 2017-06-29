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
package si.inova.neatle.source;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class CalllableInputSourceTest {
    @Test
    public void testChunked() throws IOException {
        byte[] data = "01234567890123456789-second".getBytes();

        SimpleCallable callable = new SimpleCallable(ByteBuffer.wrap(data));
        CalllableInputSource source = new CalllableInputSource(callable);

        source.open();
        assertEquals(0, callable.calledTimes);

        byte[] firstChunk = source.nextChunk();
        assertEquals(1, callable.calledTimes);
        assertEquals("01234567890123456789", new String(firstChunk));

        byte[] secondChunk = source.nextChunk();
        assertEquals(1, callable.calledTimes);
        assertEquals("-second", new String(secondChunk));

        byte[] thirdChunk = source.nextChunk();
        assertEquals(1, callable.calledTimes);
        assertNull(thirdChunk);
    }

    private static class SimpleCallable implements Callable<ByteBuffer> {
        private int calledTimes = 0;
        private ByteBuffer bb;

        public SimpleCallable(ByteBuffer bb) {
            this.bb = bb;
        }

        @Override
        public ByteBuffer call() throws Exception {
            calledTimes++;
            return bb;
        }
    }
}
