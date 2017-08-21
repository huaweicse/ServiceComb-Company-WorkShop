# Run Company on Kubernetes Cluster
These scripts show you how to run Company on kubernetes cluster.

## Before you begin

Scripts are tested with kubernetes 1.7+ and docker 17.06.0-ce

## Start up 
```
git clone https://github.com/ServiceComb/ServiceComb-Company-WorkShop.git
cd ServiceComb-Company-WorkShop/kubernetes/
bash start.sh
```
## Visit company service

*  **Visit Inside the cluster**
```
# get CLUSTER-IP and TARGET-PORT with api gateway
  kubectl get svc |grep company-manager

# export HOST variable
  export HOST="<CLUSTER-IP>:<TARGET-PORT>"

# verify login and retrive token from Authorization section
  curl -v -H "Content-Type: application/x-www-form-urlencoded" -d "username=jordan&password=password" -XPOST "http://$HOST/doorman/rest/login"
```
***Reference to [Verify services](../README.md) to get more to visit company service***

* **Visit outside the cluster**

Modified the HOST variable to use EXTERNAL-IP and NODE-PORT
```
# get EXTERNAL-IP and NODE-PORT with api gateway
  kubectl get svc company-manager -o yaml | grep ExternalIP -C 1
  kubectl get svc company-manager -o yaml | grep nodePort -C 1

# export HOST variable
  export HOST="<EXTERNAL-IP>:<NODE-PORT>"
```
***Notes:***
*If no free external-ip can be reserved by kubernetes, try to use the public-ip with the node.*
```
# get NODE-NAME of the node which api gateway is runing on
  kubectl get po -owide |grep company-manager
# get PUBLIC-IP which of the node
  kubectl describe  node <NODE-NAME> |grep public-ip
# set HOST variable
  export HOST="<PUBLIC-IP>:<NODE-PORT>"
```

## Clean up
```
bash cleanup.sh
```
