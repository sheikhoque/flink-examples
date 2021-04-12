import sys
from awsglue.transforms import *
from awsglue.utils import getResolvedOptions
from pyspark.context import SparkContext
from awsglue.context import GlueContext
from awsglue.job import Job
from pyspark.sql.types import DoubleType, TimestampType, LongType, StringType
from pyspark.sql import functions as f
from pyspark.sql import Window
from pyspark.sql.functions import col, lit, udf
from awsglue.dynamicframe import DynamicFrame
from datetime import date, datetime, timedelta
from py4j.java_gateway import java_import
from pyspark.sql.functions import unix_timestamp

# @params: [JOB_NAME]

prepareColumnUDF = udf(lambda
                       browser, device_hardware_type, device_manufacturer, device_model, device_os, device_os_version, app_session_id, platform,
                       connection_type, device_ad_id, device_id, geolocation, launch_type, platform_version,
                       profile_id, restart_eligible, start_type, stream_id, target_group, serial_number, strmt:
                       "dv.brv="+str(browser)+"&dv.hwt=" + str(device_hardware_type)+"&dv.mnf=" + str(
                           device_manufacturer)
                       + "&dv.mod="+str(device_model) + "&dv.os="+str(device_os)+"&dv.osv="+str(
                           device_os_version)
                       + "&appSessionID="+str(app_session_id) + "&c3.player.name="+str(
                           platform) + "&connectionType=" +
                       str(connection_type) + "&devideAdID=" +
                           str(device_ad_id) + "&devideID=" + str(device_id)
                       + "&geolocation="+str(geolocation) + "&launchType="+str(
                           launch_type)
                       + "&playerVersion=" + str(platform_version) + "&profileID=" + str(
                           profile_id) + "&restart=" + str(restart_eligible) + "&startType=" + str(start_type)
                       + "&streamID=" + str(stream_id) + "&TargetGroup=" + str(
                           target_group) + "&SerialNumber=" + str(serial_number) + "&streamType=" + str(strmt),
                       StringType())

# write SSD report in three format


def generateSSDReport(ssd_output_dir, sessionsDF, ssd_csv_file_name_prefix, year, month, day):
    # prepare the data frame

    sessionsDF = sessionsDF.withColumn("session tags",
                                       prepareColumnUDF(col('browser'),
                                                        col('device_hardware_type'),
                                                        col('device_manufacturer'),
                                                        col('device_model'),
                                                        col('device_os'),
                                                        col('device_os_version'),
                                                        col('app_session_id'),
                                                        col('platform'),
                                                        col('connection_type'),
                                                        col('device_ad_id'),
                                                        col('device_id'),
                                                        col('geolocation'),
                                                        col('launch_type'),
                                                        col('platform_version'),
                                                        col('profile_id'),
                                                        col('restart_eligible'),
                                                        col('start_type'),
                                                        col('stream_id'),
                                                        col('target_group'),
                                                        col('serial_number'),
                                                        col('strmt')
                                                        )).withColumn("start time", unix_timestamp(col("start_time").cast(TimestampType())))

    convivaSSDDF = sessionsDF.selectExpr("viewerId",
                                         "asset",
                                         "device_os as `device/os`",
                                         "country", "state", "city", "asn", "isp",
                                         "`start time`",
                                         "startup_time as `startup time`",
                                         "playing_time as `playing time`",
                                         "buffering_time as `buffering time`", "interrupts",
                                         "average_bitrate as `average bitrate`",
                                         "vsf as `startup error`", "`session tags`",
                                         "ip_address as `ip address`", "cdn", "browser",
                                         "sid as `conviva session id`",
                                         "streaming_url as `stream url`",
                                         "error_list as `error list`",
                                         "percentage_complete as `percentage complete`",
                                         "connection_induced_rebuffering_time",
                                         "vpf as VPF", "vpf_error_list as `VPF error list`",
                                         "year", "month", "day"
                                         )
    # writing csv
    generateSSDReportCsvFormat(
        ssd_output_dir, convivaSSDDF, ssd_csv_file_name_prefix, year, month, day)


