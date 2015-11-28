# JCA 1.6 Connector for the Apple Push Notification Service

This adapter can be plugged into a Java EE application server, so that applications can send push messages to
iOS, Apple Watch and OS X devices. It has been written for Java EE 7 compliant application servers running on Java 8.

## Usage

Clone this Github project, and install it in your local Maven directory:

```
mvn install
```

Then, add this dependency to your project:

```
<dependency>
    <groupId>nl.meandi</groupId>
    <artifactId>apns-connector-jar</artifactId>
    <version>0.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

In order to send a push notification message to your app, inject the APNS resource in your Java EE component:

```
@Resource(lookup = "java:/eis/apns-connector")
private PushNotificationConnectionFactory connectionFactory;
```

Sending the message goes like this:

```
try (PushNotificationConnection conn = connectionFactory.getConnection()) {
    byte[] deviceTokenBytes = /* array with 32 bytes */;
    byte[] payload = /* payload in JSON format */;
    int notificationIdentifier = /* unique identification of this push notification */;
    Instant expirationDate = /* date/time indicating when the message expires */;
    NotificationPriority priority = /* TIME (deliver as soon as possible) or ENERGY (minimize power consumption) */;

    conn.sendNotification(deviceTokenBytes, payload, notificationIdentifier, expirationDate, priority);
} catch (Exception e) {
    throw new RuntimeException(e);
}
```

In addition, it is required to declare a message driven bean for processing feedback information from Apple:

```
@MessageDriven
public class MessageDrivenBean implements ApnsListener {
    ...
}
```

The feedback is about devices that failed to consume push notifications. The application should cease the
notifications for those devices. The message driven bean should implement the handleDeviceRemoval method of
the ApnsListener interface:

```
@Override
public void handleDeviceRemoval(Instant timestamp, byte[] deviceId) {
    /* Remove notification subscriptions for the device identified by deviceId. */;
}
```

## Configuration and Deployment

After the Maven build, the resource adapter can be found here:

```
apns-connector-rar/target/apns-connector-rar-0.0-SNAPSHOT.rar
```

It depends on the Java EE server how this resource adapter needs to be deployed, configured, and how both
application components (the notification sender and the feedback receiver) need to be bound to the adapter.

For the notification sender part, this connection factory needs to be bound to a JNDI name:

```
nl.meandi.apns.ManagedConnectionFactoryImpl
```

In addition, the connection factory needs to be configured with these two properties:

- certificateFileName: the name of the file that contains the APNS certificate
- certificateFilePassword: the password of the certificate file

For the feedback part, the message driven bean needs to be configured, by setting the activationConfig
element of the @MessageDrivenBean annotation, and specifying the same two certificate properties that were just
mentioned for the notification sender.

The adapter will automatically derive from the certificate whether it needs to connect to the sandbox
environment of Apple, or to the production environment. It is possible to configure multiple pairs of connection
factories and message driven beans, if you need to handle multiple iOS apps (that each require a certificate
of their own).

## Sample application

A sample application has been included in this project. It's a JSF application that runs inside a Wildfly 9
application server. In order to run it, you first need to build the docker images, in a shell that has
a Docker engine configured:

```
mvn install -Pdocker
```

After that, run this script to start the application:

```
etc/start-example.sh <certificate_file_name> <certificate_file_password>
```

The web application will be available at this URL:

```
<docker_host>:8080/apns-connector-war-0.0-SNAPSHOT
```
