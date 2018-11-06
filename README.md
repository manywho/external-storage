Boomi Flow External Storage [![Build Status](https://travis-ci.org/manywho/external-storage.svg?branch=master)](https://travis-ci.org/manywho/external-storage)
===========================

This application is the default implementation of the Boomi Flow External Storage API. It aims to serve the 90% use case
for consumers of the API, so you can run it as-is, or customize it to fit your requirements if there's something missing.

## Usage

Various example deployment strategies are provided in the [`examples`](examples) folder, and support the following, in
a test environment:

* Kubernetes
* systemd

The following instructions document how to run the service manually, and can be used to base your own deployment strategies
from, if they don't fit into the example ones we provide.

### Configuring

The available configuration settings for the application are:

* **DATABASE_URL:** A JDBC connection string that points to where data should be stored, e.g. `jdbc:postgresql://localhost/flow`
* **DATABASE_USERNAME** The username to use when connecting to the database
* **DATABASE_PASSWORD** The password to use when connecting to the database
* **RECEIVER_KEY** You can get this value from the Boomi Flow platform
* **PLATFORM_KEY** You can get this value from the Boomi Flow platform

It is also possible to configure a store to use multiple keys, and therefore accept data from multiple tenants. You can configure more than one key by adding multiple environment variables in the following format:

* **RECEIVER_KEY_xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx** You can get this value from the Boomi Flow platform
* **PLATFORM_KEY_xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx** You can get this value from the Boomi Flow platform

> **DATABASE_URL** examples
> * **mysql** jdbc:mysql://your-host.com/external_storage_db
> * **postgresql** jdbc:postgresql://your-host.com/external_storage_db
> * **sqlserver** jdbc:sqlserver://your-host.com;database=external_storage_db

> **Note:** Database migrations are run automatically on startup, so the schema should always be up-to-date

#### Environment Variables

You will have to configure the application at runtime by using environment variables. Here's an example that runs the
application from the command line:

```bash
$ DATABASE_URL=jdbc:postgresql://localhost/flow \
DATABASE_USERNAME=manywho \
DATABASE_PASSWORD=password \
RECEIVER_KEY=receiverkey \
PLATFORM_KEY=platformkey \
java -jar target/external-storage.jar
```

### Building

To build the application, you will need to have Maven 3 and a Java 10+ implementation installed (OpenJDK and Oracle Java
SE are both supported).

Now you can build the runnable shaded JAR:

```bash
$ mvn clean package
```

### Running

The application is a RestEASY JAX-RS application, that by default is run under the Undertow server on port 8080 (if you
use the packaged JAR).

The default path for this app is `api/storage/1`.

#### Defaults

Running the following command will start the service listening on `0.0.0.0:8080/api/storage/1`:

```bash
$ java -jar target/external-storage.jar
```

#### Heroku

The service is compatible with Heroku, and can be deployed by clicking the button below:

[![Deploy to Heroku](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy?template=https://github.com/manywho/external-storage)

## Contributing

Contributions are welcome to the project - whether they are feature requests, improvements or bug fixes! Refer to 
[CONTRIBUTING.md](CONTRIBUTING.md) for our contribution requirements.

## Roadmap

* Tests, including Heroku

## License

This application is released under the [MIT License](https://opensource.org/licenses/MIT).
