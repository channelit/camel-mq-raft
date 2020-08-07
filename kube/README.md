### Kube Commands

```
-- Create Docker Image
cd ..
docker build -t backend-app .

-- Create Config Map
kubectl create configmap local-config --from-file=configs/

-- Check Config Map
kubectl get configmaps local-config -o yaml

```