#!/bin/bash
kubectl expose -f zipkin-service.yaml
kubectl create -f zipkin-deployment.yaml

kubectl expose -f company-bulletin-board-service.yaml 
kubectl create -f company-bulletin-board-deployment.yaml 

#kubectl create -f company-worker-service.yaml 
kubectl create -f company-worker-deployment.yaml 
#kubectl create -f company-doorman-service.yaml 
kubectl create -f company-doorman-deployment.yaml 
#kubectl create -f company-beekeeper-service.yaml 
kubectl create -f company-beekeeper-deployment.yaml 

kubectl create -f company-manager-service.yaml 
kubectl create -f company-manager-deployment.yaml 
