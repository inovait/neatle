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

import java.io.IOException;

/**
 * An input source that provides a byte array.
 */
public class ByteArrayInputSource implements InputSource {

    private final byte[] data;

    protected byte[] buffer;
    protected int offset;

    public ByteArrayInputSource(byte[] data) {
        this.data = data;
    }

    @Override
    public void close() throws IOException {
        buffer = null;
        offset = 0;
    }

    @Override
    public void open() throws IOException {
        buffer = data;
        offset = 0;
    }

    @Override
    public byte[] nextChunk() throws IOException {
        if (buffer == null) {
            return null;
        }

        int remaining = buffer.length - offset;
        if (remaining <= 0) {
            return null;
        }

        int chunkSize = Math.min(20, remaining);

        byte[] ret = new byte[chunkSize];
        System.arraycopy(buffer, offset, ret, 0, chunkSize);
        offset += chunkSize;

        return ret;
    }
}
