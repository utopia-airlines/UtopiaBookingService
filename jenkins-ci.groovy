properties([parameters([string(defaultValue: '', description: 'URL for database to connect instances to. ', name: 'RDSURL', trim: false), string(defaultValue: 'root', description: 'Username for connecting to the database.', name: 'RDSUser', trim: false), password(defaultValue: '', description: 'Password for connecting to the database', name: 'RDSPassword'), string(defaultValue: '', description: 'The S3 bucket the artifact(s) should be placed in', name: 'TargetBucket', trim: false)])]);
node {
    stage("Source"){
        git branch: 'elk-monitoring', url: 'https://github.com/kingjon3377/UtopiaBookingService.git';
    }
    stage('Add application.properties') {
        sh label: 'Remove old application.properties', script: 'rm -f src/main/resources/application.properties';

        writeFile encoding: 'UTF-8', file: 'src/main/resources/application.properties', text: '''spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.mvc.throw-exception-if-no-handler-found=true
spring.resources.add-mappings=false
spring.jpa.hibernate.ddl-auto = update
spring.jpa.hibernate.naming.implicit-strategy=org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyJpaImpl
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl''';
        sh label: 'Add sensitive details to application.properties', script: "printf \'\n%s\n%s\n%s\n\' \'spring.datasource.url=jdbc:mysql://${params.RDSURL}\' \'spring.datasource.username=${params.RDSUser}\' \'spring.datasource.password=${params.RDSPassword}\' >> src/main/resources/application.properties"
    }
   
    stage("build") {
        sh 'mvn clean package';
    }

    stage("archive") {
        archiveArtifacts 'target/*.jar';
        s3Upload bucket: "${params.TargetBucket}", includePathPattern: 'target/*.jar';
    }
}
