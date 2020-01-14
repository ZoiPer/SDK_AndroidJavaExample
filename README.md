# SDK_AndroidJavaExample

# Introduction

## Generic Information

**To obtain the SDK package, please contact us on zoiper.com**

This guide is here to assist software developers to create a VoIP application with the use of Zoiper SDK 2.0. With this guide you will get an overview of the entities in the SDK and some examples of their implementation, usage and configurataion.

## Licensing

Zoiper SDK 2.0 requires a license which can be purchased from Zoiper. There are two possibilities:

- Licensed per end-user installation model;
- Unlimited instalations;

Contact Zoiper for more details and test licenses.

## Common stuff for Zoiper SDK 2.0

### Threading model

Zoiper SDK 2.0 is thread-safe — shared objects can be called simultaneously from multiple threads. All callbacks from the SDK modules to the application code are performed in the context of the application thread which invokes the respective functions and methods. Processing time must be given to the SDK from the application core by invoking the respective functions in order to receive callbacks. In most cases on Android, iOS, and macOS, the main UI thread handles the giving of processing time to the SDK.

Regarding the sockets and transports, the SDK manages and utilizes the threads internally.

The SDK internally manages and utilizes its own separate thread for interaction with sockets/transports. Like that the application code can utilize the processing time without blocking the SDK sockets.

### Additional resources

Additionally to that document, inside the SDK packages you can find the respective reference for all the methods, functions, APIs, callbacks and so on. The basic usage of each one of these is also illustrated in it. 

## Third-party software

Portions copyrights:

- JThread, Copyright (C) 2000-2005 Jori Liesenborgs (jori@lumumba.uhasselt.be)
- JRTPLIB, part of JRTPLIB Copyright (C) 1999-2005 Jori Liesenborgs
- GSM, Copyright 1992, 1993, 1994 by Jutta Degener and Carsten Bormann, Technische Universitaet Berlin
- iLBC, iLBC Speech Coder ANSI-C Source Code iLBC_define.h Copyright (C) The Internet Society (2004). All Rights Reserved.
- SPEEX The Xiph OSC and the Speex Parrot logos are trademarks (TM) of Xiph.Org.
- OpenLDAP, Copyright 1999-2003 The OpenLDAP Foundation, Redwood City, California, USA. All Rights Reserved.
- PortAudio, Copyright (C) 1999-2002 Ross Bencina and Phil Burk
- PortMixer, PortMixer, Windows WMME Implementation, Copyright (C) 2002, Written by Dominic Mazzoni and Augustus Saunders
- Resiprocate, The Vovida license The Vovida Software License, Version 1.0, Copyright (C) 2000 Vovida Networks, Inc. All rights reserved.
- This product includes cryptographic software written by Eric Young (eay@cryptsoft.com).
- This product includes software written by Tim Hudson (tjh@cryptsoft.com).
- This product is using the gloox XMPP library - Copyright by Jakob Schroeter.

For any concerns and for more information please contact sales@zoiper.com.


## Introduction to the Zoiper SDK 2.0 for Android

This section is intended for developers who are designing a VoIP application using Java, or Kotlin and provides information for setting up the Zoiper Software Development Kit for Android.
Zoiper SDK 2.0 is an all-inclusive solution for developing applications with audio calls (SIP, IAX), video calls (SIP), presence, messaging, call recordings and other functionality. The SDK consists of an Android library with the respective headers that can be easily integrated into a target application.
### Hardware and software requirements

|Requirement|Description|
|--------|--------|
|Processor|Intel Core i3 or better|
|Memory|4GB (minimum) 8 GB (recommended)|
|Hard disk space|1 GB|
|Android Studio|Android Studio, Gradle configuration and Android API level of 16 or above (64 Bit requires API level 21 or above)|
|Operating system (Development environment)|Windows, Linux|
|Operating system (Runtime environment)|Android 4.1.3 or newer|
|Architecture|armv7, arm64|
|Mode|Live device|
### Contents of the SDK package

|Folder|Description|
|--------|--------|
|package/Demo|Example application for demonstrating some of the SDK functionalities.|
|package/Documentation|Contains the Zoiper SDK Documentation. There is an HTML folder, which holds the HTML reference documentation. Open the index.html file to open the reference documentation at the main page.|
|package/zdk.java-release.aar|The actual framework, which has to be imported in the project. |

## Setting up the Demo project
### Default configuration
Transport type: TCP
Enabled codecs: aLaw, uLaw, GSM, speex, iLBC30, G729, VP8 
STUN: Disabled
Default STUN settings:
Server: stun.zoiper.com
Port: 3478
Refresh period: 30
### Activation
To be able to test the example, an SDK Activation is required. You may use the activation username and password received from Zoiper. Just fill them in before you build the application in assets/credentials.json (you might need to rename the file to be called credentials.json beforehand). 
In case the activation is fine, the activation status will change to **“Success”** and you should be able to start testing the application. If it is not successful **“Failed”** status will be returned, followed by the cause code. You may check the statuses in the console log.
The credentials can be hardcoded as well, so there will be no need to be entered manually. To do so, use the following call:

```
private class ActivatorThread extends Thread{
    ActivatorThread() {
        super("ActivatorThread");
    }
    @Override
    public void run() {
        // Load the credentials.
        Credentials credentials = Credentials.load(getApplicationContext());
        // It is wise to offload that to a background thread because it takes some time
        zdkContext.activation().startSDK(
                getCertCacheFile().getAbsolutePath(),
                credentials.username,
                credentials.password
        );
    }
}
```

