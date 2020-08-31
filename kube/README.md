### Kube Commands

```
-- Create Docker Image
cd ..
docker build -t backend-app .

-- Create Config Map
kubectl create configmap local-config --from-env-file=configs/app.properties -n server
kubectl get configmap local-config -n server -o yaml
-- Check Config Map
kubectl get configmap local-config -o yaml -n server

-- Postgres
kubectl create namespace postgres
kubectl apply -f kube_pg.yaml -n postgres
kubectl get pods -n postgres
 kubectl logs -f postgres-776768fc7f-ctj7c -n postgres

-- ActiveMQ
kubectl create namespace activemq
kubectl apply -f kube_amq.yml -n activemq
kubectl get pods -n activemq
  kubectl logs -f activemq-7c6c9fdd8b-7k99b -n activemq
kubectl port-forward svc/activemq 8162:8161 -n activemq

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

Expose commands
```
kubectl expose deployment postgres --port=5432 --target-port=5432 -n postgres
kubectl expose deployment activemq --port=8161 --target-port=8161 -n activemq
```

Cleanup
```
kubectl delete deployment activemq -n activemq
kubectl delete service activemq -n activemq
kubectl delete deployment postgres -n postgres
kubectl delete service postgres -n postgres
```