# NeatLE

[![Build Status](https://img.shields.io/travis/inovait/neatle/master.svg)](https://travis-ci.org/inovait/neatle) [![Codacy gradle](https://img.shields.io/codacy/grade/009856db7a104d29b11249a8ef024716.svg)](https://www.codacy.com/app/inovait/neatle) [![Codacy coverage](https://img.shields.io/codacy/coverage/009856db7a104d29b11249a8ef024716.svg)](https://www.codacy.com/app/inovait/neatle) [ ![Download](https://api.bintray.com/packages/inovait/NeatLE/neatle/images/download.svg) ](https://bintray.com/inovait/NeatLE/neatle/_latestVersion)

NeatLE is an Android BTLE (Bluetooth low energy) support library that simplifies management of BTLE connections, subscriptions, devices and operations.
It provides a single entry point for all BTLE related operations.

## Features
 * Monitor a connection to a BTLE device.
 * Send read / write commands and receive responses.
 * Subscribe to characteristics and get notified of changes.

## Installation

Include the NeatLE library in your Android project as a Gradle dependency:
```groovy
dependencies {
    compile 'si.inova:neatle:0.9.3'
}
```

## Examples

### Monitor for connections:

A `ConnectionMonitor` tries to connect to a specific BTLE device, and if configured with `setKeepAlive(true)`, will try to maintain that connection until the monitor is stopped.
The `ConnectionStateListener` will be notified of any changes to the connection, (e.g. device connected / disconnected).

```java
ConnectionMonitor monitor =
        Neatle.createConnectionMonitor(context, Neatle.getDevice("00:11:22:33:44:55"));
monitor.setOnConnectionStateListener(new ConnectionStateListener() {
    @Override
    public void onConnectionStateChanged(Connection connection, int newState) {
        if(connection.isConnected()){
            // The device has connected
        }
    }
});
monitor.start();
```


### Create an operation:

An `Operation` is a series of read / write commands that can be executed for a specific BTLE device.
It can have multiple read **and** write operations, that will be executed on the device sequentially.

An operation can also be executed more than once.

```java
BluetoothDevice device = Neatle.getDevice("00:11:22:33:44:55");
UUID batteryService = Neatle.createUUID(0x180f);
final UUID batteryCharacteristic = Neatle.createUUID(0x2a19);

Operation operation = Neatle.createOperationBuilder(context)
        .read(batteryService, batteryCharacteristic)
        .onFinished(new SimpleOperationObserver() {
            @Override
            public void onOperationFinished(Operation op, OperationResults results) {
                if(results.wasSuccessful()){
                    int battery = results.getResult(batteryCharacteristic).getValueAsInt();
                }
            }
        })
        .build(device);
operation.execute();
```


### Create a subscription:

A `Subscription` listens for notification events on a specific service for a specific characteristic on the BTLE device, and reports them back to the caller.
```java
BluetoothDevice device = Neatle.getDevice("00:11:22:33:44:55");
UUID batteryService = Neatle.createUUID(0x180f);
UUID batteryCharacteristic = Neatle.createUUID(0x2a19);

CharacteristicSubscription subscription =
        Neatle.createSubscription(context, device, batteryService, batteryCharacteristic);
subscription.setOnCharacteristicsChangedListener(new CharacteristicsChangedListener() {
    @Override
    public void onCharacteristicChanged(CommandResult change) {
        if (change.wasSuccessful()) {
            int batteryLevel = change.getValueAsInt();
        }
    }
});
subscription.start();
```
Note: An active subscription will keep a `ConnectionMonitor` with `setKeepAlive(false)` (the default setting) alive.

## Permissions

The following permission are required by NeatLE (and are already defined in it's manifest):

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```
The `ACCESS_COARSE_LOCATION` permission is considered *dangerous* on Android 6.0 (23) and up, and should be requested manually by the developer, before running NeatLE.


## License

    MIT License
    
    Copyright (c) 2017 Inova IT
    
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.`