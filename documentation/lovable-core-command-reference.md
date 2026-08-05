# Lovable Core — PowerShell & Kubernetes Command Reference

> **Purpose:** Quick-reference commands used while running and debugging Lovable Core on GKE/Kubernetes.
>
> **Important:** Secrets/API keys from the original PowerShell history have intentionally been replaced with placeholders. Do **not** store real API keys in this file.

---

## 1. PowerShell Basics

### Show current PowerShell session history

```powershell
Get-History
```

### Show only the command text

```powershell
(Get-History).CommandLine
```

### Show last 20 commands

```powershell
Get-History | Select-Object -Last 20
```

### Clear the terminal

```powershell
cls
```

### List files

```powershell
ls
```

### Check a local file

```powershell
Get-Item .\common-lib-k8s.jar
```

### Change directory

```powershell
cd D:\GitHub\lovable-core\k8s\infra
```

```powershell
cd D:\GitHub\lovable-core\k8s\stateful
```

```powershell
cd D:\GitHub\lovable-core\k8s\services
```

```powershell
cd ..
```

---

# 2. Google Cloud CLI (GCloud)

## Check installation

```powershell
gcloud --version
```

## Check authenticated accounts

```powershell
gcloud auth list
```

## Check current gcloud configuration

```powershell
gcloud config list
```

## Install kubectl

```powershell
gcloud components install kubectl
```

## Configure kubectl for GKE cluster

```powershell
gcloud container clusters get-credentials lovable-cluster --region asia-south1
```

---

# 3. kubectl Setup

## Check kubectl client

```powershell
kubectl version --client
```

## Check GKE authentication plugin

```powershell
gke-gcloud-auth-plugin --version
```

## Create shortcut for kubectl

```powershell
Set-Alias k kubectl
```

After this, instead of:

```powershell
kubectl get pods
```

you can use:

```powershell
k get pods
```

---

# 4. Kubernetes Cluster / Namespace

## Get cluster nodes

```powershell
kubectl get nodes
```

## List namespaces

```powershell
k get namespaces
```

## Set current namespace to lovable-core

```powershell
kubectl config set-context --current --namespace=lovable-core
```

## Get pods in current namespace

```powershell
k get pods
```

## Get pods with detailed node/IP information

```powershell
kubectl -n lovable-core get pods -o wide
```

## Get pods in a specific namespace

```powershell
k get pods -n lovable-preview
```

---

# 5. Apply Kubernetes YAML Files

## Create namespaces

```powershell
k apply -f namespaces.yaml
```

## Apply all service manifests

```powershell
k apply -f services/
```

## Apply individual service

```powershell
k apply -f account-service.yaml
```

## Apply stateful infrastructure

```powershell
k apply -f kafka.yaml
```

```powershell
k apply -f minio.yaml
```

```powershell
k apply -f pgvector.yaml
```

```powershell
k apply -f redis.yaml
```

## Apply config service

```powershell
k apply -f config-service.yaml
```

## Apply runner pool

```powershell
k apply -f runner-pool.yaml
```

## Apply ingress

```powershell
k apply -f ingress.yaml
```

---

# 6. Kubernetes Pods

## List pods

```powershell
k get pods
```

```powershell
k get pods -n lovable-preview
```

## List pods by label

```powershell
kubectl get pods -n lovable-core -l app=account-service
```

## Get pod details

```powershell
k describe pod <pod-name>
```

Example:

```powershell
k describe pod account-service-xxxxxxxxxx-xxxxx
```

## Describe pod using a label

```powershell
kubectl describe pod -n lovable-core -l app=account-service
```

## Get all namespaces and ingress-related resources

```powershell
kubectl get ingress -A
```

---

# 7. Pod Logs

## View logs

```powershell
k logs <pod-name>
```

Example:

```powershell
k logs account-service-xxxxxxxxxx-xxxxx
```

## Logs for a service pod

```powershell
k logs intelligence-service-xxxxxxxxxx-xxxxx
```

```powershell
k logs workspace-service-xxxxxxxxxx-xxxxx
```

