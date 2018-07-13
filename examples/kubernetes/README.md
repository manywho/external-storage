Kubernetes
==========

This folder contains a few example configurations to run the service using Kubernetes, with health checking and a
test-ready database.

> **Note:** These configurations aren't production-ready, and should be modified to fit your organization's production
requirements! 

### Prerequisites

You'll need to have a Kubernetes cluster, running v1.10.0+, with an ingress controller and a default persistent volume
provisioner configured. You can also run these configurations against a [`minikube`](https://github.com/kubernetes/minikube)
cluster.

### Usage

The example configurations will create a new namespace called `boomi-flow` and deploy some pods running the service,
along with a statefulset running PostgreSQL. The PostgreSQL configurations aren't suitable for production, as they don't
provide any HA capability, but the service configurations can be used in a production environment.

1. Apply the configurations against your cluster using `kubectl`:

    ```bash
    $ kubectl apply -f examples/kubernetes
    ```
