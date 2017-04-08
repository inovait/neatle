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

import org.junit.Test;

import java.io.IOException;

import si.inova.neatle.source.ByteArrayInputSource;
import si.inova.neatle.source.StringInputSource;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertArrayEquals;

public class SourceTest {

    @Test
    public void stringInputSourceTest() throws IOException {
        StringInputSource stringInputSource;

        // Empty test
        stringInputSource = new StringInputSource("");
        stringInputSource.open();
        assertNull(stringInputSource.nextChunk());
        stringInputSource.close();

        // Null test
        stringInputSource = new StringInputSource(null);
        stringInputSource.open();
        assertNull(stringInputSource.nextChunk());
        stringInputSource.close();

        // Value test
        stringInputSource = new StringInputSource("Lorem ipsum dolor sit amet, consectetur adipiscing elit.");
        stringInputSource.open();
        assertArrayEquals("Lorem ipsum dolor si".getBytes("UTF8"), stringInputSource.nextChunk());
        assertArrayEquals("t amet, consectetur ".getBytes("UTF8"), stringInputSource.nextChunk());
        assertArrayEquals("adipiscing elit.".getBytes("UTF8"), stringInputSource.nextChunk());
        assertNull(stringInputSource.nextChunk());
        stringInputSource.close();
        assertNull(stringInputSource.nextChunk());
    }

    @Test
    public void byteArrayInputSourceTest() throws IOException {
        ByteArrayInputSource byteArrayInputSource;

        // Empty test
        byteArrayInputSource = new ByteArrayInputSource(new byte[]{});
        byteArrayInputSource.open();
        assertNull(byteArrayInputSource.nextChunk());
        byteArrayInputSource.close();

        // Null test
        byteArrayInputSource = new ByteArrayInputSource(null);
        byteArrayInputSource.open();
        assertNull(byteArrayInputSource.nextChunk());
        byteArrayInputSource.close();

        // Value test
        byteArrayInputSource = new ByteArrayInputSource("Lorem ipsum dolor sit amet, consectetur adipiscing elit.".getBytes("UTF8"));
        byteArrayInputSource.open();
        assertArrayEquals("Lorem ipsum dolor si".getBytes("UTF8"), byteArrayInputSource.nextChunk());
        assertArrayEquals("t amet, consectetur ".getBytes("UTF8"), byteArrayInputSource.nextChunk());
        assertArrayEquals("adipiscing elit.".getBytes("UTF8"), byteArrayInputSource.nextChunk());
        assertNull(byteArrayInputSource.nextChunk());
        byteArrayInputSource.close();
        assertNull(byteArrayInputSource.nextChunk());
    }
}
