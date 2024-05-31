# APK Servlet Container

<img src="./forReadme/runningExample.png" style="max-width: 320px" />

## Useful commands
```sh
$ $ANDROID_HOME/platform-tools/adb push ./webapp-parent/web-app/target/web-app-1.0-SNAPSHOT.war /storage/emulated/0/jetty/webapps/

$ $ANDROID_HOME/build-tools/34.0.0/dexdump -d ./classes.dex

# rebuild and deploy (requires manual restart)
$ mvnw -f $PROJECT_DIR/webapp-parent/pom.xml clean package && $ANDROID_HOME/platform-tools/adb push $PROJECT_DIR/webapp-parent/web-app/target/web-app-1.0-SNAPSHOT-dexed.war /storage/emulated/0/jetty/webapps/

# pair device through wifi
$ $ANDROID_HOME/platform-tools/adb pair host:port
```

## `obfuscate` and `deobfuscate` jetty passwords
```java
import org.eclipse.jetty.util.security.Password;

String obfuscatedPassword = "OBF:1u2u1wml1z7s1z7a1wnl1u2g";
Password.obfuscate("test");
Password.deobfuscate(obfuscatedPassword);
```

## Generate keystore for https
```sh
# Generate keystore.p12
$ keytool -genkeypair -alias mykey -keyalg RSA -keysize 2048 -keystore keystore.p12 -storetype PKCS12 -storepass keystore_password -keypass keystore_password -dname "CN=localhost,OU=Unknown,O=Unknown,L=Unknown,ST=Unknown,C=Unknown"

# -keypass = setKeyManagerPassword
# -storepass = setKeyStorePassword

# Export certificate from keystore to certfile.crt
$ keytool -exportcert -alias mykey -keystore keystore.p12 -file certfile.crt -storetype PKCS12 -storepass keystore_password

# Generate truststore.p12
$ keytool -importcert -alias mykey -file certfile.crt -keystore truststore.p12 -storetype PKCS12 -storepass truststore_password -noprompt
```
### Usage in java
```java
SslContextFactory sslContextFactory = new SslContextFactory();
sslContextFactory.setKeyStorePath("keystore.p12");
sslContextFactory.setTrustStorePath("truststore.p12");
sslContextFactory.setKeyManagerPassword("keystore_password");
sslContextFactory.setKeyStorePassword("keystore_password");
sslContextFactory.setTrustStorePassword("truststore_password");
```

## References:
- https://github.com/jetty-project/i-jetty
- https://github.com/jretty-org/jretty-server