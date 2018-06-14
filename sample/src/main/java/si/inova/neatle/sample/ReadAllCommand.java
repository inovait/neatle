package si.inova.neatle.sample;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import si.inova.neatle.monitor.Connection;
import si.inova.neatle.operation.Command;
import si.inova.neatle.operation.CommandObserver;
import si.inova.neatle.operation.CommandResult;

/**
 * Created by tomazs on 26. 05. 2017.
 */

public class ReadAllCommand extends Command {
    private Queue<BluetoothGattCharacteristic> queue = new LinkedList<>();
    private ReadAllObserver observer;

    public ReadAllCommand(ReadAllObserver observer) {
        super(observer);
        this.observer = observer;
    }

    @Override
    protected void start(Connection connection, BluetoothGatt gatt) {
        List<BluetoothGattService> services = gatt.getServices();
        for (BluetoothGattService service : services) {
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : characteristics) {
                int props = characteristic.getProperties();
                if ((props & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
                    queue.add(characteristic);
                }
            }
        }

        readNext(gatt);
    }

    @Override
    protected void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        if (status != BluetoothGatt.GATT_SUCCESS) {
            finish(CommandResult.createErrorResult(null, status));
        } else {
            if (observer != null) {
                observer.characteristicRead(CommandResult.createCharacteristicRead(characteristic, status));
            }
            readNext(gatt);
        }
    }

    private void readNext(BluetoothGatt gatt) {
        BluetoothGattCharacteristic characteristic = queue.poll();
        if (characteristic == null) {
            finish(CommandResult.createEmptySuccess(null));
            return;
        }
        if (!gatt.readCharacteristic(characteristic)) {
            finish(CommandResult.createErrorResult(null, BluetoothGatt.GATT_FAILURE));
        }
    }

    @Override
    protected void onError(int error) {
        finish(CommandResult.createErrorResult(null, error));
    }

    interface ReadAllObserver extends CommandObserver {

        void characteristicRead(CommandResult result);


    }
}
