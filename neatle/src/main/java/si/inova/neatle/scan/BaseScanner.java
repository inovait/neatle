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

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import si.inova.neatle.util.NeatleLogger;

abstract class BaseScanner implements Scanner {
    private final Handler handler;


    private Runnable pauseCallback = new PauseCallback();
    private Runnable resumeCallback = new ResumeCallback();

    private boolean scanning = false;
    private boolean doStarted = false;

    private long scanDuration;
    private long scanInterval;
    private int scanMode;

    private BroadcastReceiver broadcastReceiver = new BluetoothStateReceiver();
    private Context context;


    public BaseScanner() {
        handler = new Handler();
        //get default values
        ScanMode defaults = new ScanMode();
        scanDuration = defaults.getDuration();
        scanInterval = defaults.getInterval();
        scanMode = defaults.getMode();
    }

    @Override
    public void setMode(ScanMode mode) {
        if (mode.getDuration() == scanDuration
                && mode.getInterval() == scanInterval
                && mode.getMode() == scanMode) {
            return;
        }
        boolean wasScanning = scanning;
        Context oldContext = this.context;
        stopScanning();


        scanMode = mode.getMode();
        scanInterval = mode.getInterval();
        scanDuration = mode.getDuration();

        if (wasScanning) {
            startScanning(oldContext);
        }
    }

    @Override
    public final void startScanning(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (scanning) {
            NeatleLogger.i("Already scanning, ignoring start scanning request");
            return;
        }
        this.context = context;
        registerStateReciever(context);
        scanning = true;

        conditionalStart();
    }

    private void conditionalStart() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            NeatleLogger.e("Bluetooth LE scan failed to start. No bluetooth adapter found");
            return;
        }
        int adapterState = adapter.getState();
        if (adapterState != BluetoothAdapter.STATE_ON) {
            NeatleLogger.e("Bluetooth off, will start scanning when it turns on.");
            pause();
            return;
        }

        onStart(adapter, scanMode);
        doStarted = true;
        if (scanDuration > 0) {
            handler.postDelayed(pauseCallback, scanDuration);
        }
    }

    protected void resume() {
        handler.removeCallbacks(pauseCallback);
        handler.removeCallbacks(resumeCallback);
        if (!scanning) {
            NeatleLogger.e("resume called but scanning is stopped. ");
            return;
        }
        conditionalStart();
    }

    private void pause() {
        handler.removeCallbacks(pauseCallback);
        handler.removeCallbacks(resumeCallback);

        if (!scanning) {
            NeatleLogger.i("called pause, but there is no scanning in progress");
            return;
        }

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (doStarted) {
            doStarted = false;
            onStop(adapter);
        }


        if (scanInterval > 0 && adapter.getState() == BluetoothAdapter.STATE_ON) {
            NeatleLogger.i("scanning paused, will resume in " + scanInterval + " milliseconds");
            handler.postDelayed(resumeCallback, scanInterval);
        } else {
            NeatleLogger.i("no scan interval set or bluetooth off, stopping scanning");
        }
    }



    private void registerStateReciever(Context context) {
        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    private void unregisterStateReciever(Context context) {
        context.unregisterReceiver(broadcastReceiver);
    }

    protected abstract void onStart(BluetoothAdapter adapter, int scanMode);

    @Override
    public void stopScanning() {
        handler.removeCallbacks(pauseCallback);
        handler.removeCallbacks(resumeCallback);
        if (scanning) {
            unregisterStateReciever(context);
            if (doStarted) {
                doStarted = false;
                onStop(BluetoothAdapter.getDefaultAdapter());
            }

            context = null;
            scanning = false;
        }
    }

    protected abstract void onStop(BluetoothAdapter adapter);


    private class ResumeCallback implements Runnable {

        @Override
        public void run() {
            resume();
        }
    }

    private class PauseCallback implements Runnable {
        @Override
        public void run() {
            pause();
        }
    }

    private class BluetoothStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                return;
            }
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            int state = adapter.getState();
            if (state == BluetoothAdapter.STATE_ON) {
                NeatleLogger.i("BluetoothAdapter turned on");
                resume();
            } else {
                NeatleLogger.i("BluetoothAdapter state changed to " + state  +", turning off");
                pause();
            }
        }
    }
}
