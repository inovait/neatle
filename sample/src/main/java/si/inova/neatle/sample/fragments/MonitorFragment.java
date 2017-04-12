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

package si.inova.neatle.sample.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import si.inova.neatle.Neatle;
import si.inova.neatle.monitor.Connection;
import si.inova.neatle.monitor.ConnectionMonitor;
import si.inova.neatle.monitor.ConnectionStateListener;
import si.inova.neatle.sample.R;

/**
 * A fragment displaying the monitor functionality of NeatLE.
 */
public class MonitorFragment extends Fragment implements ConnectionStateListener {

    @BindView(R.id.monitor_mac_input)
    EditText macInput;
    @BindView(R.id.monitor_connection_state_label)
    TextView connectionStateLabel;

    private Unbinder unbinder;

    private ConnectionMonitor connectionMonitor;

    public MonitorFragment() {
    }

    public static MonitorFragment newInstance() {
        return new MonitorFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_monitor, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.monitor_connect_button)
    public void onConnectClicked() {
        String macAddress = macInput.getText().toString();

        if (!Neatle.isMacValid(macAddress)) {
            Toast.makeText(getContext(), "MAC address not recognized", Toast.LENGTH_LONG).show();
            return;
        }

        if (connectionMonitor != null) {
            connectionMonitor.stop();
            connectionMonitor = null;
        }

        connectionMonitor = Neatle.createConnectionMonitor(getContext(), Neatle.getDevice(macAddress));
        connectionMonitor.setKeepAlive(true);
        connectionMonitor.setOnConnectionStateListener(this);
        connectionMonitor.start();
    }

    @Override
    public void onConnectionStateChanged(Connection connection, int newState) {
        if (connection.isConnected()) {
            connectionStateLabel.setText(R.string.state_connected);
        } else if (connection.isConnecting()) {
            connectionStateLabel.setText(R.string.state_connecting);
        } else {
            connectionStateLabel.setText(R.string.state_not_connected);
        }
    }
}
