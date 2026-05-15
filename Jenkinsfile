// =====================================================================
// Blogplatform CI/CD pipeline
//
// Lépések:
//   1) Checkout         – legfrissebb commit a GitHub repó-ból
// =====================================================================
pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }


    stages {

        stage('Checkout') {
            steps {
                checkout scm
                sh 'git log -1 --oneline'
            }
        }
    }
}