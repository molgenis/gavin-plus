pipeline {
    agent {
        kubernetes {
            label 'molgenis'
        }
    }
    stages {
        stage('Prepare') {
            steps {
                script {
                    env.GIT_COMMIT = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                }
                container('vault') {
                    script {
                        sh "mkdir /home/jenkins/.m2"
                        sh(script: 'vault read -field=value secret/ops/jenkins/maven/settings.xml > /home/jenkins/.m2/settings.xml')
                        env.GITHUB_TOKEN = sh(script: 'vault read -field=value secret/ops/token/github', returnStdout: true)
                        env.CODECOV_TOKEN = sh(script: 'vault read -field=gavin-plus secret/ops/token/codecov', returnStdout: true)
                        env.SONAR_TOKEN = sh(script: 'vault read -field=value secret/ops/token/sonar', returnStdout: true)
                        env.GITHUB_USER = sh(script: 'vault read -field=username secret/ops/token/github', returnStdout: true)
                    }
                    dir('/home/jenkins/.m2') {
                        stash includes: 'settings.xml', name: 'maven-settings'
                    }
                }
            }
        }
        stage('Build: [ pull request ]') {
            when {
                changeRequest()
            }
            steps {
                container('maven') {
                    sh "mvn clean install -Dmaven.test.redirectTestOutputToFile=true -T4"
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/**.xml'
                    container('maven') {
                        sh "curl -s https://codecov.io/bash | bash -s - -c -F unit -K  -C ${GIT_COMMIT}"
                        sh "mvn -q -B sonar:sonar -Dsonar.login=${env.SONAR_TOKEN} -Dsonar.github.oauth=${env.GITHUB_TOKEN} -Dsonar.pullrequest.base=${CHANGE_TARGET} -Dsonar.pullrequest.branch=${BRANCH_NAME} -Dsonar.pullrequest.key=${env.CHANGE_ID} -Dsonar.pullrequest.provider=GitHub -Dsonar.pullrequest.github.repository=molgenis/gavin-plus -Dsonar.ws.timeout=120"
                    }
                }
            }
        }
        stage('Build: [ master ]') {
            when {
                branch 'master'
            }
            steps {
                milestone 1
                container('maven') {
                    sh "mvn clean deploy -Dmaven.test.redirectTestOutputToFile=true -T4"
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/**.xml'
                    container('maven') {
                        sh "curl -s https://codecov.io/bash | bash -s - -c -F unit -K  -C ${GIT_COMMIT}"
                        sh "mvn -q -B sonar:sonar -Dsonar.login=${SONAR_TOKEN} -Dsonar.ws.timeout=120"
                    }
                }
            }
        }
        stage('Steps: [ x.x ]') {
            when {
                expression { BRANCH_NAME ==~ /[0-9]\.[0-9]/ }
            }
            stages {
                stage('Build [ x.x ]') {
                    steps {
                        dir('/home/jenkins/.m2') {
                            unstash 'maven-settings'
                        }
                        container('maven') {
                            sh "mvn -q -B clean deploy -Dmaven.test.redirectTestOutputToFile=true -T4"
                            sh "curl -s https://codecov.io/bash | bash -s - -c -F unit -K  -C ${GIT_COMMIT}"
                            sh "mvn -q -B sonar:sonar -Dsonar.login=${SONAR_TOKEN} -Dsonar.branch.name=${BRANCH_NAME} -Dsonar.ws.timeout=120"
                        }
                    }
                }
                stage('Prepare Release [ x.x ]') {
                    steps {
                        timeout(time: 40, unit: 'MINUTES') {
                            input(message: 'Prepare to release?')
                        }
                        container('maven') {
                            sh "mvn -q -B release:prepare -Dmaven.test.redirectTestOutputToFile=true -Darguments=\"-q -B -DskipITs -Dmaven.test.redirectTestOutputToFile=true\""
                        }
                    }
                }
                stage('Perform release [ x.x ]') {
                    steps {
                        container('maven') {
                            sh "mvn -q -B release:perform -Darguments=\"-q -B -Dmaven.test.redirectTestOutputToFile=true\""
                        }
                    }
                }
            }
        }
        stage('Steps [ feature ]') {
            when {
                expression { BRANCH_NAME ==~ /feature\/.*/ }
            }
            environment {
                TAG = "$BRANCH_NAME-$BUILD_NUMBER".replaceAll(~/[^\w.-]/, '-').toLowerCase()
            }
            stages {
                stage('Build [ feature ]') {
                    steps {
                        container('maven') {
                            sh "mvn -q -B clean verify -Dmaven.test.redirectTestOutputToFile=true"
                            sh "curl -s https://codecov.io/bash | bash -s - -c -F unit -K  -C ${GIT_COMMIT}"
                            sh "mvn -q -B sonar:sonar -Dsonar.branch.name=${BRANCH_NAME} -Dsonar.login=${SONAR_TOKEN} -Dsonar.ws.timeout=120"
                        }
                    }
                    post {
                        always {
                            junit '**/target/surefire-reports/**.xml'
                        }
                    }
                }
            }
        }
    }
}