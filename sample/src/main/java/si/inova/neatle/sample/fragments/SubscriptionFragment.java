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

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import si.inova.neatle.Neatle;
import si.inova.neatle.operation.CharacteristicSubscription;
import si.inova.neatle.operation.CharacteristicsChangedListener;
import si.inova.neatle.operation.CommandResult;
import si.inova.neatle.sample.R;

/**
 * A fragment displaying the operation functionality of NeatLE.
 */
public class SubscriptionFragment extends Fragment implements CharacteristicsChangedListener {

    public static final UUID BATTERY_LEVEL_SERVICE = Neatle.createUUID(1);
    public static final UUID BATTERY_LEVEL = Neatle.createUUID(2);

    @BindView(R.id.subscription_mac_input)
    EditText macInput;
    @BindView(R.id.subscription_connection_state_label)
    TextView statusLabel;

    private Unbinder unbinder;

    private CharacteristicSubscription characteristicSubscription;

    public SubscriptionFragment() {
    }

    public static SubscriptionFragment newInstance() {
        return new SubscriptionFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_subscription, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.subscription_connect_button)
    public void onSubscribeClicked() {
        String macAddress = macInput.getText().toString();

        if (!Neatle.isMacValid(macAddress)) {
            Toast.makeText(getContext(), "MAC address not recognized", Toast.LENGTH_LONG).show();
            return;
        }

        if (characteristicSubscription != null) {
            characteristicSubscription.stop();
            characteristicSubscription = null;
        }

        characteristicSubscription = Neatle.createSubscription(getContext(), Neatle.getDevice(macAddress), BATTERY_LEVEL_SERVICE, BATTERY_LEVEL);
        characteristicSubscription.setOnCharacteristicsChangedListener(this);
        characteristicSubscription.start();
    }

    @Override
    public void onCharacteristicChanged(CommandResult change) {
        if (change.wasSuccessful()) {
            int batteryLevel = change.getValueAsInt8();
            statusLabel.setText(getString(R.string.battery_level_status, batteryLevel));
        } else {
            statusLabel.setText(R.string.subscription_failed);
        }
    }
}