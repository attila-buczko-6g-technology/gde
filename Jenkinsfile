// =====================================================================
// Blogplatform CI/CD pipeline
//
// Lépések:
//   1) Checkout         – legfrissebb commit a GitHub repó-ból
//   2) Build & Test     – Gradle bootJar + tesztek a Jenkins konténerben
//   3) Docker image     – blogplatform-app:latest image építése
//   4) Deploy           – régi 'blog-app' konténer leállítása + új indítása
//                         a blognet hálózaton, a postgres mellé
// =====================================================================
pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    environment {
        IMAGE_NAME     = 'blogplatform-app'
        IMAGE_TAG      = "build-${env.BUILD_NUMBER}"
        APP_CONTAINER  = 'blog-app'
        APP_PORT       = '8080'
        DB_URL         = 'jdbc:postgresql://blog-postgres:5432/blogdb'
        DB_USER        = 'bloguser'
        DB_PASSWORD    = 'blogpass'
        JWT_SECRET     = 'production-jwt-titkos-kulcs-legalabb-32-karakter'
    }

    stages {

        steps {
            git branch: 'main',
                url: 'https://github.com/attila-buczko-6g-technology/gde.git'

            sh 'git log -1 --oneline'
        }

        stage('Build & Test (Gradle)') {
            agent {
                docker {
                    // A Jenkins egy temurin JDK 21 konténerben futtatja a Gradle buildet.
                    image 'eclipse-temurin:21-jdk'
                    reuseNode true
                    args  '-v gradle-cache:/root/.gradle'
                }
            }
            steps {
                dir('backend') {
                    sh 'chmod +x ./gradlew || true'
                    // A gradlew script már a repóban van; ha nem, a wrapper task generálja.
                    sh '''
                        if [ ! -x ./gradlew ]; then
                            echo "gradlew nem létezik – fallback: gradle"
                            apt-get update && apt-get install -y --no-install-recommends gradle
                            gradle wrapper
                        fi
                        ./gradlew --no-daemon clean test bootJar
                    '''
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
                    cp backend/build/libs/blogplatform.jar docker/app/blogplatform.jar
                    docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest docker/app/
                '''
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    # Régi konténer leállítása és eltávolítása (ha létezik)
                    docker rm -f ${APP_CONTAINER} || true

                    # Új konténer indítása a blognet hálózaton, a postgres mellé
                    docker run -d \
                        --name ${APP_CONTAINER} \
                        --network blognet \
                        -p ${APP_PORT}:8080 \
                        -e DB_URL=${DB_URL} \
                        -e DB_USER=${DB_USER} \
                        -e DB_PASSWORD=${DB_PASSWORD} \
                        -e JWT_SECRET=${JWT_SECRET} \
                        --restart unless-stopped \
                        ${IMAGE_NAME}:latest

                    echo "✅ Alkalmazás elérhető: http://localhost:${APP_PORT}/"
                '''
            }
        }
    }

    post {
        success {
            echo "Build #${env.BUILD_NUMBER} sikeresen telepítve."
        }
        failure {
            echo "Build #${env.BUILD_NUMBER} sikertelen – nézd meg a logokat."
        }
    }
}
