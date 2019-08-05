properties([parameters([string(defaultValue: 'booking-0.0.1-SNAPSHOT.jar', description: 'Name of the artifact to be deployed', name: 'ArtifactName', trim: true), string(defaultValue: 'us-east-1', description: 'AWS region to deploy the stack in', name: 'AWSRegion', trim: true), string(defaultValue: '', description: 'Name to give the stack', name: 'StackName'), string(defaultValue: '', description: 'The S3 bucket the artifact should be taken from and endpoint URL placed in', name: 'TargetBucket', trim: false)])]);
node {
    stage("Source"){
        git branch: 'elk-monitoring', url: 'https://github.com/kingjon3377/UtopiaBookingService.git';
    }
    stage('Delete previous cloud formation stack') {
        sh label: 'Delete old cloud formation', script: 'aws --region us-east-1 cloudformation delete-stack --stack-name booking-service-auto-gen'
        sh label: 'Wait for the deletion of cloud formation', script: 'aws --region us-east-1 cloudformation wait stack-delete-complete --stack-name booking-service-auto-gen || true'
    }

    stage('Create parameters file') {
        sh label: 'Remove old parameters file', script: 'rm -f cloud-formation-params.json';
        sh label: 'Create parameters file', script: "printf '[{%s},\n{%s}]\n' '{\"ParameterKey\": \"JarFileSource\",\"ParameterValue\": \"s3://${params.TargetBucket}/${params.ArtifactName}\"}' '{\"ParameterKey\": \"JarFileName\",\"ParameterValue\": \"${params.ArtifactName}\"}' > cloud-formation-params.json"
    }

    stage('Create cloud-formation stack') {
        sh label: 'Create new Cloud Stack', script: "aws --region ${params.AWSRegion} cloudformation create-stack --stack-name ${params.StackName} --template-body file://cftemplate.json --parameters file://cloud-formation-params.json";
        sh label: 'Wait for the creation of the cloud formation', script: "aws --region ${params.AWSRegion} cloudformation wait stack-create-complete --stack-name ${params.StackName}";
        sh label: 'Get endpoint DNS', script: 'aws cloudformation describe-stacks --stack-name EC2stack | jq \'.Stacks | .[0] | .Outputs | .[] | .OutputValue\' | sed \'s@^"\\(.*\\)"@\\1@\' | tac | tr \'\\n\' : | sed \'s@:$@@\' > BookingHost.url';
        s3Upload bucket: "${params.TargetBucket}", file: 'BookingHost.url';
    }
}
