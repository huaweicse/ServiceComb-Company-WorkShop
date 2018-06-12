#!/bin/bash
## Make sure to run the script under project root folder.
version=$(printf 'VER\t${project.version}' | mvn help:evaluate | grep '^VER' | cut -f2)

if [ "${version}" == "" ]; then
	echo "Failed to get project version"
	exit 1
fi

echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
docker tag worker:${version} servicecomb/worker:${version}
docker push servicecomb/worker:${version}
docker tag beekeeper:${version} servicecomb/beekeeper:${version}
docker push servicecomb/beekeeper:${version}
docker tag doorman:${version} servicecomb/doorman:${version}
docker push servicecomb/doorman:${version}
docker tag manager:${version} servicecomb/manager:${version}
docker push servicecomb/manager:${version}
