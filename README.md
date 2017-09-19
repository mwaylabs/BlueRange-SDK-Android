
# BlueRange SDK


## Overview
The BlueRange SDK is a library for Android and iOS that enables apps to interact with [Relution SmartBeacons](https://www.relution.io/de/beacon-management-plattform/). SmartBeacons can be managed, monitored, updated and configured centrally in Relution IoT by building up a BLE network (mesh) based on our [FruityMesh](https://github.com/mwaylabs/fruitymesh/wiki) beacon firmware making them constantly connected to the cloud.

## Features
Currently the BlueRange SDK supports iOS devices that run at least on iOS 8.0 and Android devices with API level 18 or higher. Features that are based on BLE advertising, however, additionally require the Bluetooth LE peripheral mode (API level 21).

The BlueRange SDK is build up of a core and a service layer.

The **core layer** contains a set of components that simplify processing streams of beacon messages (e.g. iBeacon or Eddystone messages). These message processing components can be combined to form a flexible event driven architecture.

The **service layer** builds on top of the core layer and contains Relution IoT specific services. 

More specifically, the following features are supported:

### Core
#### Advertising
- Sending BLE advertising messages of arbitrary data.
- Sending advertising messages for heatmap generation.

#### Scanning
- Scanning beacon messages of different schemes:
    - **iBeacon** message: Apple's standardized iBeacon format for BLE advertising which contains an identifier triple (UUID, major and minor).
    - **Eddystone UID** message: One of Google's standardized BLE beacon formats which consists of a 10-byte namespace UID and a 6-byte instance identifier.
    - **Eddystone URL** message: Another Eddystone beacon format that contains a URL.
    - **Join Me** message: Beacons based on FruityMesh broadcast these messages to establish a beacon network. Each packet holds the beacon id, its connectivity and some more information that can be used to identify and analyze Relution SmartBeacons.
    - **Relution Tag** message: An advertising message format only supported by Relution IoT. It contains a list of tags used for offline proximity messaging scenarios.
	- **Asset tracking** message: The advertising message format used by Relution AssetBeacons. Asset beacons can be used for asset localization and tracking.
- Scanning will be continued when the app is running in **background**. This feature, however, is limited due to Apple restrictions.
- **Energy efficiency**: can be controlled by changing the scan cycle duration and sleep interval.

#### Logging
- Messages can **logged persistently and postprocessed at a later time**.
- Useful for collecting training and validation data for indoor localization.

#### Reporting
- Messages logged over a long period of time can be summed up to **status reports** and published to the cloud. 
- The cloud could evaluate these reports for **heatmaps generation** or **indoor positioning**.

#### Aggregating
- Most proximity messaging use cases require stable signal strengths (RSSI) to correctly estimate distances to beacons. Message aggregators will **average RSSI values** of beacon message streams using average filters.

#### Triggering
- The message trigger can be used to implement **proximity messaging** scenarios. Whenever messages arrive andspecific time and/or location conditions are fulfilled, the trigger will execute an action and notify your app. Currently supported action parameters are:
  - Action delay: Actions will be executed after a predefined delay.
  - Action lock: Actions will be blocked for a specific amount of time after they have been triggered.
  - Activation distance: Actions will only be triggered, if the device has a distance to the beacon which falls below a predefined threshold.
  - Validation period: An action can have a start and end validation time.

### Service

#### Relution SmartBeacon Calibration
- Since each beacon varies in its radio characteristics, distance estimation can be improved by calibrating the RSSI.

#### Relution Proximity messaging
- Realization of proximity messaging (which is called "Relution Campaigns" in Relution IoT).

#### Relution Heatmaps
- The device will send advertising messages which will be collected by the beacons for heatmap generation in the cloud. 

## API reference
- Use the API reference in the ```docs``` folder for more information about the specific classes.

## Installation

The easiest way to import the BlueRangeSDK is to add a dependency to your project's ```build.gradle``` file, after you have imported the bluerangesdk-release.aar which you can find under the path bluerangesdk/build/outputs/aar.
```gradle
dependencies {
    compile project(':bluerangesdk')
}
```



## Build


### Run tests
```bash
./gradlew test
```


### Build

```bash
./gradlew build
```



### Generate Javadoc

```bash
./gradlew generateJavadoc
```


## Sample code
The following section shows you some code samples that may help you to integrate the library into your app.

### Relution IoT Services
#### Service configuration
If your app should depend on Relution services, the primary class of interest is ```RelutionIoTService```. As can be seen below, you must configure the service before starting it.
- **Campaigns**: Turn on this feature, if you want to use Relution proximity messaging and get notified about executed actions that you defined in the Relution "campaigns" section.
- **Analytics**: Turn on this feature, if you want the SDK to periodically send reports to Relution, which could later be used for analytics.
- **Heatmap**: Turn on this feature, if you want the device to send out heatmap messages. Relution SmartBeacons will estimate the number of devices next to them and send heatmap reports to the cloud.
- **Logging**: Turn logging on, if you want the SDK to log to the console. Turn this off to increase the app's overall performance.


```java
String baseUrl = "https://iot2.relution.io";
String username = "your_relution_username";
String password = "your_relution_password";

RelutionIoTService.LoginObserver observer = new RelutionIoTService.LoginObserver() {
    @Override
    public void onLoginSucceeded() {

    }

    @Override
    public void onLoginFailed() {

    }

    @Override
    public void onRelutionError() {

    }
};

new RelutionIoTService()
    .setLoginData(baseUrl, username, password, observer)
    .setLoggingEnabled(true) // Logging
    .setCampaignActionTriggerEnabled(true) // Campaigns
    .setHeatmapGenerationEnabled(true) // Heatmap
    .setHeatmapReportingEnabled(true) // Analytics
    .startAsThread(context.getApplicationContext());
```


#### Relution SmartBeacon calibration
To calibrate the RSSI value of an iBeacon message, just place the device 1 meter away from the beacon
and send the mean RSSI value (of approximately 10 seconds) to Relution by calling ```calibrateIBeacon```, as shown below:

```java
RelutionIoTService.addBeaconMessageObserver(new RelutionIoTService.BeaconMessageObserver() {
  @Override
  public void onMessageReceived(BeaconMessage message) {
    // Do something with the message.
    if (message instanceof IBeaconMessage) {
      // Get the iBeacon message.
      IBeaconMessage iBeaconMessage = (IBeaconMessage) message;
      // User moves to a place 1 meter away from the beacon that sends the iBeacon message...
      // Calibrate the iBeacon message.
      RelutionIoTService.calibrateIBeacon(iBeaconMessage.getIBeacon(), iBeaconMessage.getRssi());
    }
  }
});
```

#### Relution Proximity messaging
Register event listeners for the actions defined in the Relution "campaigns" section.

```java
// Get informed about to campaign actions.
RelutionIoTService.addBeaconNotificationActionObserver(new RelutionIoTService.BeaconNotificationActionObserver() {
    @Override
    public void onNotificationActionExecuted(RelutionNotificationAction notificationAction) {
        // Do something...
    }
});
RelutionIoTService.addBeaconContentActionObserver(new RelutionIoTService.BeaconContentActionObserver() {
    @Override
    public void onContentActionExecuted(RelutionContentAction contentAction) {
        // Do something...
    }
});
RelutionIoTService.addBeaconTagActionObserver(new RelutionIoTService.BeaconTagActionObserver() {
    @Override
    public void onTagActionExecuted(RelutionTagAction tagAction) {
        // Do something...
    }
});
```


#### Relution Heatmaps
To enable heatmap reporting, just start advertising the discovery message using the ```BeaconAdvertiser``` class.

```java
new BeaconAdvertiser().startAdvertisingDiscoveryMessage();
```



#### Relution tags
If you use Relution Tags for proximity messaging, register a ```RelutionTagObserver``` to get informed about all received Relution Tags. If you need to have access to the name or description of a Relution Tag, just call ```getTagInfoForTag```:

```java
RelutionIoTService.addRelutionTagObserver(new RelutionIoTService.RelutionTagObserver() {
  @Override
  public void onTagReceived(long tag, RelutionTagMessage message) {
    try {
      RelutionIoTService.getTagInfoForTag(tag);
    } catch (RelutionTagInfoRegistry.RelutionTagInfoRegistryNoInfoFound relutionTagInfoRegistryNoInfoFound) {
      // ...
    }
  }
});
```


### Core
Use the core layer, if you want to build beacon-aware apps that are independent of Relution.

#### Scanning
Start the scanner, as shown below. You can change the scanner's configuration even if it has already been started.

```java
final BeaconMessageScanner beaconScanner = new BeaconMessageScanner(this);
final BeaconMessageScannerConfig config = new BeaconMessageScannerConfig(beaconScanner);
config.scanIBeacon("b9407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1);
config.scanIBeacon("c9407f30-f5f8-466e-aff9-25556b57fe6d", 46, 2);
config.scanRelutionTags(new long[]{13, 2});
config.scanJoinMeMessage();
beaconScanner.setConfig(config);
beaconScanner.addReceiver(new BeaconMessageStreamNodeReceiver() {
  @Override
  public void onMeshActive(BeaconMessageStreamNode senderNode) {
    Log.d(Config.projectName, "onMeshActive");
  }

  @Override
  public void onReceivedMessage(BeaconMessageStreamNode senderNode, BeaconMessage message) {
    Log.d(Config.projectName, "onBeaconUpdate");
    Log.d(Config.projectName, message.toString());
  }

  @Override
  public void onMeshInactive(BeaconMessageStreamNode senderNode) {
    Log.d(Config.projectName, "onMeshInactive");
  }
});
beaconScanner.startScanning();
```


#### Logging
If you want to process beacon messages at a later time, it might be useful to save them on the device persistently and read them out later. To do this, you can use the ```BeaconMessageLogger``` which provides an easy-to-use and thread-safe interface. In most cases you will pass the scanner to the logger's constructor. However, if your message processing pipeline is more complex, you can pass any message processing component implementing the ```BeaconMessageStreamNode``` interface. The received messages will be instantly passed to all receivers that have attached to the logger. Thus, you can use the logger to silently persist the message stream:

```java
// Configure Beacon scanner
final BeaconMessageScanner beaconScanner = new BeaconMessageScanner(context);
BeaconMessageScannerConfig config = new BeaconMessageScannerConfig(beaconScanner);
config.scanIBeacon("b9407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1);
config.scanIBeacon("c9407f30-f5f8-466e-aff9-25556b57fe6d", 46, 2);
config.scanRelutionTagsV1(new long[]{13, 2});
beaconScanner.setConfig(config);

// Configure BeaconMessageLogger
BeaconMessageLogger logger = new BeaconMessageLogger(beaconScanner, context);
logger.addReceiver(new BeaconMessageStreamNodeReceiver() {
    @Override
    public void onMeshActive(BeaconMessageStreamNode senderNode) {}

    @Override
    public void onReceivedMessage(BeaconMessageStreamNode senderNode, BeaconMessage message) {
        // Do something
    }

    @Override
    public void onMeshInactive(BeaconMessageStreamNode senderNode) {}
});
beaconScanner.startScanning();
```



If you need to consume all messages saved in the log in one step, you can use the ```readLog``` method. However, if your log contains a large number of messages, better use the log iterator or a for each loop to reduce the memory consumption. The iterator will load the messages in the order they have been saved and is optimized for thread-safety and performance.

```java
for (BeaconMessage message : logger) {
    // Do something
}
```


#### Aggregating
Message aggregation can be useful, if you want to reduce the overall message throughput or if you want to average the received signal strength (RSSI). Currently the ```BeaconMessageAggregator``` supports two modes, a packet mode and a sliding window mode. The packet mode combines a stream of equivalent messages received for a specific amount of time, whereas the sliding window mode keeps the same number of messages in the stream while averaging the RSSI using a moving average filter:

```java
BeaconMessageAggregator aggregator = new BeaconMessageAggregator(Tracer.getInstance(), beaconScanner);
aggregator.setAggregationMode(BeaconMessageAggregator.AggregationMode.SLIDING_WINDOW);
aggregator.setAggregateDurationInMs(5 * 1000);
aggregator.setAverageFilter(new LinearWeightedMovingAverageFilter(0.3f));
```



#### Triggering
For implementing proximity messaging use cases, use the ```BeaconMessageActionTrigger``` class. The trigger will execute an action, whenever a matching message is received and the location and time conditions are fulfilled. The message-action mapping and the time and location parameters must be defined in a ```BeaconActionRegistry```, as shown below.

```java
BeaconMessageActionTrigger actionTrigger = new BeaconMessageActionTrigger(scanner, new BeaconActionRegistry() {
    @Override
    public boolean isAvailable(BeaconMessage message) throws UnsupportedMessageException {
        return false;
    }
    
    @Override
    public List<BeaconAction> getBeaconActionsForMessage(BeaconMessage message)
            throws RegistryNotAvailableException, UnsupportedMessageException {
        List<BeaconAction> actions = new ArrayList<BeaconAction>();
        if (message instanceof IBeaconMessage) {
            IBeaconMessage iBeaconMessage = (IBeaconMessage) message;
            if (iBeaconMessage.getMinor() == 5) {
                BeaconAction action = new BeaconAction("unique-identifier");
                action.setDistanceThreshold(5);
                action.setValidityBegins(new Date());
                action.setValidityEnds(new Date(new Date().getTime() + 5 * 1000));
                action.setReleaseLockAfterMs(5 * 1000);
    
                actions.add(action);
            }
        }
        return actions;
    }
    });
    
    actionTrigger.addActionListener(new BeaconActionListener() {
    @Override
    public void onActionTriggered(BeaconAction action) {
        if (action instanceof RelutionContentAction) {
            Log.d("TriggerSystemTest", "Content action triggered.");
        } else if (action instanceof RelutionTagAction) {
            RelutionTagAction tagAction = (RelutionTagAction) action;
            RelutionTagVisit tag = tagAction.getTag();
            Log.d("TriggerSystemTest", "Collected tag: " + tag.toString());
        }
    }
    });
    
    // 4. Start trigger and scanner
    actionTrigger.start();
    scanner.startScanning();
```


#### Advertising
To periodically send advertising messages, just call one of the ```start``` methods of the ```BeaconAdvertiser``` class:

```java
public class AdvertisingService extends BlueRangeService {
    @Override
    public void onStarted() {
        try {
            BeaconAdvertiser advertiser = new BeaconAdvertiser(this.getApplicationContext());
            advertiser.startAdvertisingDiscoveryMessage();
        } catch (PeripheralAdvertisingNotSupportedException e) {
            Log.d("Advertiser", "This device does not support advertising in peripheral mode!");
            e.printStackTrace();
        }
    }
}
```


## License
```
* Copyright (c) 2017, M-Way Solutions GmbH
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*     * Redistributions of source code must retain the above copyright
*       notice, this list of conditions and the following disclaimer.
*     * Redistributions in binary form must reproduce the above copyright
*       notice, this list of conditions and the following disclaimer in the
*       documentation and/or other materials provided with the distribution.
*     * Neither the name of the M-Way Solutions GmbH nor the
*       names of its contributors may be used to endorse or promote products
*       derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY M-Way Solutions GmbH ''AS IS'' AND ANY
* EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL M-Way Solutions GmbH BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
```
