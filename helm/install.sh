#!/bin/bash
set -euo pipefail

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR:${NC} $1" >&2
}

trap 'error "An unexpected error occurred. Exiting..."; exit 1' ERR

check_command() {
    local cmd="$1"
    local install_instructions="$2"
    if ! command -v "$cmd" &>/dev/null; then
        error "$cmd is not installed. $install_instructions"
        exit 1
    fi
}

check_kubectl_cluster() {
    if ! kubectl cluster-info &>/dev/null; then
        error "kubectl is not connected to a cluster. Please check your kubeconfig."
        exit 1
    fi
}

deploy_chart() {
    local chart_name="$1"
    local chart_dir="$2"
    local namespace="$3"
    local update_dependencies="${4:-true}"

    log "Starting ${chart_name} installation..."

    if [ "$update_dependencies" = "true" ]; then
        log "Updating Helm dependencies for ${chart_name}..."
        helm dependency update "$chart_dir" || { error "Failed to update ${chart_name} dependencies"; exit 1; }
    fi

    log "Installing ${chart_name}..."
    helm upgrade --install "$chart_name" "$chart_dir" -n "$namespace" --create-namespace || { error "Failed to install ${chart_name}"; exit 1; }

    log "${chart_name} installation completed successfully."
}

main() {
    log "Starting installation process..."

    check_command "helm" "Please install Helm from https://helm.sh/"
    check_command "kubectl" "Please install kubectl from https://kubernetes.io/docs/tasks/tools/"
    check_kubectl_cluster

    deploy_chart "utils" "./utils" "utils"
    deploy_chart "pulsar" "./pulsar" "pulsar"
    deploy_chart "clickhouse" "./clickhouse" "clickhouse"
    deploy_chart "iot-platform" "./iot-platform" "iot-platform" false

    log "All installations completed successfully!"
}

main
