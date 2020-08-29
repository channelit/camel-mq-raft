### Kube Commands

```
-- Create Docker Image
cd ..
docker build -t backend-app .

-- Create Config Map
kubectl create configmap local-config --from-file=configs/

-- Check Config Map
kubectl get configmaps local-config -o yaml

-- Postgres
kubectl create namespace postgres
kubectl get pods -n postgres
 kubectl logs -f postgres-776768fc7f-zc6x9 -n postgres
-- ActiveMQ
kubectl create namespace activemq


```

#### Kubernetes Dashboared
```shell script
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.0.0/aio/deploy/recommended.yaml
kubectl proxy
```
Dashboard URL: http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/.
Get Token
```shell script
kubectl -n kube-system describe secret $(kubectl -n kube-system get secret | awk '/^deployment-controller-token-/{print $1}') | awk '$1=="token:"{print $2}'
```