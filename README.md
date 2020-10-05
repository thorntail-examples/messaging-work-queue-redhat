# Thorntail Messaging Work Queue Example

## Purpose

This example demonstrates how to dispatch tasks to a scalable set of worker processes using a message queue.
It uses the AMQP 1.0 message protocol to send and receive messages.

## Prerequisites

* Log into an OpenShift cluster of your choice: `oc login ...`.
* Select a project in which the services will be deployed: `oc project ...`.

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

### Deployment with the JKube Maven Plugin

```bash
oc apply -f ./frontend/.openshiftio/service.amq.yaml

mvn clean oc:deploy -Popenshift
```

## Test everything

This is completely self-contained and doesn't require the application to be deployed in advance.
Note that this may delete anything and everything in the OpenShift project.

```bash
mvn clean verify -Popenshift,openshift-it
```
