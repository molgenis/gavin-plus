#!/bin/bash

ARTIFACT_ID=${1}
GROUP_ID=${2}
RELEASE_SCOPE=${3}
VERSION=$(mvn -Dexec.executable='echo' -Dexec.args='${project.version}' --non-recursive exec:exec -q)


echo "MAVEN release is prepared"
echo "Group                 : ${GROUP_ID}"
echo "Artifact              : ${ARTIFACT_ID}"
echo "Release scope         : ${RELEASE_SCOPE}"

VERSION_PARSED=(${VERSION//./ })
MAJOR=${VERSION_PARSED[0]}
MINOR=${VERSION_PARSED[1]}
PATCH_PARSED=(${VERSION_PARSED[2]//-/ })
PATCH=${PATCH_PARSED[0]}

if [[ ${RELEASE_SCOPE} = "major" ]]; then
  MAJOR=$((${MAJOR}+1))
  MINOR=0
  PATCH=0
elif [[ ${RELEASE_SCOPE} = "minor" ]]; then
  MAJOR=${MAJOR}
  MINOR=$((${MINOR}+1))
  PATCH=0
else
  MAJOR=${MAJOR}
  MINOR=${MINOR}
  PATCH=${PATCH}
fi

RELEASE_VERSION=${MAJOR}.${MINOR}.${PATCH}
echo "Release version       : ${RELEASE_VERSION}"

DEV_PATCH=$((${PATCH}+1))
DEV_VERSION="${MAJOR}.${MINOR}.${DEV_PATCH}-SNAPSHOT"
echo "New dev version       : ${DEV_VERSION}"

echo "scm.tag=${RELEASE_VERSION}\n
project.rel.${GROUP_ID}\:${ARTIFACT_ID}=${RELEASE_VERSION}\n
project.dev.${GROUP_ID}\:${ARTIFACT_ID}=${DEV_VERSION}" >> release.properties