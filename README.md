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
A `docker-compose.yaml` file is provided to start all services and their dependencies as docker containers.
1. Build all service images using command `mvn package -Pdocker`
1. Run all service images using command `docker-compose up`

If you are using [Docker Toolbox](https://www.docker.com/products/docker-toolbox), please add an extra profile `-Pdocker-machine`.

```mvn package -Pdocker -Pdocker-machine```

## Run Integration Tests

```
mvn verify -Pdocker -Pdocker-machine
```

## Verify services
You can verify the services using [curl][curl] by the following steps:
1. Retrieve manager's ip address
  * If you use docker compose:
    ```bash
    export IP="127.0.0.1"
    ```
  * If you use docker machine(supposed your docker machine name is `default`):
    ```
    export IP=$(docker-machine ip default)
    ```
2. Log in and retrieve token from `Authorization` section
```bash
curl -v -H "Content-Type: application/x-www-form-urlencoded" -d "username=jordan&password=password" -XPOST "http://$IP:8083/doorman/rest/login"
```
Then you can copy the token from the `Authorization` section and use it to replace the `Authorization` header in the following requests.
3. Get the sixth fibonacci number from the worker service
```bash
curl -H "Authorization: replace_with_the_authorization_token" -XGET "http://$IP:8083/worker/fibonacci/term?n=6"
```
4. Get the number of drone's ancestors at the 30th generation from the beekeeper service
```bash
curl -H "Authorization: replace_with_the_authorization_token" -XGET "http://$IP:8083/beekeeper/rest/drone/ancestors/30"
```
5. Get the number of queen's ancestors at the 30th generation from the beekeeper service
```bash
curl -H "Authorization: replace_with_the_authorization_token" -XGET "http://$IP:8083/beekeeper/rest/queen/ancestors/30"
```

[curl]: https://curl.haxx.se
