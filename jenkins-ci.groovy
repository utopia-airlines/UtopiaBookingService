node {
    stage("Source"){
        git branch: 'elk-monitoring', url: 'https://github.com/kingjon3377/UtopiaBookingService.git';
    }
    stage('Add application.properties') {
        sh label: 'Remove old properties file', script: 'rm -f src/main/resources/database-config.properties';

        withCredentials([string(credentialsId: 'RDSURL', variable: 'RDSURL'), usernamePassword(credentialsId: 'RDSCredentials', passwordVariable: 'RDSPassword', usernameVariable: 'RDSUser')]) {
            writeFile encoding: 'UTF-8', file: 'src/main/resources/database-config.properties', text: """spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.mvc.throw-exception-if-no-handler-found=true
spring.resources.add-mappings=false
spring.jpa.hibernate.ddl-auto = update
spring.jpa.hibernate.naming.implicit-strategy=org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyJpaImpl
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
spring.datasource.url=jdbc:mysql://${RDSURL}
spring.datasource.username=${RDSUser}
spring.datasource.password=${RDSPassword}""";
        }
    }
   
    stage("build") {
        sh 'mvn clean package';
    }

    stage("archive") {
        archiveArtifacts 'target/*.jar';
        withCredentials([string(credentialsId: 'TargetBucket', variable: 'TargetBucket')]) {
            dir("target") {
                s3Upload bucket: "${TargetBucket}", includePathPattern: '*.jar';
            }
        }
    }
}
