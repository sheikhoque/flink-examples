#### Instructions 
###### Build
```
#download flink-kinesis connector
wget -qO- https://github.com/apache/flink/archive/release-1.6.4.zip | bsdtar -xf-
# build flink-kinesis connector & install in local repo
cd flink-release-1.6.4
mvn clean install -B -DskipTests -Dfast -Pinclude-kinesis -pl flink-connectors/flink-connector-kinesis
cd..
# package app
mvn clean package
# upload to s3
aws s3 cp target/player-metrics-app-1.0-SNAPSHOT.jar s3://${s3_bucket}/${s3_path}/ --profile ${aws_cli_profile}
e.g. 
aws s3 cp target/player-metrics-app-0.0.1-SNAPSHOT.jar s3://att-dtv-raptor-kda/flink_app/ --profile att
```
###### Provision infrastructure
```concept
aws cloudformation deploy --template-file resources/cfn/kinesis-es.yml --stack-name kinesis-es \
--parameter-overrides ClientIpAddressRange=10.0.4.0/24 FlinkApplicationS3BucketArn=${s3_bucket_arn} \
FlinkApplicationS3Path=flink_app/player-metrics-app-0.0.1-SNAPSHOT.jar --capabilities CAPABILITY_IAM \
--profile ${aws_cli_profile}
e.g. 
aws cloudformation deploy --template-file resources/cfn/kinesis-es.yml --stack-name kinesis-es \
--parameter-overrides ClientIpAddressRange=10.0.4.0/24 FlinkApplicationS3BucketArn=arn:aws:s3:::att-dtv-raptor-kda \
FlinkApplicationS3Path=flink_app/player-metrics-app-0.0.1-SNAPSHOT.jar --capabilities CAPABILITY_IAM --profile att
```

###### Post build
```
# wait until CF stack, was created successfully
aws --region ${AWS::Region} cloudformation wait stack-create-complete --stack-name '${AWS::StackName}'
# create ES indices
cat es-resources/agg_index.json | curl -s -w "\n" -XPUT https://${ElasticsearchService.DomainEndpoint}/session -H "Content-Type: application/json" -d @-
# create Kinaba visualizations and dashboard
cat es-resources/sample-dashboard.json | curl -s -w "\n" -XPOST https://${ElasticsearchService.DomainEndpoint}/_plugin/kibana/api/saved_objects/_bulk_create -H 'Content-Type: application/json' -H 'kbn-xsrf: true' -d @-
# set default Kibana index pattern
curl -s -w "\n" -XPOST 'https://${ElasticsearchService.DomainEndpoint}/_plugin/kibana/api/kibana/settings' -H 'content-type: application/json' -H 'kbn-xsrf: true' --data '{"changes":{"defaultIndex":"session-index-pattern"}}'
# Add VPC connectivity to app (change app name, subnet IDs, and security group IDs in add_app_vpc.json file
aws kinesisanalyticsv2 add-application-vpc-configuration --current-application-version-id ${version} --cli-input-json file://resources/add_app_vpc.json --profile att
# start the Flink application
aws kinesisanalyticsv2 start-application --cli-input-json file://resources/start_app.json --profile att
```

###### Update app code
```concept
# get app version
aws kinesisanalyticsv2 describe-application --application-name VideoMetricsApp --profile att | grep ersion
# update app
aws kinesisanalyticsv2 update-application --current-application-version-id ${app_version} --cli-input-json file://resources/update_app.json --profile att
```