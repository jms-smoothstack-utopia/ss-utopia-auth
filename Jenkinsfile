pipeline {
    agent any
    stages {
        stage('Clean target') {
            steps {
                sh 'mvn clean'
            }
        }
        stage('Lint') {
            steps {
                sh 'mvn checkstyle:check'
            }
        }
        stage('Test and Package') {
            steps {
                sh 'mvn test package'
            }
        }
        stage('Code Analysis: SpotBugs') {
            steps {
                sh 'mvn spotbugs:check'
            }
        }
        stage('Code Analysis: PMD') {
            steps {
                sh 'mvn pmd:check'
            }
        }
        stage('Code Analysis: Sonarqube') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn sonar:sonar'
                }
            }
        }
        stage('Await Quality Gateway') {
            steps {
                waitForQualityGate abortPipeline: true
            }
        }
        stage('Build Docker image') {
            steps {
                sh 'mvn git-commit-id:revision docker:build'
            }
        }
        stage('Push image to repository') {
            when {
                anyOf {
                    branch 'main';
                    branch 'dev'
                }
            }
            steps {
                sh 'aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 247293358719.dkr.ecr.us-east-1.amazonaws.com'
                sh 'mvn git-commit-id:revision docker:push'
            }
        }
    }
    post {
        always {
            sh 'mvn clean'
            sh 'docker system prune -f'
        }
    }
}
