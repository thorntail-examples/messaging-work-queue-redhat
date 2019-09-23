# Thorntail Messaging Work Queue Example

## Purpose

This example demonstrates how to dispatch tasks to a scalable set of worker processes using a message queue.
It uses the AMQP 1.0 message protocol to send and receive messages.

## Prerequisites

* The user has access to an OpenShift instance and is logged in.
* The user has selected a project in which the frontend and backend processes will be deployed.

## Modules

The `frontend` module serves the web interface and communicates with workers in the backend.

The `worker` module implements the worker service in the backend.

## Deployment

Run the following commands to configure and deploy the applications.

### Deployment using S2I

```bash
oc apply -f ./frontend/.openshiftio/service.amq.yaml

oc apply -f ./frontend/.openshiftio/application.yaml
oc new-app --template=thorntail-messaging-work-queue-frontend

oc apply -f ./worker/.openshiftio/application.yaml
oc new-app --template=thorntail-messaging-work-queue-worker
```

### Deployment with the Fabric8 Maven Plugin

```bash
oc apply -f ./frontend/.openshiftio/service.amq.yaml

cd frontend
mvn clean fabric8:deploy -Popenshift
cd ..

cd worker
mvn clean fabric8:deploy -Popenshift
cd ..
```
