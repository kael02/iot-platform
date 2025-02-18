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

4. Install helm charts
```bash
  ./helm/install.sh
```

5. Start minikube tunnel

```bash
minikube tunnel
```

6. Modify host file

```bash
127.0.0.1 iot-platform.local
```

7. Open browser and navigate to http://iot-platform.local
  - Login with username: admin and password: admin
  - Add new data connection
    - Name: ClickHouse
    - Host: clickhouse-single.clickhouse
    - Port: 8123
    - Protocol: HTTP
    - Username: default
    - Password: mysecurepassword



