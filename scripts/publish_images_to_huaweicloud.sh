#!/bin/bash
# huawei service stage website :  https://servicestage.hwclouds.com/
# How to use(Linux):
# 1. Uncomment the variables and set their values, details can refer to https://github.com/ServiceComb/ServiceComb-Company-WorkShop/blob/master/docs/how-to-auto-publish-images-to-huawei-cloud.md
# 2. Execute: bash publish_images_to_huaweicloud.sh

# TENANT_NAME=xxxxxxxxxxx                                               # ---------huawei cloud tenant name.
# REPO_ADDRESS=registry.cn-north-1.hwclouds.com                         # ---------huawei cloud images repository address.
# USERNAME=xxxxx                                                        # ---------username to login huawei cloud images repository.
# PASSWORD=xxxxxxx                                                      # ---------password to login huawei cloud images repository.

# PROJECT_PATH=                                                         # ---------(optional) path to maven project.
THIRD_PARTY_IMAGES=(openzipkin/zipkin:1)				# ---------(optional) third party images that published on Docker Hub.


which docker > /dev/null
if [ $? -ne 0 ]; then
    echo "no docker, please install docker 1.11.2."
    exit 1
fi

which mvn > /dev/null
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
    if [ ${PREV_PROJECT_VERSION} != ${PROJECT_VERSION} ]; then
        BUILD_VERSION=0
    fi
    if [ -z "${PROJECT_VERSION##*SNAPSHOT*}" ]; then
        BUILD_VERSION=$(printf "%03d" $((10#${BUILD_VERSION} + 1)))
        TARGET_VERSION=$(printf "${PROJECT_VERSION}-build-%s" ${BUILD_VERSION})
    else
        if [ ${PREV_PROJECT_VERSION} == ${PROJECT_VERSION} ]; then
            echo "You have published version ${PROJECT_VERSION} before. Please update your pom version."
            exit 1
        fi
        TARGET_VERSION=${PROJECT_VERSION}
        BUILD_VERSION=0
    fi
}

properties=(TENANT_NAME REPO_ADDRESS USERNAME PASSWORD)
for property in ${properties[@]}; do
    isPropertySet $property ${!property}
done


if [ -z ${PROJECT_PATH} ]; then
    # set default project path to parent directory of the script's path
    PROJECT_PATH=$(cd "$dirname $0)/.."; pwd)
else
    PROJECT_PATH=$(cd ${PROJECT_PATH}; pwd)
fi
if [ ! -e "${PROJECT_PATH}/pom.xml" ]; then
    echo "Project path is invalid. Please specify a maven project path."
    exit 1
fi
cd ${PROJECT_PATH}
PROJECT_VERSION=$(mvn help:evaluate -Dexpression=project.version | grep Building | awk '{print $4}')

PREV_PROJECT_VERSION=0.0.0
BUILD_VERSION=0
TARGET_VERSION=
incrementVersion

declare -a modules
autoInferModules ${PROJECT_PATH}

echo "Removing old docker images"
for module in ${modules[@]}; do
    image_id=$(docker images| grep $module| grep $PROJECT_VERSION| awk '{print $3}'| uniq)
    if [ ! -z $image_id ]; then
       echo ${image_id} | xargs docker rmi -f
    fi
done

echo "Generating new docker images"
mvn clean package -DskipTests -DskipITs -PHuaweiCloud -Pdocker

echo "Tagging image versions"
for module in ${modules[@]}; do
    docker tag $module:$PROJECT_VERSION ${REPO_ADDRESS}/${TENANT_NAME}/$module:$TARGET_VERSION
done

VALID_THIRD_PARTY_IMAGES=()
for image in ${THIRD_PARTY_IMAGES[@]}; do
    IFS=: read imageName imageVersion <<< $image
    if [ -z ${imageVersion} ]; then
        imageVersion="latest"
    fi
    validImageName=$(cut -d "/" -f2 <<< ${imageName})
    VALID_IMAGE=${REPO_ADDRESS}/${TENANT_NAME}/${validImageName}
    image_exists=$(docker images| grep ${VALID_IMAGE}| awk '{print $2}'| grep ${imageVersion})
    if [ -z ${image_exists} ]; then
        docker pull ${imageName}:${imageVersion}
        docker tag ${imageName}:${imageVersion} ${VALID_IMAGE}:${imageVersion}
    fi
    VALID_THIRD_PARTY_IMAGES+=("${VALID_IMAGE}:${imageVersion}")
done

docker login -u ${USERNAME} -p ${PASSWORD} ${REPO_ADDRESS}

echo "Pushing images to huawei docker repository"
for module in ${modules[@]}; do
    docker push ${REPO_ADDRESS}/${TENANT_NAME}/$module:$TARGET_VERSION
done
for validImage in ${VALID_THIRD_PARTY_IMAGES}; do
    docker push ${validImage}
done

# update version in script
SCRIPT_PATH=$(cd "$(dirname $0)"; pwd)
sed -i "s|$PREV_PROJECT_VERSION|$PROJECT_VERSION|g" $SCRIPT_PATH
sed -i "s/^BUILD_VERSION=[[:digit:]]\+/BUILD_VERSION=$BUILD_VERSION/g" $SCRIPT_PATH

echo "Done"
