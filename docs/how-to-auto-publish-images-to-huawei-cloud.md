# Auto build and publish images to Huawei Image Warehouse

## Prerequisites
1. Linux(Ubuntu is recommended)
2. [Docker 1.11.2][docker_install_guide]
3. [Maven 3.x][maven]
4. [Oracle JDK 1.8+][jdk]

## Auto build and publish images
1. Get the workshop demo's code
  ```bash
  git clone https://github.com/ServiceComb/LinuxCon-Beijing-WorkShop.git
  ```
2. Set up environment variables of `scripts/publish_images_to_huaweicloud.sh`.  

Before you use the script, there are several variables you need to set up first.  

| Name            | Description                                         |
|-----------------|-----------------------------------------------------|
| TARGET\_VERSION | image version publish in the Huawei Image Warehouse |
| TENANT\_NAME    | tenant name shown in fig.1                          |
| REPO\_ADDRESS   | the image repository address shown in fig.1         |
| USER\_NAME      | username shown in fig.1                             |
| PW              | password shown in fig.1                             |

The following figure(fig.1) can be retrieved by visiting [Huawei Image Warehouse][image_warehouse], and then click the *Pull/Push Guide* -> *generate docker login instruction*.

![fig.1 variables information][variables_information]
  
[variables_information]: images/variables_information.png
3. run the script by executing
  ```bash
  bash scripts/publish_images_to_huaweicloud.sh
  ```
  Wait for a while and then you can check your images in the [Huawei Image Warehouse][image_warehouse].

[docker_install_guide]: how-to-install-docker.md
[maven]: https://maven.apache.org/install.html
[jdk]: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
[image_warehouse]: https://servicestage.hwclouds.com/servicestage/#/stage/softRepository/mirrorCenter/myMirrorPack
