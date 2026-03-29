
pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 45, unit: 'MINUTES')
    }

    environment {
        SPRING_DATASOURCE_URL = 'jdbc:postgresql://postgres:5432/fleet_db'
        SPRING_DATASOURCE_USERNAME = 'fleet'
        SPRING_DATASOURCE_PASSWORD = 'fleet'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Résoudre le répertoire backend') {
            steps {
                script {
                    if (fileExists('docker-compose.yml')) {
                        env.BACKEND_DIR = '.'
                        env.MAVEN_WORKDIR = '/workspace'
                    } else if (fileExists('fleet_management_backend/docker-compose.yml')) {
                        env.BACKEND_DIR = 'fleet_management_backend'
                        env.MAVEN_WORKDIR = '/workspace/fleet_management_backend'
                    } else {
                        error('docker-compose.yml introuvable : placez le job à la racine du backend ou du monorepo.')
                    }
                }
            }
        }

        stage('Démarrer PostgreSQL') {
            steps {
                sh '''
                    set -e
                    cd "$BACKEND_DIR"
                    docker compose -f docker-compose.yml up -d postgres
                    echo "Attente de PostgreSQL..."
                    for i in $(seq 1 60); do
                      if docker compose -f docker-compose.yml exec -T postgres pg_isready -U fleet -d fleet_db; then
                        echo "PostgreSQL prêt."
                        exit 0
                      fi
                      sleep 2
                    done
                    echo "Timeout: PostgreSQL n'est pas prêt."
                    exit 1
                '''
            }
        }

        stage('Tests & package Maven') {
            steps {
                sh '''
                    set -e
                    docker run --rm \
                      --network fleet_net \
                      -v "$WORKSPACE:/workspace" \
                      -w "$MAVEN_WORKDIR" \
                      -e SPRING_DATASOURCE_URL="$SPRING_DATASOURCE_URL" \
                      -e SPRING_DATASOURCE_USERNAME="$SPRING_DATASOURCE_USERNAME" \
                      -e SPRING_DATASOURCE_PASSWORD="$SPRING_DATASOURCE_PASSWORD" \
                      maven:3.9-eclipse-temurin-21 \
                      mvn --batch-mode clean verify
                '''
            }
        }

        stage('Image Docker backend') {
            steps {
                sh '''
                    set -e
                    cd "$BACKEND_DIR"
                    docker compose -f docker-compose.yml build backend
                '''
            }
        }
    }

    post {
        success {
            echo 'Pipeline terminé avec succès.'
        }
        failure {
            echo 'Pipeline en échec — voir les logs ci-dessus.'
        }
    }
}
