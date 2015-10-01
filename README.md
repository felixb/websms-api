# General

This is the API to build connectors for [websms][1].
Please see one of the open source connectors to get an idea how to use the API.
A very good example fot the use of BasicConnector with a very straight forward API is [cherry-sms][2].
Check out the [o2 connector][3] to get an idea of a connector implementing the base class Connector and parsing a full website.

## How to build

There are two ways to build a connector with this websms-api.

### Android-Studio / gradle / maven

Add the websms-api maven repository to your build.gradle and simply reference the remote dependency.
There is no need to check out/ clone the api manually.

    repositories {
        maven {
            url 'https://raw.githubusercontent.com/felixb/mvn-repo/master/'
        }
        mavenCentral()
    }

    dependencies {
        compile 'de.ub0r.android.websms.connector.common:WebSMSAPI:1.1'
    }

You can see it in full details in the [cherry-sms connector's code base][2].
In maven just in add the dependency like this.

    <dependency>
        <groupId>de.ub0r.android.websms.connector.common</groupId>
        <artifactId>WebSMSAPI</artifactId>
        <version>{latest.version}</version>
        <type>jar</type>
    </dependency>

### Eclipse / ant

Please clone this git repo and check out the tag `build-env-eclipse`.
You'll need to import this eclipse project and reference it as library project in your connector.

## Further reading

 * [Developer Help][4]
 * [Developer FAQ][5]

[1]: http://github.com/felixb/websms/
[2]: https://github.com/felixb/websms-connector-cherrysms/
[3]: https://github.com/lmb/websms-connector-o2/
[4]: https://github.com/felixb/websms-api/blob/master/doc/DeveloperHelp.md
[5]: https://github.com/felixb/websms-api/blob/master/doc/DeveloperFAQ.md
