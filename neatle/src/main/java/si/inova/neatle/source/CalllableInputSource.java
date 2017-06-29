package si.inova.neatle.source;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

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

/**
 * A input source that invokes {@link Callable} just before the first chunk is requested.
 */
public class CalllableInputSource implements InputSource {

    private final Callable<ByteBuffer> source;

    private ByteBuffer byteBuffer;

    public CalllableInputSource(Callable<ByteBuffer> source) {
        this.source = source;
    }

    @Override
    public void open() throws IOException {
        //nothing to do
    }

    @Override
    public byte[] nextChunk() throws IOException {
        if (byteBuffer == null) {
            try {
                byteBuffer = source.call();
            } catch (Exception e) {
                throw new IOException("Failed to get bytes from callback");
            }
        }
        if (byteBuffer == null || !byteBuffer.hasRemaining()) {
            return null;
        }

        int remaining = Math.min(byteBuffer.remaining(), 20);
        byte[] chunk = new byte[remaining];

        byteBuffer.get(chunk);
        return chunk;


    }

    @Override
    public void close() throws IOException {
        byteBuffer = null;
    }
}