You would only need to pass your credentials instead of taking them from the GUI.
Keep in mind that the activation is put in a different thread for optimization.
### Account registration
You will need to have a SIP account in order to register to the server and make calls. To configure it, enter its details – username, password and hostname at least, and then hit the **„Register“** button. 
The status screen will show the registration status. When the registration is successful you should be able to make calls. If not, then the status screen will show the error code for the reason.
### Changing the project configuration
To change the SIP configuration, you will need to adjust the calls in:
```
private func createDefaultSIPConfiguration() -> ZDKSIPConfig
```
You can adjust the configuration by using the following method calls.
#### Transport type
```
sipConfig.transport(TransportType.TCP);
Possible values: .NA, .TCP, .UDP, .TLS
```
#### STUN
```
if(Configuration.STUN){
    sipConfig.stun(createStunConfig(ap));
}
...
private StunConfig createStunConfig(AccountProvider ap){
    final StunConfig stunConfig = ap.createStunConfiguration();
    stunConfig.stunEnabled(true);
    stunConfig.stunServer("stun.zoiper.com");
    stunConfig.stunPort(3478);
    stunConfig.stunRefresh(30000);
    return stunConfig;
}
```
#### Rport
```
sipConfig.rPort(RPortType.Signaling);
```
Possible values: No, Signaling, SingnalingAndMedia
#### DTMF
```
public static final DTMFTypeSIP DTMF_TYPE = DTMFTypeSIP.SIP_info_numeric;
```
Possible values: .NA, .Inband, .RFC_2833, .SIP_info_numeric, .Disabled
#### Changing the used codecs
```
@NonNull
private List<AudioVideoCodecs> getAudioCodecs() {
    List<AudioVideoCodecs> codecs = new ArrayList<>();
    codecs.add(AudioVideoCodecs.OPUS_WIDE);
    codecs.add(AudioVideoCodecs.PCMU);
    codecs.add(AudioVideoCodecs.vp8); // This is for the videocall
    return codecs;
}
```
Possible values: NA, G729, GSM, iLBC_20, iLBC_30, h263_plus, h264, VP8, h264_hwd, SPEEX_NARROW, SPEEX_WIDE, SPEEX_ULTRA, G726, OPUS_NARROW, OPUS_WIDE, OPUS_SUPER, OPUS_FULL, AMR, AMR_WB, PCMU, PCMA, G722 

## Create your App
You will need to setup your environment the same way as for the test of the example application. Keep in mind that the application must run on a real device and cannot be used with simulators. To use Zoiper SDK 2.0 in your project, you will need to import it as a new module into the project. This can be done as follows:

1. In Android Studio from `Flle->New->New module...`;
2. Choose `Import .JAR/.AAR package`;
3. Find the Zoiper SDK 2.0 file and choose it;
4. After Zoiper SDK 2.0 is added to the project open `Flle->Project structure` and select the `Dependencies` tab
5. Add a new dependency of the application to Zoiper SDK 2.0;
6. Make sure to initialize Zoiper SDK 2.0 context into your application, so you may use all of its functions.

A sample of initializing the context is shown here:

###### Initialize Zoiper SDK 2.0 Context in the application
```
    @Override
    public void onCreate() {
        super.onCreate();

        initializeZoiperContext();
        mainHandler = new Handler(getMainLooper());
    }
```
###### Zoiper SDK 2.0 initialization function example
```
    public void initializeZoiperContext(){
        zdkContext = makeZoiperContext();
        zdkContext.setStatusListener(this);

        // Before we can use the instance, we need to activate it.
        new ActivatorThread().start();
    }

    private Context makeZoiperContext(){
        // We create the Context instance
        // NOTE: Keep this object single-instance
        try{
            Context zdkContext = new Context(getApplicationContext());

            File logFile = new File(getFilesDir(), "logs.txt");

            zdkContext.logger().logOpen(
                    logFile.getAbsolutePath(),
                    null,
                    LoggingLevel.Debug,
                    0
            );

            // Make sure you have both
            // ACCESS_NETWORK_STATE
            // and
            // INTERNET
            // permissions!!!!!!!!!!!
            zdkContext.configuration().sipPort(5060);
            //zdkContext.configuration().iaxPort();
            //zdkContext.configuration().rtpPort();

            zdkContext.configuration().enableSIPReliableProvisioning(false);
            zdkContext.encryptionConfiguration().tlsConfig().secureSuite(TLSSecureSuiteType.SSLv2_v3);
            zdkContext.encryptionConfiguration().globalZrtpCache(getZrtpCacheFile().getAbsolutePath());

            return zdkContext;
        } catch (UnsatisfiedLinkError e) {
            throw new RuntimeException(e);
        }
    }

```
Keep in mind that inside the initialization the SIP and RTP ports will be also initialized.
Right after the Zoiper SDK 2.0 is initialized, it is best to activate its license by using the activation username and password, provided to you by [Zoiper.com](https://www.zoiper.com/). The activation can be done by invoking the activateZDK method like so:
```
zdkContext.activation().startSDK(
	getCertCacheFile().getAbsolutePath(),
	credentials.username,
    credentials.password
    );
```

## [Android SDK Reference - Java](https://www.zoiper.com/documentation/android-java-sdk/)

The API and methods references can be found [here](https://www.zoiper.com/documentation/android-java-sdk/).