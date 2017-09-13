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

import android.util.SparseArray;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import si.inova.neatle.Neatle;
import si.inova.neatle.util.NeatleLogger;

/**
 * Represents a scan record from a Bluetooth LE scan.
 *
 * @see android.bluetooth.le.ScanRecord
 */
public final class ScanRecord {

    private static final int FLAGS = 0x1;

    private static final int SERVICE_UUIDS_16_PARTIAL = 0x02;
    private static final int SERVICE_UUIDS_16_COMPLETE = 0x3;
    private static final int SERVICE_UUIDS_32_PARTIAL = 0x04;
    private static final int SERVICE_UUIDS_32_COMPLETE = 0x05;
    private static final int SERVICE_UUIDS_128_PARTIAL = 0x06;
    private static final int SERVICE_UUIDS_128_COMPLETE = 0x07;

    //private static final int LOCAL_NAME_PARTIAL = 0x08;
    //private static final int LOCAL_NAME_COMPLETE = 0x09;
    //private static final int DATA_TYPE_TX_POWER_LEVEL = 0x0A;
    //private static final int DATA_TYPE_SERVICE_DATA = 0x16;


    private static final int MANUFACTURER_SPECIFIC_DATA = 0xff;
    private final SparseArray<byte[]> manufacturerData;
    private final List<UUID> serviceUUIDs;

    private int flags = 0;


    private ScanRecord(int flags, SparseArray<byte[]> manufacturerData, List<UUID> serviceUUIDs) {
        this.flags = flags;
        this.manufacturerData = manufacturerData;
        this.serviceUUIDs = serviceUUIDs;
  //      this.rawData = data;
    }


    public SparseArray<byte[]> getManufacturerData() {
        return manufacturerData;
    }

    public List<UUID> getServiceUUIDs() {
        return serviceUUIDs;
    }

    /**
     * Create a scan record from the LE data payload.
     *
     * @param data scan record data reported by a BLE device.
     *
     * @return a parsed ScanRecord instance.
     *
     * @see  android.bluetooth.BluetoothAdapter.LeScanCallback
     */
    public static ScanRecord createFromBytes(byte[] data) {
        SparseArray<byte[]> mfgData = new SparseArray<>();
        List<UUID> serviceUUIDs = new ArrayList<>();
        int flags = 0;

        int index = 0;
        do {
            int len = data[index++];
            if (len == 0) {
                break;
            }
            int type = data[index] & 0xFF;
            switch (type) {
                case FLAGS:
                    flags = data[index + 1];
                    break;
                case SERVICE_UUIDS_16_PARTIAL:
                case SERVICE_UUIDS_16_COMPLETE:
                    if (len > 2) {
                        int uuid = data[index + 1]& 0xFF;
                        uuid += (data[index + 2] & 0xFF) << 8;

                        serviceUUIDs.add(Neatle.createUUID(uuid));
                    }
                    break;
                case SERVICE_UUIDS_32_PARTIAL:
                case SERVICE_UUIDS_32_COMPLETE:
                    if (len > 4) {
                        int uuid = data[index + 1]& 0xFF;
                        uuid += (data[index + 2] & 0xFF) << 8;
                        uuid += (data[index + 3] & 0xFF) << 16;
                        uuid += (data[index + 4] & 0xFF) << 24;

                        serviceUUIDs.add(Neatle.createUUID(uuid));
                    }
                    break;
                case SERVICE_UUIDS_128_PARTIAL:
                case SERVICE_UUIDS_128_COMPLETE:
                    //+1 for length itself
                    if (len > 16) {
                        ByteBuffer bb = ByteBuffer.wrap(data, index + 1, 16).order(ByteOrder.LITTLE_ENDIAN);
                        long lsb = bb.getLong();
                        long msb = bb.getLong();
                        serviceUUIDs.add(new UUID(msb, lsb));
                    }
                    break;
                case MANUFACTURER_SPECIFIC_DATA:
                    if (len < 3) {
                        NeatleLogger.e("Bad manufacturer data. Length should be more than 2");
                        break;
                    }
                    int id = (data[index + 2] & 0xFF << 8) | (data[index + 1] & 0xFF);
                    ByteBuffer bb = ByteBuffer.allocate(len - 2);
                    bb.put(data, index + 3, len - 3);

                    byte[] mfg = new byte[len - 3];
                    System.arraycopy(data, index + 3, mfg, 0, len - 3);

                    mfgData.append(id, mfg);
                    break;
                default:
                    //TODO, add support for other datatypes
                    break;


            }
            index += len;
        } while (index < data.length);

        return new ScanRecord(flags, mfgData,serviceUUIDs);
    }

    private static String bufferToString(byte bb[]) {
        StringBuilder sb = new StringBuilder(bb.length * 2);
        for (byte b : bb) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[flags = 0x").append(Integer.toHexString(flags));

        for (int i = 0; i < manufacturerData.size(); i++) {
            int id = manufacturerData.keyAt(i);
            byte data[] = manufacturerData.valueAt(i);
            sb.append(", manufacturerId = ").append(id);
            sb.append(", data = ").append(bufferToString(data));
        }
        if (!serviceUUIDs.isEmpty()) {
            sb.append(", serviceUUIDS = ").append(serviceUUIDs);
        }
//        sb.append(", raw = ").append(bufferToString(rawData));


        sb.append(']');

        return sb.toString();
    }
}

