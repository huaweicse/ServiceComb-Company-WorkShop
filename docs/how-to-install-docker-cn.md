# Docker 1.11.2 安装指南 

## 环境准备
* 64位Linux操作系统(推荐使用Ubuntu 14.04或以上版本)
* Linux 内核 >= 3.10

## 安装指南
### Ubuntu
1. 卸载系统中已安装的docker
  ```bash
  sudo apt-get remove docker docker-engine docker.io docker-ce
  sudo mv /var/lib/docker /var/lib/docker-old
  ```
2. 安装docker依赖的软件
  ```bash
  sudo apt-get update
  sudo apt-get install apt-transport-https ca-certificates
  ```
3. 添加docker官方的GPG密钥
  ```bash
  sudo apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D
  ```
4. 添加docker官方的镜像源
  ```bash
  echo "deb https://apt.dockerproject.org/repo ubuntu-$(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/docker.list
  sudo apt-get update
  ```
5. 安装docker
  ```bash
  sudo apt-get install docker-engine=1.11.2-0~$(lsb_release -cs)
  ```
6. 将用户添加至docker用户组
  ```bash
  sudo usermod -aG docker $USER
  ```
7. 添加华为镜像仓库配置
  ```bash
  echo DOCKER_OPTS="--insecure-registry registry.cn-north-1.hwclouds.com" | sudo tee -a /etc/default/docker 
  ```
8. 重启docker
  * Ubuntu 16.04或以上
  ```
  sudo systemctl restart docker
  ```
  * Ubuntu 14.04或以下
  ```
  sudo service docker restart
  ```

### 其它Linux发行版
1. 确保你没有安装docker，如果系统中安装了docker，请先卸载。
2. 下载并解压docker压缩包。
  ```bash
  wget https://get.docker.com/builds/Linux/x86_64/docker-1.11.2.tgz /tmp
  cd /tmp; tar xvf docker-1.11.2.tgz
  ```
3. 将docker可执行文件移至系统可执行文件目录
  ```bash
  sudo mv docker/* /usr/bin
  ```
4. 创建docker运行目录
  ```bash
  sudo mkdir /var/lib/docker
  ```
5. 运行docker后台进程
  ```bash
  sudo docker daemon --insecure-registry=registry.cn-north-1.hwclouds.com
  ```

docker的安装已完成，现在可以使用docker了。