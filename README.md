General
=======

This is the API to build connectors for [websms](http://github.com/felixb/websms/).
Please see ome open source connector to get an idea how to use the API.
A very good example fot the use of BasicConnector with a very straight forward API is [cherry-sms](https://github.com/felixb/websms-connector-cherrysms/).
Check out the [o2 connector](https://github.com/lmb/websms-connector-o2/) to get an idea of a connector implementing the base class Connector and parsing a full website.

You will need this API to build websms [1] or any connector.

How to build
============

There are two ways to build a connector with this websms-api.

Android-Studio / gradle / maven
-------------------------------

Add the websms-api maven repository to your build.gradle and simply reference the remote dependency.
There is no need to check out/ clone the api manually.

    repositories {
        maven {
            url 'https://raw.githubusercontent.com/felixb/websms-api/mvn-repo/'
        }
        mavenCentral()
    }

    dependencies {
        compile 'de.ub0r.android.websms.connector.common:WebSMSAPI:1.1'
    }

You can see it in full details in the [o2 connector's code base](https://github.com/lmb/websms-connector-o2/).
In maven just in add the dependency like this.

    <dependency>
        <groupId>de.ub0r.android.websms.connector.common</groupId>
        <artifactId>WebSMSAPI</artifactId>
        <version>{latest.version}</version>
        <type>jar</type>
    </dependency>

Eclipse / ant
-------------

Please clone this git repo and check out the tag `build-env-eclipse`.
You'll need to import this eclipse project and reference it as library project in your connector.
[Cherry-sms](https://github.com/felixb/websms-connector-cherrysms/) is an example for this kind of configuration.
