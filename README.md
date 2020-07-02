# Spark Java with pac4j Kerberos Authentication

## Requirements
The project requires that [Docker](https://www.docker.com/) is installed and it is also *recommended* to have [Postman](https://www.postman.com/) installed.

## Secure Transmission with HTTPS-Encryption
The Spark Java documentation provides good documentations for how to set up HTTPS over SSL/TLS: [http://sparkjava.com/documentation#embedded-web-server](http://sparkjava.com/documentation#embedded-web-server). It also references to the Oracle pages for how to create a KeyStore: [https://docs.oracle.com/cd/E19509-01/820-3503/ggfen/index.html](https://docs.oracle.com/cd/E19509-01/820-3503/ggfen/index.html).

In this project, the following has been done to achieve HTTPS encryption over SSL/TLS:
1. The `deploy` directory was created within `./code/deploy`.
2. The `keytool` was used to generate a new KeyStore: `keytool -genkey -keyalg RSA -alias keystore -keystore keystore.jks -validity 365 -keysize 2048`</br>> At the password prompt, the password was set to `secretkey`.</br>> For the "What is your first and last name?" prompt, enter the domain of the project. Since this is a local test project, just add `localhost`.</br>> Remaining fields should be filled out as you please and end it with `yes` at the last prompt if everything is correct.
3. The new `keystore.jks` can now be found at `.code/deploy/keystore.jks`.
4. The routes are then secured by adding `secure("deploy/keystore.jks", "secretkey", null, null);` to the `Main.java`. It is **important** to add this before any routes are defined.

All pages will now require `https://` to be used.

### Transmission over HTTP vs HTTPS

| HTTP | HTTPS |
| ---- | ----- |
| ![HTTP](https://github.com/FredrikBakken/sparkjava-pac4j-examples/blob/master/docs/assets/images/http.png?raw=true) | ![HTTPS](https://github.com/FredrikBakken/sparkjava-pac4j-examples/blob/master/docs/assets/images/https.png?raw=true) |


## Pac4j Kerberos

### Dependencies
The implementation of Kerberos authentication with pac4j requires that the dependency `pac4j-kerberos` is installed. `mockito-core` is used in this case for sandbox testing purposes.
