## Purpose of this documentation

This guide assists you in rapidly developing your VoIP application with Zoiper SDK 2.0. This manual contains an overview of the entities in the SDK with a lot of practical examples of implementation, usage and configuration.

**To obtain the SDK package, please contact us on zoiper.com**

## Licensing

To enjoy the powerful benefits of Zoiper SDK 2.0, you need a license. Depending on your needs, you can buy 2 different types of licenses:

- Installation license per end-user
- Unlimited installations;

Please [<span style="color:orange">contact Zoiper</span>](mailto:sales@zoiper.com) for more details and test licenses or to receive licenses for testing purposes.

## Threading model

Zoiper SDK 2.0 is thread-safe. Shared objects can be called simultaneously from multiple threads. All callbacks from the SDK modules to the application code are performed in the context of the application thread which invokes the respective functions and methods.

In order to receive callbacks, the SDK needs to receive processing time from your application core. You can achieve this by invoking the respective functions.

On Android, iOS, and macOS, the main UI thread usually handles the assignment of processing time to the SDK.

Regarding sockets and transports, the SDK manages and utilizes the threads internally. For the interaction with sockets and transports, the SDK also internally manages and utilizes its own separate thread. As a result, the application code can use the processing time without blocking the SDK sockets.

## More resources

Inside the SDK packages, you can find the respective reference and examples of basic usage for all:

- methods
- functions
- APIs
- callbacks
- etc.

## Third-party software

The SDK is (partially) built with:

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

Please contact [<span style="color:orange">sales@zoiper.com</span>](mailto:sales@zoiper.com) for more information.

## Introduction to the Zoiper SDK 2.0 for Android

If you develop an Android VoIP application with Java, or Kotlin, the Zoiper Software Development Kit for Android will come in handy.  Zoiper SDK 2.0 is namely an all-inclusive solution for developing Android applications with

- audio calls (SIP, IAX)
- video call (SIP)
- presence
- messaging
- call recordings
- other functionalities

The SDK consists of an Android library with the respective headers that you can easily integrate into your target application.

### Hardware and software requirements

|Requirement|Description|
|--------|--------|
|Processor|Intel Core i3 or better|
|Memory|4GB (minimum) 8 GB (recommended)|
|Hard disk space|1 GB|
|Android Studio|Android Studio, Gradle configuration and Android API level of 16 or above (64 Bit requires API level 21 or above)|
|Operating system (Development environment)|Windows, Linux|
|Operating system (Runtime environment)|Android 4.1.3 or newer|
|Architecture|armv7, arm64, x86, x86_64|
|Mode|Live device|
### Contents of the SDK package

|Folder|Description|
|--------|--------|
|package/Demo | Demo application with some of the main SDK functionalities. Contains the Zoiper SDK Documentation. Click on the index.html file to open the html documentation in your browser. |
|package/Documentation | Contains the Zoiper SDK Documentation. There is an HTML folder, which holds the HTML reference documentation. Open the index.html file to open the reference documentation on the main page. |
|package/zdk.java-release.aar | The actual framework, which you need to import in your project. |


## How to add ZDK to your project
### 1. Create a new project.
### 2. Create a new module from the .aar file
- Go to File -> New -> New Module.
- Choose "Import .Jar/.Aar package"

![](./docs/import_aar.jpg)

- Choose File location (The ZDK .aar file)

![](./docs/import_module.jpg)

### 3. Add the new module to the project.
- Go to File -> Project structure -> Modules <app name>
- "Dependencies" tab
- Clink on the "+" sign
- Choose "Module dependency"

![](./docs/module_dependency.jpg)

- Select the ZDK module and click "OK"

![](./docs/choose_modules.jpg)

## Setting up the Demo project
### Default configuration
Transport type: TCP
Enabled codecs: 
- aLaw
- uLaw
- GSM
- speex
- iLBC30
- G729
- VP8 

STUN: Disabled
- Default STUN settings:
- Server: stun.zoiper.com
- Port: 3478
- Refresh period: 30

### Activation

Before you can test the demo app, you need to activate the SDK. You will receive a username and password from Zoiper.com. 

##### Manual 
Enter the activation credentials before you build the application in assets/credentials.json. Please note: you might first need to rename the file to credentials.json. 

When the activation is fine, the status will change to “Success”.  You can now start testing the demo app. 

When the registration fails, you will receive a “Failed” status, followed by an error code. Please, check the status in the console log. 

##### Hardcoded
If you prefer hardcoded credentials, you can use  the following call:

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

Do not take your credentials from the GUI, but pass them. Please mind that the activation is put in a different thread for reasons of optimization.

### Account registration

You need a SIP account to register to the server and make calls. Configure it in 2 steps:
1. Enter the details (username, password and hostname at least)
1. Click on the „Register“ button 

The status screen will show the registration status. When the registration is successful, you should be able to make calls. If not, then the status screen will show an error code.

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
Possible values: 
- No
- Signaling
- SingnalingAndMedia

#### DTMF
```
public static final DTMFTypeSIP DTMF_TYPE = DTMFTypeSIP.SIP_info_numeric;
```
Possible values: 
- .NA
- .Inband
- .RFC_2833
- .SIP_info_numeric
- .Disabled

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
Possible values:
- NA
- G729
- GSM
- iLBC_20
- iLBC_30
- h263_plus
- h264
- VP8
- h264_hwd
- SPEEX_NARROW
- SPEEX_WIDE
- SPEEX_ULTRA
- G726
- OPUS_NARROW
- OPUS_WIDE
- OPUS_SUPER
- OPUS_FULL
- AMR
- AMR_WB
- PCMU
- PCMA
- G722

## Create your Android VoIP App

Set up your environment in the same way as you did for the demo application. 
Keep in mind that your application must run on a real device. It cannot be used with simulators. To use Zoiper SDK 2.0, you need to import it as a new module into your project. You can do so as follows:

1. In Android Studio from `Flle->New->New module...`;
2. Choose `Import .JAR/.AAR package`;
3. Find the Zoiper SDK 2.0 file and choose it;
4. After Zoiper SDK 2.0 is added to the project open `Flle->Project structure` and select the `Dependencies` tab
5. Add a new dependency of the application to Zoiper SDK 2.0;
6. Make sure you initialize Zoiper SDK 2.0 context into your application. This way, you can use all of its functions.

Here you can find a sample of initializing the context:

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

## Android SDK Reference - Java

You can find the API and methods references [<span style=color:orange>here</span>](https://www.zoiper.com/documentation/android-java-sdk/).