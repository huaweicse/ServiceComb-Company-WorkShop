# Docker 1.11.2 Installation Guide

## Prerequisites
* 64 bit Linux OS(Ubuntu 14.04 or above is recommended)
* Linux kernel >= 3.10

## Installation Guide
### Ubuntu
1. Uninstall docker
  ```bash
  sudo apt-get remove docker docker-engine docker.io docker-ce
  sudo mv /var/lib/docker /var/lib/docker-old
  ```
2. Update package information and install necessary packages
  ```bash
  sudo apt-get update
  sudo apt-get install apt-transport-https ca-certificates
  ```
3. Add docker's official GPG key
  ```bash
  sudo apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D
  ```
4. Add docker's mirror source
  ```bash
  echo "deb https://apt.dockerproject.org/repo ubuntu-$(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/docker.list
  sudo apt-get update
  ```
5. Install docker
  ```bash
  sudo apt-get install docker-engine=1.11.2-0~$(lsb_release -cs)
  ```
6. Add user to docker group
  ```bash
  sudo usermod -aG docker $USER
  ```
7. Add Huawei's image repository
  ```bash
  echo DOCKER_OPTS="--insecure-registry registry.cn-north-1.hwclouds.com" | sudo tee -a /etc/default/docker 
  ```
8. Restart docker
  * Ubuntu 16.04 or above
  ```
  sudo systemctl restart docker
  ```
  * Ubuntu 14.04 or below
  ```
  sudo service docker restart
  ```

### Other Linux distribution
1. Make sure you don't have docker installed. If you do, remove it first.
2. Download and extract docker archive files
  ```bash
  wget https://get.docker.com/builds/Linux/x86_64/docker-1.11.2.tgz /tmp
  cd /tmp; tar xvf docker-1.11.2.tgz
  ```
3. Move docker executable files to system executable path
  ```bash
  sudo mv docker/* /usr/bin
  ```
4. Create docker directory
  ```bash
  sudo mkdir /var/lib/docker
  ```
5. Run the docker daemon
  ```bash
  sudo docker daemon --insecure-registry=registry.cn-north-1.hwclouds.com
  ```

The installation of docker has finished. You are free to use `docker` now.
