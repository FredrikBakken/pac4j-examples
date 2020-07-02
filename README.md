# Spark Java with pac4j Kerberos Authentication
The motivation behind this project is to create an open-source example for how to use the [pac4j-kerberos](https://www.pac4j.org/docs/clients/kerberos.html) library with the [Spark Java Framework](http://sparkjava.com/) (not to be confused with [Apache Spark](http://spark.apache.org/)), and then strengthening the security by adding HTTPS-encryption to all routes.

## Requirements
The project requires that [Docker](https://www.docker.com/) is installed and it is also *recommended* to have [Postman](https://www.postman.com/) installed for the testing.

## Secure Transmission with HTTPS-Encryption
The Spark Java documentation provides good documentations for how to set up HTTPS over SSL/TLS: [http://sparkjava.com/documentation#embedded-web-server](http://sparkjava.com/documentation#embedded-web-server). It also references to the Oracle pages for how to create a KeyStore: [https://docs.oracle.com/cd/E19509-01/820-3503/ggfen/index.html](https://docs.oracle.com/cd/E19509-01/820-3503/ggfen/index.html).

In this project, the following has been done to achieve HTTPS encryption over SSL/TLS:
1. The `deploy` directory was created within `./code/deploy`.
2. The `keytool` was used to generate a new KeyStore: `keytool -genkey -keyalg RSA -alias keystore -keystore keystore.jks -validity 365 -keysize 2048`</br>> At the password prompt, the password was set to `secretkey`.</br>> For the "What is your first and last name?" prompt, enter the domain of the project. Since this is a local test project, just add `localhost`.</br>> Remaining fields should be filled out as you please and end it with `yes` at the last prompt if everything is correct.
3. The new `keystore.jks` can now be found at `.code/deploy/keystore.jks`.
4. The routes are then secured by adding `secure("deploy/keystore.jks", "secretkey", null, null);` to the `Main.java`. It is **important** to add this before any routes are defined.

All pages will now require `https://` to be used.

You will still get a warning about there not being a secure connection, since the certificate is not signed by a trusted entity (CA). Further details describing this process can be found here: [https://support.code42.com/Administrator/6/Configuring/Install_a_CA-signed_SSL_certificate_for_HTTPS_console_access](https://support.code42.com/Administrator/6/Configuring/Install_a_CA-signed_SSL_certificate_for_HTTPS_console_access).

### Transmission over HTTP vs HTTPS

| HTTP | HTTPS |
| ---- | ----- |
| ![HTTP](https://github.com/FredrikBakken/sparkjava-pac4j-kerberos/blob/master/docs/assets/images/http.png?raw=true) | ![HTTPS](https://github.com/FredrikBakken/sparkjava-pac4j-kerberos/blob/master/docs/assets/images/https.png?raw=true) |


## Pac4j Kerberos

### Dependencies
The implementation of Kerberos authentication with pac4j requires that the dependency `pac4j-kerberos` is installed. `mockito-core` is used in this case for sandbox testing purposes.

### Authorization Implementation
The functionality implemented into this project was derived by combining the [KerberosClientTests.java](https://github.com/pac4j/pac4j/blob/master/pac4j-kerberos/src/test/java/org/pac4j/kerberos/client/direct/KerberosClientTests.java) file and the [spark-pac4j-demo](https://github.com/pac4j/spark-pac4j-demo) project. It can be studied in further detail under the [authorization](https://github.com/FredrikBakken/sparkjava-pac4j-kerberos/tree/master/code/src/main/java/com/bakkentechnologies/authorization) directory.

In order to success authenticate with Kerberos, the *header* needs to have an `Authorization` key-value pair defined as follows: `{ "Authorization": "Negotiate <KERBEROS TICKET>" }`

For this example, any base64-value for a `KERBEROS TICKET` will result in successful authentication. 

### Examples

#### DirectKerberosClient
| Unauthorized | Authorized |
| ------------ | ---------- |
| ![Unauthorized DirectKerberosClient](https://raw.githubusercontent.com/FredrikBakken/sparkjava-pac4j-kerberos/master/docs/assets/images/direct_unauthorized.png) | ![Authorized DirectKerberosClient](https://raw.githubusercontent.com/FredrikBakken/sparkjava-pac4j-kerberos/master/docs/assets/images/direct_authorized.png) |


#### IndirectKerberosClient
| Unauthorized | Authorized |
| ------------ | ---------- |
| ![Unauthorized IndirectKerberosClient](https://raw.githubusercontent.com/FredrikBakken/sparkjava-pac4j-kerberos/master/docs/assets/images/indirect_unauthorized.png) | ![Authorized IndirectKerberosClient](https://raw.githubusercontent.com/FredrikBakken/sparkjava-pac4j-kerberos/master/docs/assets/images/indirect_authorized.png) |

## Application Deployment
The current example is just a simple and local sandbox test, without any connection to a running KDC with validation against a Kerberos keytab file. In order to implement this functionality, the [ConfigurationFactory.java](https://github.com/FredrikBakken/sparkjava-pac4j-kerberos/blob/master/code/src/main/java/com/bakkentechnologies/authorization/ConfigurationFactory.java) (lines 32-42) has to be updated to use the `SunJaasKerberosTicketValidator`, as described in the [pac4j Kerberos documentation](https://www.pac4j.org/docs/clients/kerberos.html).
