pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    environment {
        IMAGE_NAME    = 'blogplatform-app'
        IMAGE_TAG     = "build-${env.BUILD_NUMBER}"
        APP_CONTAINER = 'blog-app'
        APP_PORT      = '8080'
        DB_URL        = 'jdbc:postgresql://blog-postgres:5432/blogdb'
        DB_USER       = 'bloguser'
        DB_PASSWORD   = 'blogpass'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git log -1 --oneline'
            }
        }

        stage('Build & Test') {
            agent {
                docker {
                    image 'eclipse-temurin:21-jdk'
                    reuseNode true
                    args '-v /var/jenkins_home/.gradle:/root/.gradle'
                }
            }

            steps {
                dir('backend') {
                    sh 'chmod +x gradlew'
                    sh './gradlew --no-daemon clean test bootJar'
                }
            }

            post {
                always {
                    junit allowEmptyResults: true,
                        testResults: 'backend/build/test-results/test/*.xml'
                }
            }
        }

        stage('Docker image build') {
            steps {
                sh '''
                    cp backend/build/libs/*.jar docker/app/blogplatform.jar
                    docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest docker/app/
                '''
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    docker rm -f ${APP_CONTAINER} || true

                    docker run -d \
                      --name ${APP_CONTAINER} \
                      --network blognet \
                      -p ${APP_PORT}:8080 \
                      -e DB_URL=${DB_URL} \
                      -e DB_USER=${DB_USER} \
                      -e DB_PASSWORD=${DB_PASSWORD} \
                      --restart unless-stopped \
                      ${IMAGE_NAME}:latest
                '''
            }
        }
    }
}