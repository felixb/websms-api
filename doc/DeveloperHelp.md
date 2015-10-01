# If you want to code your own Connector, please read this page.

## Introduction 

WebSMS is built modular. This makes it very easy to implement new Connectors.

Each Connector is installed separately.
This makes it very easy to build your own Connector and even deploy it on your own!

## API 

You will need the public API of WebSMS's Connector framework.

For examples, just check out the code on any of the connectors hosted on [github][1].
Just pick the one, that fits you best.
Some are using XML, some are using custom HTTP methods, some are using very simple HTTP APIs.

Basically you just need to inherit the [Connector class][3] or even simpler the [BasicConnector class][4].
But see the example or any other Connector for more details.

With all this, you should be able to build and deploy your own Connector.
If something is unclear. Do not hesitate to ask me.

## Architecture 

Here is a sequence diagram showing how the inner communication is working:

![Seqence diagram][5]

The main app is just an activity with all it's GUI and logic behind.
There is nothing special.
But: It is not able to communicate with any web service without a Connector.
Each connector is installed as single apk (only the GSM SMS Connector is shipped with WebSMS.apk).
Each connector is living in it's own context.

Communication is done with [Broadcasts][6].
The WebSMS app is sending broadcast to the specific connector when running the update, bootstrap or send command, 
The connectors context will spawn here, if it's not alife anymore.
The connector spawns (in most cases) a [Service][7], that may run in background without disturbing the user.
This service runs a [AsyncTask][8] to do the network IO.
At this point the API ends, this is where the `doBootstrap()`, `doUpdate()` or `doSend()` methods are called.

As you might have seen, there are some more methods you could overwrite:
 * `initSpec()` is run once to initialisize some strings and stuff on spawning.
 * `getSpec()` is run, everytime it is needed. The Connector context will cache the actual instance.

On startup, WebSMS will check, if the number of installed connectors has changed.
If not: it will use the cached instances of all installed connectors.
If it changed: it will clear the cache and run a broadcast (info request) to all connectors.
They will spawn, run `initSpec()` and `getSpec()` and send back the instance to WebSMS. That's why no IO should be done in either of these methods.

## FAQ 

Please have a look on the [DeveloperFAQ][2].

[1]: https://github.com/felixb/
[2]: https://github.com/felixb/websms-sms/blob/master/doc/DeveloperFAQ.md
[3]: https://github.com/felixb/websms-api/blob/master/WebSMSAPI/src/main/java/de/ub0r/android/websms/connector/common/Connector.java
[4]: https://github.com/felixb/websms-api/blob/master/WebSMSAPI/src/main/java/de/ub0r/android/websms/connector/common/BasicConnector.java
[5]: https://raw.githubusercontent.com/felixb/websms-api/master/doc/sequence.png
[6]: http://developer.android.com/reference/android/content/BroadcastReceiver.html
[7]: http://developer.android.com/reference/android/app/Service.html
[8]: http://developer.android.com/reference/android/os/AsyncTask.html
