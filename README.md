Boomi Flow External Storage
===========================

> This application is currently in-development, and its API could change without warning!

This application is the default implementation of the Boomi Flow External Storage API. It aims to serve the 90% use case
for consumers of the API, so you can run it as-is, or customize it to fit your requirements if there's something missing.

## Usage

### Configuring

The available configuration settings for the application are:

* **DATABASE_URL:** A JDBC connection string that points to where data should be stored, e.g. `jdbc:postgresql://localhost/flow`
* **DATABASE_USERNAME** The username to use when connecting to the database
* **DATABASE_PASSWORD** The password to use when connecting to the database

> **Note:** Database migrations are run automatically on startup, so the schema should always be up-to-date

#### Environment Variables

You will have to configure the application at runtime by using environment variables. Here's an example that runs the
application from the command line:

```bash
$ DATABASE_URL=jdbc:postgresql://localhost/flow DATABASE_USERNAME=manywho DATABASE_PASSWORD=password java -jar target/external-storage.jar
```

### Building

To build the application, you will need to have Maven 3 and a Java 10+ implementation installed (OpenJDK and Oracle Java
SE are both supported).

Now you can build the runnable shaded JAR:

```bash
$ mvn clean package
```

### Running

The application is a RestEASY JAX-RS application, that by default is run under the Jetty server on port 8080 (if you
use the packaged JAR).

#### Defaults

Running the following command will start the service listening on `0.0.0.0:8080`:

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

* Support for databases out of the box, other than PostgreSQL
* Example deployments, e.g. Heroku, Kubernetes, systemd
* Tests, including Heroku

## License

This application is released under the [MIT License](https://opensource.org/licenses/MIT).
