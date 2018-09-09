systemd
=======

This folder contains a few example configurations to run the service using `systemd`, with health checking and resource
restrictions.

### Prerequisites

It is assumed that a database already exists and is running, and the access details are known. It's also assumed `curl`
is installed, and also that a supported Java 10+ runtime is installed and available at `/usr/bin/java`.

### Usage

The unit itself is a normal service unit that supports having up to 10 instances running on the same machine, using the
usual `@` nomenclature.

1. Fill in the appropriate configuration values in the `external-storage` file
2. Copy it to `/etc/default/external-storage`
3. Compile and package the service ([instructions available in the main README.md](https://github.com/manywho/external-storage))
4. Copy the compiled package to `/opt/boomi/flow/external-storage.jar`
5. Copy `external-storage@.service` to your systemd unit folder (usually `/usr/lib/systemd/system`, or
`/lib/systemd/system` on Debian)
6. Enable and start the service (this example starts 2 instances, running on port 8080 and 8081):

  ```bash
  $ sudo systemctl enable external-storage@{8080,8081}
  $ sudo systemctl start external-storage@{8080,8081}
  ```
7. Configure your load balancer/ingress to proxy requests to the appropriate ports