```powershell
k logs api-gateway-xxxxxxxxxx-xxxxx
```

```powershell
k logs reverse-proxy-xxxxxxxxxx-xxxxx
```

## Follow logs

```powershell
k logs -f <pod-name>
```

> Pod names change after a restart/redeployment. Use `k get pods` first and copy the current pod name.

---

# 8. Restart / Delete Pods

## Delete a pod

```powershell
k delete pod <pod-name>
```

Example:

```powershell
k delete pod workspace-service-xxxxxxxxxx-xxxxx
```

The Deployment normally creates a replacement pod automatically.

## Restart a Deployment

```powershell
kubectl rollout restart deployment/<deployment-name> -n lovable-core
```

Examples:

```powershell
kubectl rollout restart deployment/account-service -n lovable-core
```

```powershell
kubectl rollout restart deployment/api-gateway -n lovable-core
```

```powershell
kubectl rollout restart deployment/workspace-service -n lovable-core
```

```powershell
kubectl -n lovable-core rollout restart deployment/reverse-proxy
```

---

# 9. Deployment Management

## Delete a Deployment

```powershell
k delete deployment <deployment-name>
```

Example:

```powershell
k delete deployment account-service
```

## Apply Deployment again

```powershell
k apply -f account-service.yaml
```

## Get deployments

```powershell
kubectl get deployments -n lovable-core
```

---

# 10. Update Docker Image of a Deployment

## Update image using `latest`

```powershell
kubectl set image deployment/account-service -n lovable-core account-service=nagrajh/lovable-account-service:latest
```

## Update image using immutable digest

```powershell
kubectl set image deployment/account-service -n lovable-core account-service=nagrajh/lovable-account-service@sha256:<IMAGE_DIGEST>
```

### Workspace service

```powershell
kubectl set image deployment/workspace-service -n lovable-core workspace-service=nagrajh/lovable-workspace-service@sha256:<IMAGE_DIGEST>
```

### Intelligence service

```powershell
kubectl set image deployment/intelligence-service -n lovable-core intelligence-service=nagrajh/lovable-intelligence-service@sha256:<IMAGE_DIGEST>
```

---

# 11. Kubernetes Secrets

## Create secret from `.env`

```powershell
kubectl create secret generic app-secrets --from-env-file=.env -n lovable-core
```

## Recommended: Create/update secret without deleting it first

```powershell
kubectl create secret generic app-secrets --from-env-file=.env --dry-run=client -o yaml | kubectl apply -n lovable-core -f -
```

## List secrets

```powershell
kubectl get secret app-secrets -n lovable-core
```

## Delete secret

```powershell
k delete secret app-secrets
```

---

# 12. Read a Specific Secret

## Get encoded value

```powershell
kubectl -n lovable-core get secret app-secrets -o jsonpath="{.data.JWT_SECRET}"
```

## Decode Base64 value in PowerShell

```powershell
kubectl -n lovable-core get secret app-secrets -o jsonpath="{.data.JWT_SECRET}" | %{ [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($_)) }
```

> Use this carefully. Decoded secrets will be visible in the terminal.

---

# 13. Check Environment Variables Inside Pods

## Check JWT_SECRET

```powershell
kubectl -n lovable-core exec deploy/account-service -- printenv JWT_SECRET
```

```powershell
kubectl -n lovable-core exec deploy/api-gateway -- printenv JWT_SECRET
```

```powershell
kubectl -n lovable-core exec deploy/workspace-service -- printenv JWT_SECRET
```

## Check active Spring profile

```powershell
kubectl -n lovable-core exec deploy/api-gateway -- printenv SPRING_PROFILES_ACTIVE
```

```powershell
kubectl -n lovable-core exec deploy/account-service -- printenv SPRING_PROFILES_ACTIVE
```

---

# 14. Execute Commands Inside a Pod

## Find JAR files

```powershell
kubectl exec -n lovable-core <pod-name> -- find / -name "*.jar" 2>/dev/null
```

PowerShell-friendly version:

