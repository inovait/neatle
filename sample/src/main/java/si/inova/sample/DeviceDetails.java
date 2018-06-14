package si.inova.sample;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import si.inova.neatle.Neatle;
import si.inova.neatle.ServicesDiscoveredListener;
import si.inova.neatle.monitor.Connection;
import si.inova.neatle.monitor.ConnectionMonitor;
import si.inova.neatle.monitor.ConnectionStateListener;
import si.inova.neatle.operation.Command;
import si.inova.neatle.operation.CommandResult;
import si.inova.neatle.operation.Operation;
import si.inova.neatle.operation.OperationBuilder;
import si.inova.neatle.operation.OperationObserver;
import si.inova.neatle.operation.OperationResults;
import si.inova.neatle.operation.SimpleOperationObserver;
import si.inova.neatle.sample.R;
import si.inova.neatle.sample.ReadAllCommand;
import si.inova.neatle.sample.fragments.ScannerFragment;

public class DeviceDetails extends AppCompatActivity implements ConnectionStateListener, ServicesDiscoveredListener {
    private BluetoothDevice device;
    private ConnectionMonitor monitor;
    private DeviceDetailsAdapter adapter;

    private Operation readAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new DeviceDetailsAdapter();
        RecyclerView rView = (RecyclerView) findViewById(R.id.servicesList);
        rView.setAdapter(adapter);

        device = getIntent().getParcelableExtra("device");

        monitor = Neatle.createConnectionMonitor(this, device);
        monitor.setKeepAlive(true);
        monitor.setOnServiceDiscoveredListener(this);

        readAll = Neatle.createOperationBuilder(this)
                .executeCommand(new ReadAllCommand(null))
                .onFinished(new SimpleOperationObserver() {
                    @Override
                    public void onOperationFinished(Operation op, OperationResults results) {
                        adapter.notifyDataSetChanged();
                    }
                }).build(device);

    }

    @Override
    protected void onResume() {
        super.onResume();
        monitor.start();

        if (monitor.getConnection().areServicesDiscovered()) {
            adapter.updateServices(monitor.getConnection());
        }

        readAll.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();

        monitor.stop();
        readAll.cancel();
    }

    @Override
    public void onConnectionStateChanged(Connection connection, int newState) {
        if (connection.isConnected()) {
            adapter.updateServices(connection);
        }
    }

    @Override
    public void onServicesDiscovered(Connection connection) {
        adapter.updateServices(connection);
    }

    public static class DeviceDetailsAdapter extends RecyclerView.Adapter<DeviceDetailsAdapter.ViewHolder> {
        private List<Object> items = new ArrayList<>();

        @Override
        public DeviceDetailsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.btle_service_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DeviceDetailsAdapter.ViewHolder holder, int position) {
            Object item = items.get(position);
            if (item instanceof BluetoothGattService) {
                BluetoothGattService service = (BluetoothGattService) item;
                holder.setTitle(service.getUuid().toString());
                holder.setSubTitle("service");
            } else if (item instanceof  BluetoothGattCharacteristic) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) item;
                holder.setTitle(characteristic.getUuid().toString());
                holder.setSubTitle("Data: " + characteristic.getStringValue(0));
            } else {
                holder.setTitle("Unknown");
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private void updateServices(Connection conn) {
            items.clear();
            for (BluetoothGattService service : conn.getServices()) {
                items.add(service);
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    items.add(characteristic);
                }
            }
            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView viewTitle;
            private final TextView viewSubTitle;

            public ViewHolder(View itemView) {
                super(itemView);
                viewTitle = (TextView) itemView.findViewById(R.id.textTitle);
                viewSubTitle = (TextView) itemView.findViewById(R.id.textSubtitle);
            }

            public void setTitle(String text) {
                viewTitle.setText(text);
            }

            public void setSubTitle(String text) {
                viewSubTitle.setText(text);
            }
        }
    }


}
