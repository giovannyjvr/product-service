pipeline {
  agent any

  environment {
    IMAGE = "seuUsuario/product-service"
    TAG   = "${env.BUILD_NUMBER}"
  }

  stages {
    stage('Checkout') {
      steps {
        git branch: 'main', url: 'https://github.com/seuUsuario/product-service.git'
      }
    }

    stage('Build & Test') {
      steps {
        sh 'mvn clean install -DskipTests=false'
      }
    }

    stage('Package Jar') {
      steps {
        sh 'mvn package -DskipTests'
      }
    }

    stage('Build Docker Image') {
      steps {
        script {
          sh "docker build -t ${IMAGE}:${TAG} ."
        }
      }
    }

    stage('Push to Registry') {
      steps {
        // Exemplo: Docker Hub
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', 
                                         usernameVariable: 'DOCKER_USER', 
                                         passwordVariable: 'DOCKER_PASS')]) {
          sh "echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin"
          sh "docker push ${IMAGE}:${TAG}"
        }
      }
    }
  }

  post {
    success {
      echo "Build concluído e imagem enviada: ${IMAGE}:${TAG}"
    }
    failure {
      echo "Falha no pipeline do Product Service"
    }
  }
}
