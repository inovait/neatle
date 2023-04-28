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
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import si.inova.neatle.Neatle;
import si.inova.neatle.operation.Operation;
import si.inova.neatle.operation.OperationResults;
import si.inova.neatle.operation.SimpleOperationObserver;
import si.inova.neatle.sample.R;

/**
 * A fragment displaying the operation functionality of NeatLE.
 */
public class OperationFragment extends Fragment {

    public static final UUID BATTERY_LEVEL_SERVICE = Neatle.createUUID(1);
    public static final UUID BATTERY_LEVEL = Neatle.createUUID(2);

    @BindView(R.id.operation_mac_input)
    EditText macInput;
    @BindView(R.id.operation_status_label)
    TextView statusLabel;

    private Unbinder unbinder;

    private Operation operation;

    public OperationFragment() {
    }

    public static OperationFragment newInstance() {
        return new OperationFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_operation, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (operation != null) {
            operation.cancel();
            operation = null;
        }
    }

    @OnClick(R.id.operation_read_button)
    public void onReadClicked() {
        if (operation != null) {
            return;
        }

        String macAddress = macInput.getText().toString();

        if (!Neatle.isMacValid(macAddress)) {
            Toast.makeText(getContext(), "MAC address not recognized", Toast.LENGTH_LONG).show();
            return;
        }

        operation = Neatle.createOperationBuilder(getContext())
                .read(BATTERY_LEVEL_SERVICE, BATTERY_LEVEL)
                .onFinished(new SimpleOperationObserver() {
                    @Override
                    public void onOperationFinished(Operation op, OperationResults results) {
                        if (results.wasSuccessful()) {
                            int batteryLevel = results.getResult(BATTERY_LEVEL).getValueAsInt();
                            statusLabel.setText(getString(R.string.battery_level_status, batteryLevel));
                        } else {
                            statusLabel.setText(R.string.operation_failed);
                        }

                        operation = null;
                    }
                })
                .build(Neatle.getDevice(macAddress));
        operation.execute();
    }
}
