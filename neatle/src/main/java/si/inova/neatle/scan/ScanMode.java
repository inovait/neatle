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
package si.inova.neatle.scan;

import android.bluetooth.le.ScanSettings;

/**
 * This class represents the scanning mode of a {@link Scanner} object.
 */
public final class ScanMode {
    /**
     * @see ScanSettings#SCAN_MODE_OPPORTUNISTIC
     */
    public static final int SCAN_MODE_OPPORTUNISTIC = -1;
    /**
     * @see ScanSettings#SCAN_MODE_LOW_POWER
     */
    public static final int SCAN_MODE_LOW_POWER = 0;
    /**
     * @see ScanSettings#SCAN_MODE_BALANCED
     */
    public static final int SCAN_MODE_BALANCED = 1;

    /**
     * @see ScanSettings#SCAN_MODE_LOW_POWER
     */
    public static final int SCAN_MODE_LOW_LATENCY = 2;

    private long interval = -1;
    private int mode = SCAN_MODE_LOW_POWER;
    private long duration = -1;

    /**
     * Sets the scan mode, by default it's set to SCAN_MODE_LOW_POWER.
     *
     * @param mode the scan mode
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * Gets the scan mode.
     *
     * @return @{link #SCAN_MODE_OPPORTUNISTIC}, {@link #SCAN_MODE_LOW_POWER},
     * {@link #SCAN_MODE_BALANCED} or  {@link #SCAN_MODE_LOW_LATENCY}
     */
    public int getMode() {
        return mode;
    }

    /**
     * Sets how long should the scanner scan until it's suspended. By default it will
     * scan indefinitely.
     *
     * @param duration the duration in milliseconds on -1 to scan indefinitely.
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * Gets the scan duration.
     *
     * @return the scan duration or a negative number when the scan duration is indefinitely.
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Sets the interval between subsequent scans. This is only used when the scan duration
     * is not set to indefinitely.
     *
     * For example if the duration is set 15 seconds and the interval to 10 seconds, then
     * it will actively scan for 15 seconds, than suspend itself for 10 seconds.
     *
     * @param interval the interval in milliseconds.
     */
    public void setInterval(long interval) {
        this.interval = interval;
    }


    /**
     * Gets the interval between subsequent scans.
     *
     * @return return the interval value or a negative value when the interval is not set.
     */
    public long getInterval() {
        return interval;
    }



}
