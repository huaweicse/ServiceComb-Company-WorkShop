# ServiceComb Demo - Company [![Build Status](https://travis-ci.org/ServiceComb/LinuxCon-Beijing-WorkShop.svg?branch=master)](https://travis-ci.org/ServiceComb/LinuxCon-Beijing-WorkShop)[![Coverage Status](https://coveralls.io/repos/github/ServiceComb/LinuxCon-Beijing-WorkShop/badge.svg)](https://coveralls.io/github/ServiceComb/LinuxCon-Beijing-WorkShop)

## Purpose
In order for users to better understand how to develop microservices using ServiceComb, an easy to
understand demo is provided.

## Architecture of Company
* Manager (API gateway) 
* Doorman (authentication service)
* Worker (computing service)
* Beekeeper (computing service)
* Bulletin board (service registry)
* Project archive (request cache)
* Human resource (service governance)

Please read the [blog post](http://servicecomb.io/docs/linuxcon-workshop-demo/) on the detailed explanation of this project.

## Run Services
A `docker-compose.yaml` file is provided to start all services an their dependencies as docker containers.
1. Build all service images using command `mvn package -Pdocker`
1. Run all service images using command `docker-compose up`

If you are using [Docker Toolbox](https://www.docker.com/products/docker-toolbox), please add an extra profile `-Pdocker-machine`.

```mvn package -Pdocker -Pdocker-machine```

## Run Integration Tests

```
mvn verify -Pdocker -Pdocker-machine
```
