# IOT Platform

## Overview

This is a platform for managing IoT devices. It includes a producer and a processor.

## Prerequisites

- Scoop
- Kubernetes cluster
- Minikube
- ClickHouse operator

## Installation

1. Install minikube

```bash
scoop install minikube
```

2. Start minikube

```bash
minikube start
```

3. Install ingress-nginx

```bash
minikube addons enable ingress

```
4. Install ingress-nginx-controller

```bash
helm install ingress-nginx ingress-nginx/ingress-nginx --namespace ingress-nginx --create-namespace
```

5. Install ClickHouse 

```bash 
helm dependency update ./helm/clickhouse
helm install clickhouse ./helm/clickhouse -n clickhouse --create-namespace
```

6. Install iot-platform

```bash
helm install iot-platform ./helm/iot-platform -n iot-platform --create-namespace
```

7. Start minikube tunnel

```bash
minikube tunnel
```

8. Modify host file

```bash
127.0.0.1 iot-platform.local
```

9. Open browser and navigate to http://iot-platform.local
  - Login with username: admin and password: admin
  - Add new data connection
    - Name: ClickHouse
    - Host: clickhouse-single.clickhouse
    - Port: 8123
    - Protocol: HTTP
    - Username: default
    - Password: mysecurepassword



