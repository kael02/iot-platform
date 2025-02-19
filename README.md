# IOT Platform

## Overview

This is a platform for managing IoT devices. It includes a producer and a processor.

## Prerequisites

- Scoop
- Kubernetes cluster
- Minikube
- Helm

## Installation

1. Install minikube

```bash
scoop install minikube
```

2. Start minikube

```bash
minikube start
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
127.0.0.1 iot.example.com
```

7. Open browser and navigate to http://iot.example.com
  - Login with username: admin and password: admin
  - Add new data connection
    - Name: ClickHouse
    - Host: clickhouse-single.clickhouse
    - Port: 8123
    - Protocol: HTTP
    - Username: default
    - Password: mysecurepassword

# Optional installation

- [k9s](https://k9scli.io/)

# Links

- [minikube](https://minikube.sigs.k8s.io/docs/start/)
- [ingress-nginx](https://kubernetes.github.io/ingress-nginx/deploy/)
- [ClickHouse operator](https://github.com/Altinity/clickhouse-operator)
- [ClickHouse](https://clickhouse.com/)
- [helm](https://helm.sh/)
- [kubernetes-reflector](https://github.com/emberstack/kubernetes-reflector)


- [ClickHouse on kubernetes](https://blog.duyet.net/2024/03/clickhouse-on-kubernetes.html)
- [Pulsar on kubernetes](https://pulsar.apache.org/docs/4.0.x/getting-started-helm/)