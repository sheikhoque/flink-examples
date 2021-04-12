# Heartbeat reporcessing

To correctly reprocess Heartbeat data from S3 we have to process data in proper order. Heartbeat Sorter is small app that can read raw Heartbeat events potentially from any source (right now only S3 source is supported), sort them by event timestamp and write to S3 in compressed format. Gzip compression is supported for now.

## Build

```bash
cd heartbeat-sorter
mvn -DcompilerArgument=-Xlint:unchecked clean compile package
```

## Deployment
To deploy application just copy jar file to S3 like you do for KDA application or upload it directly to master node of EMR cluster if you use EMR for pre-processing the data 
```shell script
aws s3 cp target/heartbeat-sorter-0.0.13.jar s3://att-dtv-raptor-kda/flink_app/
```
## Usage

Application can be submitted as a Flink batch job in EMR or in KDA. If submitted in EMR, provide required command line arguments:
```
Usage: heartbeat-sorter [-hV] [-e=<endDateTime>] [-f=<propertyFile>]
                        [-g=<propertyGroupId>] [-s=<startDateTime>]
                        [-t=<sessionTTL>]
The app reads raw Heartbeat events from S3 folder, sort by event timestamp and
write to another S3 location in bucketed and compressed form.
  -f, --property-file=<propertyFile>
                  Property File
  -g, --property-group=<propertyGroupId>
                  Property Group
  -h, --help      Show this help message and exit.
  -s, --start-date=<startDateTime>
                  Start date
  -e, --end-date=<endDateTime>
                  End date
  -t, --session-ttl=<sessionTTL>
                  Session time to live in hours
  -V, --version   Print version information and exit.
```

If application submitted directly in Flink on EMR then parameters can be specified either through a property file or directly in command line.

## Reprocessing instruction
1. Find a range of **sessions start time** that you want to reprocess, this is period of dates when sessions created (not when events created).

2. Define a reasonable session time to live or session TTL, the recommended way is to find P99.9 of sessions lifetime - a lifetime that is greater or equal than 99.9 per cent of
all sessions. Note: you need to provide this value in hours in parameters file or as a parameter in CLI.

3. Create EMR cluster or KDA environment with desired compute resources. Note that each KPU provides 1 core, 4 GB of RAM and 50 GB of storage. It is recommended to have a dedicated
CPU core per Flink worker. In case of reprocessing long periods with terabytes of data, EMR is preferred option since you can get needed resources without requesting of 
increasing KDA limits. Example of command to launch EMR cluster:

```shell script
aws emr create-cluster --termination-protected --applications Name=Flink --tags 'Name=Flink cluster' \
--ec2-attributes '{"KeyName":"alexego","InstanceProfile":"EMR_EC2_DefaultRole","SubnetId":"subnet-2ef78f67","EmrManagedSlaveSecurityGroup":"sg-0835c5ddf2a4f3a68",'\
  '"EmrManagedMasterSecurityGroup":"sg-0c1ef98491dd8b30c"}' \
--release-label emr-5.29.0 --log-uri 's3n://att-dtv-raptor-kda/logs/emr/' \
--instance-groups '[{"InstanceCount":1,"EbsConfiguration":{"EbsBlockDeviceConfigs":'\
  '[{"VolumeSpecification":{"SizeInGB":128,"VolumeType":"gp2"},"VolumesPerInstance":2}]},"InstanceGroupType":"CORE","InstanceType":"c5n.4xlarge","Name":"Core"},'\
  '{"InstanceCount":1,"EbsConfiguration":{"EbsBlockDeviceConfigs":[{"VolumeSpecification":{"SizeInGB":32,"VolumeType":"gp2"},"VolumesPerInstance":2}]},'\
  '"InstanceGroupType":"MASTER","InstanceType":"m5.xlarge","Name":"Master"}]' \
--auto-scaling-role EMR_AutoScaling_DefaultRole --ebs-root-volume-size 10 --service-role EMR_DefaultRole --enable-debugging --name 'Raptor Project (Flink)' \
--scale-down-behavior TERMINATE_AT_TASK_COMPLETION --region us-west-2
```

4. Prepare parameters file, build jar if needed and upload to master node if you use EMR or to S3 if you use KDA.

```json5
[
  {
    "PropertyGroupId": "Preprocess",
    "PropertyMap": {
      "region":"us-west-2",
      "sourceEndpoint": "s3://aeg-videostream-kdf-raw/raw/",
      "targetEndpoint": "s3://aeg-videostream-kdf-raw/temp/",
      "filePathPrefix":  "year=!{timestamp:YYYY}/month=!{timestamp:MM}/day=!{timestamp:dd}/",
      "startDate": "2020-02-24",
      "endDate": "2020-02-25",
      "sessionTTL": 24,
      "parallelism": 16
    }
  }
]
```
5. Submit Flink pre-processing job using KDA UI or command if you are on EMR:

```shell script 
flink run -m yarn-cluster -ys 1 -p 16 -c com.att.dtv.kda.utils.HeartbeatSorter ./heartbeat-sorter-0.0.13.jar -g Preprocess -f ~/pre-app-conf.json
```

The command above will produce 16 gzipped file in `s3://aeg-videostream-kdf-raw/temp/`. Each file contains sorted events grouped by hash(sessionID) that can be later used for
reprocessing data from S3 by VideoPlayerStatsApp.

6. Prepare parameter file for reprocessing job
```json5
[
    {
        "PropertyGroupId": "Reprocess",
        "PropertyMap": {
            "sourceEndpoint": "s3://aeg-videostream-kdf-raw/temp/",
            "region":"us-west-2",
            "intervalStatsEndpoint":"ess://search-kinesis-elasti-1mp3rw0zypl07-eegkhp4pvktxw2ffsb6ogry2ia.us-west-2.es.amazonaws.com",
            "intervalStatsIndexName":"sessions",
            "intervalLength":1,
            "parallelism":16,
            "cdn.map.location":"s3://att-dtv-raptor-kda/flink_app_load/cdn_url_mapping.csv",
            "sessionStatsEndpoint": "firehose://fh-video-metrics-report"
        }
    }
]
```

7. Submit Flink processing job using KDA UI or command if you are on EMR:

```shell script 
flink run -m yarn-cluster -ys 1 -p 16 -c com.att.dtv.kda.videoapp.VideoPlayerStatsApp ./player-metrics-app-0.0.13.jar -g Reprocess -f ~/app-conf.json
```