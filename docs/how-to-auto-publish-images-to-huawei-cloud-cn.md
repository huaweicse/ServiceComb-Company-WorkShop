# 自动编译构建并发布镜像到华为镜像仓库

## 环境准备
1. Linux(推荐使用Ubuntu)
2. [Docker 1.11.2][docker_install_guide]
3. [Maven 3.x][maven]
4. [Oracle JDK 1.8+][jdk]

## 自动构建并发布镜像
1. 获取 workshop demo 的代码
  ```bash
  git clone https://github.com/ServiceComb/LinuxCon-Beijing-WorkShop.git
  ```
2. 配置脚本 `scripts/publish_images_to_huaweicloud.sh` 中的环境变量  

在你使用脚本前，你需要对以下变量进行更改：

| 变量名称            | 描述               |
| --------------- | ---------------- |
| TARGET\_VERSION | 在华为镜像仓库发布的镜像版本号 |
| TENANT\_NAME    | 在图1中的租户名         |
| REPO\_ADDRESS   | 在图1中的镜像仓库地址      |
| USER\_NAME      | 在图1中的用户名         |
| PW              | 在图1中的密码          |

图1可以通过以下步骤来获取：

(1) 访问 [华为镜像仓库][image_warehouse] 

(2) 点击 *Pull/Push指南*

(3) 点击 *生成docker login指令*

![图1. 变量信息][variables_information]  

图1. 变量信息

[variables_information]: images/variables_information_cn.png
3. 通过执行以下命令来运行脚本
  ```bash
  bash scripts/publish_images_to_huaweicloud.sh
  ```
  稍等片刻直至屏幕输出*Done*的信息，然后就可以在 [华为镜像仓库][image_warehouse] 中看到刚才上传的镜像了。

[docker_install_guide]: how-to-install-docker_cn.md
[maven]: https://maven.apache.org/install.html
[jdk]: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
[image_warehouse]: https://servicestage.hwclouds.com/servicestage/#/stage/softRepository/mirrorCenter/myMirrorPack
