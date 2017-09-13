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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.UUID;

@RunWith(RobolectricTestRunner.class)
public class ScanRecordTest {

    @Test
    public void testManufacturerData() {
        String scanRecordTxt = "02010517ffc1009dfd3557dab547743699d8b5dd943aab5b0000c00000000000000000000000000000000000000000000000000000000000000000000000";
        byte[] scanRecord = parseHex(scanRecordTxt);

        ScanRecord record = ScanRecord.createFromBytes(scanRecord);
        int mfgCount = record.getManufacturerData().size();
        Assert.assertEquals(mfgCount, 1);
        Assert.assertEquals(193, record.getManufacturerData().keyAt(0));
    }

    @Test
    public void testFitbitResponse() {
        String txt = "0201061106ba5689a6fabfa2bd01467d6eca36abad05160a180704696e34";
        byte[] scanRecord = parseHex(txt);
        ScanRecord record = ScanRecord.createFromBytes(scanRecord);


        UUID firstUUID = UUID.fromString("adab36ca-6e7d-4601-bda2-bffaa68956ba");


        Assert.assertTrue(record.getServiceUUIDs().contains(firstUUID));
    }

    @Test
    public void test16PartialUUID() {
        String scanRecordTxt = "0f0853504545445f43454c4c000000000201060302141807ffc300e81caeeb020a00";
        byte[] scanRecord = parseHex(scanRecordTxt);
        ScanRecord record = ScanRecord.createFromBytes(scanRecord);

        UUID partialUUID = UUID.fromString("00001814-0000-1000-8000-00805f9b34fb");
        Assert.assertTrue(record.getServiceUUIDs().contains(partialUUID));
    }

    private byte[] parseHex(String text) {
        int count = text.length() / 2;
        byte[] data = new byte[count];
        for (int i = 0; i < text.length(); i += 2) {
            data[i / 2] = (byte) Integer.parseInt(text.substring(i, i + 2), 16);
        }
        return data;
    }
}