def generateSSDReportCsvFormat(ssd_output_dir, sessionsDF, ssd_csv_file_name_prefix, year, month, day):
    csv_destination_path = ssd_output_dir + "/csv"
    java_import(spark._jvm, 'org.apache.hadoop.fs.Path')
    bucket_name = csv_destination_path.split("s3://")[1].split("/")[0]
    hdfs_path = "s3://" + bucket_name
    sc._jsc.hadoopConfiguration().set("fs.defaultFS", hdfs_path)
    sessionsDF.repartition(1).na.drop(how="all").write.partitionBy("year", "month", "day").mode(
        "overwrite").format("csv").option("header", "true").save(csv_destination_path)
    curr_path = csv_destination_path + \
        "/year={}/month={}/day={}".format(year, month, day)
    fs = spark._jvm.org.apache.hadoop.fs.FileSystem.get(
        spark._jsc.hadoopConfiguration())
    list_status = fs.listStatus(
        spark._jvm.org.apache.hadoop.fs.Path(curr_path))
    generated_file_name = [file.getPath().getName(
    ) for file in list_status if file.getPath().getName().startswith('part-')][0]
    updated_file_name = ssd_csv_file_name_prefix + \
        "_{}-{}-{}.csv".format(year, month, day)
    fs.rename(sc._jvm.Path(curr_path + '/' + generated_file_name),
              sc._jvm.Path(curr_path + '/' + updated_file_name))


def generate():
    args = getResolvedOptions(sys.argv, ['JOB_NAME'])
    job = Job(glueContext)
    job.init(args['JOB_NAME'], args)

    try:
        args = getResolvedOptions(sys.argv, [
            'JOB_NAME', 'REPORT_DATE', 'CONVIVA_SSD_DEST',
            'INPUT_SESSION_DATA_SOURCE', 'CONVIVA_SSD_REPORT_NAME_PREFIX'])
    except:
        raise RuntimeError(
            'We need following parameters - \'REPORT_DATE\',\'CONVIVA_SSD_DEST\',\'INPUT_SESSION_DATA_SOURCE\',\'CONVIVA_SSD_REPORT_NAME_PREFIX\'')

    try:
        args_report_date = args['REPORT_DATE']
        report_date = datetime.strptime(args_report_date.strip(), '%Y-%m-%d')
    except:
        report_date = date.today() - timedelta(days=1)

    report_date = datetime.combine(report_date, datetime.min.time())
    input_session_data_source = args['INPUT_SESSION_DATA_SOURCE']
    ssd_output_dir = args['CONVIVA_SSD_DEST']
    ssd_csv_file_name_prefix = args['CONVIVA_SSD_REPORT_NAME_PREFIX']

    any_error = ""

    if (not (input_session_data_source and input_session_data_source.strip())):
        any_error = any_error + " INPUT_SESSION_DATA_SOURCE "
    if (not (ssd_output_dir and ssd_output_dir.strip())):
        any_error = any_error + " CONVIVA_SSD_DEST "
    if (not (ssd_csv_file_name_prefix and ssd_csv_file_name_prefix.strip())):
        any_error = any_error + " CONVIVA_SSD_REPORT_NAME_PREFIX "

    if (any_error):
        any_error = any_error + " paramater(s) are empty"
        raise RuntimeError(any_error)

    year = report_date.year
    month = report_date.strftime('%m')
    day = report_date.strftime('%0d')
    next_day = (report_date + timedelta(days=1)).strftime('%0d')

    input_session_data_source_with_partition = input_session_data_source + \
                                               "/year={}/month={}/day={{{},{}}}/".format(
                                                   year, month, day, next_day)
    sessionsDF = spark.read.json(input_session_data_source_with_partition)
    sessionsDF = sessionsDF.filter(
        (col("end_time") >= "{}".format((report_date + timedelta(hours=8)).strftime('%Y-%m-%dT%H:%M:%SZ'))) & (
                col("end_time") < "{}".format((report_date + timedelta(days=1, hours=8)).strftime('%Y-%m-%dT%H:%M:%SZ'))))
    sessionsDF = sessionsDF.na.drop(how="all").dropDuplicates()
    sessionsDF = sessionsDF.withColumn("sid", col("sid").cast(LongType())).withColumn(
        'year', lit(year)).withColumn('month', lit(month)).withColumn('day', lit(day))

    generateSSDReport(ssd_output_dir, sessionsDF,
                      ssd_csv_file_name_prefix, year, month, day)
    job.commit()


sc = SparkContext()
glueContext = GlueContext(sc)
spark = glueContext.spark_session
sc._jsc.hadoopConfiguration().set(
    "mapreduce.input.fileinputformat.input.dir.recursive", "true")
spark.conf.set("spark.sql.sources.partitionOverwriteMode", "dynamic")
spark.conf.set("spark.sql.caseSensitive", "true")
generate()
