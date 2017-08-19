#!/bin/bash
# huawei service stage website :  https://servicestage.hwclouds.com/
# How to use(Linux):
# 1. Uncomment the variables and set their values
# 2. Execute: bash publish_images_to_huaweicloud.sh

# TENANT_NAME=xxxxxxxxxxx                                               # ---------huawei cloud tenant name.
# REPO_ADDRESS=registry.cn-north-1.hwclouds.com                         # ---------huawei cloud images repository address.
# USERNAME=xxxxx                                                        # ---------username: login huawei cloud images repository.
# PASSWORD=xxxxxxx                                                      # ---------password: login huawei cloud images repository.


which docker
if [ $? -ne 0 ]; then
    echo "no docker, please install docker 1.11.2."
    exit 1
fi

which mvn
if [ $? -ne 0 ]; then
    echo "no maven, please install maven."
    exit 1
fi


function isPropertySet () {
    if [ -z $2 ]; then
        echo "$1 is empty, please set it first"
        exit 1
    fi
}

function autoInferModules () {
    local ROOT=$1
    local ROOT_POM="$ROOT/pom.xml"
    local NORMAL_IFS=$IFS
    IFS=$'\n'
    all_possible_modules=$(grep "<module>" $ROOT_POM| grep -Ev "docker|test"| grep -o -P "(?<=module\>).*(?=\<)")
    IFS=$NORMAL_IFS
    for module in ${all_possible_modules[@]}; do
        local isValid=$(grep "docker-maven-plugin" $ROOT/$module/pom.xml)
        if [ ! -z $isValid ]; then
            modules+=($module)
        fi
    done
}

function incrementVersion () {
    local version=$1
    IFS=. read major minor patch <<< "${version##*-}"
    if [ $patch -eq 99 ]; then
        patch=0
        minor=$((minor + 1))
    else
        patch=$((patch + 1))
    fi
    if [ $minor -eq 100 ]; then
        minor=0
        major=$((major + 1))
    fi
    echo "$major.$minor.$patch"
}

properties=(TENANT_NAME REPO_ADDRESS USERNAME PASSWORD)
for property in ${properties[@]}; do
    isPropertySet $property ${!property}
done

OLD_VERSION=0.0.0 
TARGET_VERSION=$(incrementVersion $OLD_VERSION) # target version in huawei cloud images repository

ROOT_PATH=$(cd "$(dirname $0)/.."; pwd)
cd $ROOT_PATH
BUILD_VERSION=$(mvn help:evaluate -Dexpression=project.version | grep Building | awk '{print $4}')

declare -a modules
autoInferModules $ROOT_PATH

echo "Removing old docker images"
for module in ${modules[@]}; do
    image_id=$(docker images| grep $module| grep $BUILD_VERSION| awk '{print $3}')
    if [ ! -z $image_id ]; then
       docker rmi -f $image_id
    fi
done

echo "Generating new docker images"
mvn clean package -DskipTests -DskipITs -PHuaweiCloud -Pdocker

echo "Tagging image versions"
for module in ${modules[@]}; do
    docker tag $module:$BUILD_VERSION ${REPO_ADDRESS}/${TENANT_NAME}/workshop-$module:$TARGET_VERSION
done

zipkin_exists=$(docker images| grep "${REPO_ADDRESS}/${TENANT_NAME}/zipkin"| awk '{print $2}'| grep 1)
if [ -z $zipkin_exists ]; then
    docker pull openzipkin/zipkin:1
    docker tag openzipkin/zipkin:1 ${REPO_ADDRESS}/${TENANT_NAME}/zipkin:1
fi

docker login -u ${USERNAME} -p ${PASSWORD} ${REPO_ADDRESS}

echo "Pushing images to huawei docker repository"
for module in ${modules[@]}; do
    docker push ${REPO_ADDRESS}/${TENANT_NAME}/workshop-$module:$TARGET_VERSION
done
docker push ${REPO_ADDRESS}/${TENANT_NAME}/zipkin:1

# update version in script
SCRIPT_PATH=$ROOT_PATH/scripts/$(basename $0)
sed -i "s|$OLD_VERSION|$TARGET_VERSION|g" $SCRIPT_PATH

echo "Done"
