#!/bin/bash
kubectl delete -f zipkin-service.yaml
kubectl delete -f zipkin-deployment.yaml

kubectl delete -f company-bulletin-board-service.yaml 
kubectl delete -f company-bulletin-board-deployment.yaml 

#kubectl delete -f company-worker-service.yaml 
kubectl delete -f company-worker-deployment.yaml 
#kubectl delete -f company-doorman-service.yaml 
kubectl delete -f company-doorman-deployment.yaml 
#kubectl delete -f company-beekeeper-service.yaml 
kubectl delete -f company-beekeeper-deployment.yaml 

kubectl delete -f company-manager-service.yaml 
kubectl delete -f company-manager-deployment.yaml 