```powershell
kubectl exec -n lovable-core <pod-name> -- find / -name "*.jar" 2>$null
```

## Inspect JAR contents

```powershell
kubectl exec -n lovable-core <pod-name> -- sh -c "jar tf /app/account-service.jar | grep common-lib"
```

## Check a specific library JAR

```powershell
kubectl exec -n lovable-core <pod-name> -- sh -c "jar tf /app/libs/common-lib-1.0.0.jar | grep AuthUtil"
```

---

# 15. Copy Files From Pod to Local Machine

## Copy JAR from pod

```powershell
kubectl cp lovable-core/<pod-name>:/app/libs/common-lib-1.0.0.jar .\common-lib-k8s.jar
```

## Verify copied file

```powershell
Get-Item .\common-lib-k8s.jar
```

---

# 16. Resource Monitoring

## Check node resource usage

```powershell
kubectl top nodes
```

---

# 17. Ingress-NGINX Installation

## Install ingress-nginx

```powershell
kubectl apply -f "https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller/v1.8.2/deploy/static/provider/cloud/deploy.yaml"
```

> The original history contains several attempts with different URLs. The command above is the clean version that was ultimately used.

## Check ingress controller service

```powershell
kubectl get svc ingress-nginx-controller -n ingress-nginx -w
```

## Apply your ingress configuration

```powershell
k apply -f ingress.yaml
```

## Check all ingress resources

```powershell
kubectl get ingress -A
```

---

# 18. Port Forwarding

## MinIO

```powershell
kubectl port-forward -n lovable-core pod/minio-0 9000:9000
```

If using port 9001:

```powershell
kubectl port-forward -n lovable-core pod/minio-0 9001:9001
```

## PostgreSQL / PGVector

```powershell
kubectl port-forward -n lovable-core pod/pgvector-0 5432:5432
```

## Redis service

```powershell
kubectl port-forward svc/redis-service 6379:6379 -n lovable-core
```

Alternative service name:

```powershell
kubectl port-forward svc/redis 6379:6379 -n lovable-core
```

## Preview runner

```powershell
kubectl port-forward pod/<runner-pod-name> 5173:5173 -n lovable-preview
```

---

# 19. Preview Namespace

## Get preview pods

```powershell
k get pods -n lovable-preview
```

## Describe runner pod

```powershell
kubectl describe pod <runner-pod-name> -n lovable-preview
```

## Get container ports

```powershell
kubectl get pod <runner-pod-name> -n lovable-preview -o jsonpath="{.spec.containers[*].ports[*].containerPort}"
```

## Copy secret from lovable-core to lovable-preview

```powershell
kubectl get secret app-secrets -n lovable-core -o yaml | ForEach-Object { $_ -replace 'namespace: lovable-core','namespace: lovable-preview' } | kubectl apply -f -
```

## Verify preview secret

```powershell
kubectl get secret app-secrets -n lovable-preview
```

---

# 20. Scale Everything Down for Cost Saving

> Useful when you want to stop workloads temporarily without deleting Kubernetes manifests/images.

## Scale all Deployments in lovable-core to zero

```powershell
kubectl get deployments -n lovable-core -o name | ForEach-Object { kubectl scale $_ -n lovable-core --replicas=0 }
```

## Scale all StatefulSets in lovable-core to zero

```powershell
kubectl get statefulsets -n lovable-core -o name | ForEach-Object { kubectl scale $_ -n lovable-core --replicas=0 }
```

## Scale all Deployments in lovable-preview to zero

```powershell
kubectl get deployments -n lovable-preview -o name | ForEach-Object { kubectl scale $_ -n lovable-preview --replicas=0 }
```

## Scale all StatefulSets in lovable-preview to zero

```powershell
kubectl get statefulsets -n lovable-preview -o name | ForEach-Object { kubectl scale $_ -n lovable-preview --replicas=0 }
```

## Verify

```powershell
kubectl get pods -n lovable-core
```

```powershell
kubectl get pods -n lovable-preview
```

---

# 21. DNS / nip.io Testing

