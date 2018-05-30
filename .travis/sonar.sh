#!/bin/bash
if [ "${TRAVIS_REPO_SLUG}" == "molgenis/gavin-plus" ] && [ "${TRAVIS_PULL_REQUEST}" == "false" ]; then
	echo "Running sonar analysis in maven..."
    ./mvnw sonar:sonar -Dsonar.login=${SONAR_TOKEN} -Dsonar.branch=${TRAVIS_BRANCH} --batch-mode --quiet
    echo "Done."
else
    echo "This is a pull request. Skipped Sonar analysis."
fi