## Test localhost nip.io

```powershell
nslookup project-1.127.0.0.1.nip.io
```

## Test IP using hyphenated nip.io format

```powershell
nslookup project-1.127-0-0-1.nip.io
```

## Test public IP

```powershell
nslookup project-2.34.14.138.43.nip.io
```

```powershell
nslookup 34.14.138.43.nip.io
```

```powershell
nslookup project-2.34-14-138-43.nip.io
```

---

# 22. Common Troubleshooting Flow

When a service is not working, use this sequence:

### 1. Check pods

```powershell
k get pods
```

### 2. Check the specific pod

```powershell
k describe pod <pod-name>
```

### 3. Check logs

```powershell
k logs <pod-name>
```

### 4. Check environment variables

```powershell
kubectl -n lovable-core exec deploy/<deployment-name> -- printenv
```

### 5. Check deployment

```powershell
kubectl get deployment <deployment-name> -n lovable-core
```

### 6. Restart deployment

```powershell
kubectl rollout restart deployment/<deployment-name> -n lovable-core
```

### 7. Watch the new pod

```powershell
k get pods -w
```

---

# 23. Useful Daily Commands

These are the commands most likely to be needed repeatedly:

```powershell
k get pods
```

```powershell
k get pods -n lovable-preview
```

```powershell
k logs <pod-name>
```

```powershell
k describe pod <pod-name>
```

```powershell
k apply -f <file>.yaml
```

```powershell
kubectl rollout restart deployment/<deployment-name> -n lovable-core
```

```powershell
kubectl get ingress -A
```

```powershell
kubectl get svc -A
```

```powershell
kubectl get deployments -n lovable-core
```

```powershell
kubectl get statefulsets -n lovable-core
```

```powershell
kubectl top nodes
```

```powershell
k get pods -w
```

---

# 24. Quick Command Finder

| Need | Command |
|---|---|
| Show pods | `k get pods` |
| Show pods in preview | `k get pods -n lovable-preview` |
| Logs | `k logs <pod-name>` |
| Describe pod | `k describe pod <pod-name>` |
| Restart service | `kubectl rollout restart deployment/<name> -n lovable-core` |
| Apply YAML | `k apply -f <file>.yaml` |
| Apply all services | `k apply -f services/` |
| Get services | `kubectl get svc -A` |
| Get ingress | `kubectl get ingress -A` |
| Get deployments | `kubectl get deployments -n lovable-core` |
| Get secrets | `kubectl get secret -n lovable-core` |
| Update secret | `kubectl create secret generic app-secrets --from-env-file=.env --dry-run=client -o yaml \| kubectl apply -n lovable-core -f -` |
| Port-forward MinIO | `kubectl port-forward -n lovable-core pod/minio-0 9000:9000` |
| Port-forward PostgreSQL | `kubectl port-forward -n lovable-core pod/pgvector-0 5432:5432` |
| Port-forward Redis | `kubectl port-forward svc/redis-service 6379:6379 -n lovable-core` |
| Node resources | `kubectl top nodes` |
| Scale core down | `kubectl get deployments -n lovable-core -o name \| ForEach-Object { kubectl scale $_ -n lovable-core --replicas=0 }` |
| Set namespace | `kubectl config set-context --current --namespace=lovable-core` |
| Kubernetes version | `kubectl version --client` |
| GCloud version | `gcloud --version` |
| GCloud auth | `gcloud auth list` |
| GCloud config | `gcloud config list` |

---

# Notes

- Replace `<pod-name>` with the current pod name from `k get pods`.
- Replace `<deployment-name>` with the actual Deployment name.
- Replace `<IMAGE_DIGEST>` with the image digest from your registry.
- `k` is an alias for `kubectl`; it exists only in the PowerShell session where you created the alias.
- Port-forward commands occupy the terminal while running. Use another PowerShell window for other commands.
- Scaling workloads to zero does not delete the Deployment/StatefulSet definitions.
- Keep real `.env` files, JWT secrets, API keys, and tokens out of this reference document and out of Git.
