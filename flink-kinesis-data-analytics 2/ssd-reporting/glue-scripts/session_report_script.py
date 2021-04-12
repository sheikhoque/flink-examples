import sys
from awsglue.transforms import *
from awsglue.utils import getResolvedOptions
from pyspark.context import SparkContext
from awsglue.context import GlueContext
from awsglue.job import Job
from pyspark.sql.types import DoubleType, TimestampType, LongType
from pyspark.sql import functions as f
from pyspark.sql import Window
from pyspark.sql.functions import col
from pyspark.sql.functions import lit
from awsglue.dynamicframe import DynamicFrame
from datetime import date, datetime, timedelta
from py4j.java_gateway import java_import

# @params: [JOB_NAME]


# write SSD report in three format
def generateSSDReport(ssd_output_dir, sessionsDF, ssd_csv_file_name_prefix, year, month, day):
    orc_destination_path = ssd_output_dir + "/orc"
    parquet_destination_path = ssd_output_dir + "/parquet"
    # writing to orc
    sessionsDF.write.partitionBy("year", "month", "day").mode(
        "overwrite").format("orc").option("header", "true").save(orc_destination_path)
    # writing to parquet
    sessionsDF.write.partitionBy("year", "month", "day").mode(
        "overwrite").format("parquet").option("header", "true").save(parquet_destination_path)
    # writing csv
    generateSSDReportCsvFormat(
        ssd_output_dir, sessionsDF, ssd_csv_file_name_prefix, year, month, day)


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


def generateAnomalyReports(input_rawhb_source_with_parition, missing_hb_dir, expired_session_dir, sessionsDF, year, month, day):
    # build missing heartbeat & session expired report
    expiredSessionDF = sessionsDF.where("ended_status=2")

    expiredSessionModifiedDF = expiredSessionDF.withColumn(
        "session_expired_time", expiredSessionDF["end_time"].cast(TimestampType()))

    rawDF = spark.read.json(input_rawhb_source_with_parition)
    rawDF = rawDF.withColumn("sst", col("sst").cast(DoubleType())) \
        .withColumn("st", col("st").cast(DoubleType())) \
        .withColumn("sid", col("sid").cast(LongType()))
    rawModifiedDF = rawDF.withColumn(
        'hb_arrival_time', f.from_unixtime((rawDF["sst"] + rawDF["st"]) / 1000))

    w = Window.partitionBy('sid')
    lastRawHBDF = rawModifiedDF.withColumn('maxhbseq', f.max('hbseq').over(
        w)).where(f.col('hbseq') == f.col('maxhbseq')).drop("maxhbseq")
    firstRawHBDF = rawModifiedDF.withColumn('minhbseq', f.min('hbseq').over(
        w)).where(f.col('hbseq') == f.col('minhbseq')).drop("minhbseq")

    # df of mission heartbeats
    lastHBNotInSessionDF = lastRawHBDF.alias("a").join(sessionsDF.alias(
        "b"), ["sid"], how="leftanti").where(f.col("sid").isNotNull())
    firstHBNotInSessionDF = firstRawHBDF.alias("a").join(sessionsDF.alias(
        "b"), ["sid"], how="leftanti").where(f.col("sid").isNotNull())
    missingHeartbeatsDF = lastHBNotInSessionDF.union(firstHBNotInSessionDF).drop_duplicates().sort(f.col("hbseq").asc()) \
        .withColumn('year', lit(year)).withColumn('month', lit(month)).withColumn('day', lit(day))

    # expired session report
    lastHBAfterExpiredDF = lastRawHBDF.alias("a") \
        .join(expiredSessionModifiedDF.alias("b"), (lastRawHBDF["sid"] == expiredSessionModifiedDF["sid"]) & (
            expiredSessionModifiedDF["session_expired_time"].isNotNull() & (
                lastRawHBDF["hb_arrival_time"] > expiredSessionModifiedDF["session_expired_time"]))) \
        .select(col('a.sid'), col('a.hbseq'), col('a.hb_arrival_time'), col('b.session_expired_time'))

    rawCandidateHBS = rawModifiedDF.alias('a') \
        .join(lastHBAfterExpiredDF.alias('b'), rawModifiedDF["sid"] == lastHBAfterExpiredDF["sid"]) \
        .select(col('a.*'), col('b.session_expired_time'))

    rawCandidatesHbSeqAfterExpired = rawCandidateHBS \
        .where(rawCandidateHBS["hb_arrival_time"] > rawCandidateHBS["session_expired_time"])

    rawHBsFirstArrivalAfterExpired = rawCandidatesHbSeqAfterExpired.withColumn('minhbseq', f.min('hbseq').over(w)) \
        .where((f.col('hbseq') == f.col('minhbseq'))) \
        .drop("minhbseq")

    timeFmt = "yyyy-MM-dd HH:mm:ss"
    timeDiff = (f.unix_timestamp('hb_arrival_time', format=timeFmt)
                - f.unix_timestamp('session_expired_time', format=timeFmt))
    rawHBsWithDiff = rawHBsFirstArrivalAfterExpired.withColumn(
        "diff_in_min_first_arrival_of_hb_after_session_expired", timeDiff / 60.0)

    candidatesHBDF = rawHBsWithDiff.drop(col("sevs")).alias('a') \
        .join(lastHBAfterExpiredDF.alias('b'), rawModifiedDF["sid"] == lastHBAfterExpiredDF["sid"]) \
        .select(col("a.*"), col("b.hb_arrival_time").alias("last_hb_arrival_time_after_session_expired"),
                col('b.hbseq').alias('last_hb_seq_after_session_expired')).withColumn('year', lit(year)).withColumn('month',
                                                                                                                    lit(
                                                                                                                        month)).withColumn(
        'day', lit(day))

    repartitionedCandidateDF = candidatesHBDF.repartition(1)
    repartitionedCandidateDF.write.partitionBy("year", "month", "day").mode(
        "overwrite").format("csv").option("header", "true").save(expired_session_dir)

    repartitionedMissingHBDF = missingHeartbeatsDF.drop(
        col("sevs")).repartition(1)
    repartitionedMissingHBDF.write.partitionBy("year", "month", "day").mode(
        "overwrite").format("csv").option("header", "true").save(missing_hb_dir)


def generate():
    args = getResolvedOptions(sys.argv, ['JOB_NAME'])
    job = Job(glueContext)
    job.init(args['JOB_NAME'], args)

    try:
        args = getResolvedOptions(sys.argv, [
            'JOB_NAME', 'REPORT_DATE', 'SSD_DEST', 'EXPIRED_SESSION_DEST', 'MISSING_HB_DEST', 'INPUT_RAW_HB_SOURCE',
            'INPUT_SESSION_DATA_SOURCE', 'CSV_SSD_REPORT_NAME_PREFIX'])
    except:
        raise RuntimeError(
            'We need following parameters - \'REPORT_DATE\',\'SSD_DEST\',\'EXPIRED_SESSION_DEST\',\'MISSING_HB_DEST\',\'INPUT_RAW_HB_SOURCE\',\'INPUT_SESSION_DATA_SOURCE\',\'CSV_SSD_REPORT_NAME_PREFIX\'')

    try:
        args_report_date = args['REPORT_DATE']
        report_date = datetime.strptime(args_report_date.strip(), '%Y-%m-%d')
    except:
        report_date = date.today() - timedelta(days=1)
    report_date = datetime.combine(report_date, datetime.min.time())
    input_rawhb_source = args['INPUT_RAW_HB_SOURCE']
    input_session_data_source = args['INPUT_SESSION_DATA_SOURCE']
    ssd_output_dir = args['SSD_DEST']
    missing_hb_dir = args['MISSING_HB_DEST']
    expired_session_dir = args['EXPIRED_SESSION_DEST']
    ssd_csv_file_name_prefix = args['CSV_SSD_REPORT_NAME_PREFIX']

    any_error = ""

    if (not (input_rawhb_source and input_rawhb_source.strip())):
        any_error = any_error + " INPUT_RAW_HB_SOURCE "
    if (not (input_session_data_source and input_session_data_source.strip())):
        any_error = any_error + " INPUT_SESSION_DATA_SOURCE "
    if (not (ssd_output_dir and ssd_output_dir.strip())):
        any_error = any_error + " SSD_DEST "
    if (not (missing_hb_dir and missing_hb_dir.strip())):
        any_error = any_error + " MISSING_HB_DEST "
    if (not (expired_session_dir and expired_session_dir.strip())):
        any_error = any_error + " EXPIRED_SESSION_DEST "
    if (not (ssd_csv_file_name_prefix and ssd_csv_file_name_prefix.strip())):
        any_error = any_error + " CSV_SSD_REPORT_NAME_PREFIX "

    if (any_error):
        any_error = any_error + " paramater(s) are empty"
        raise RuntimeError(any_error)

    year = report_date.year
    month = report_date.strftime('%m')
    day = report_date.strftime('%0d')
    next_day = (report_date + timedelta(days=1)).strftime('%0d')
    input_rawhb_source_with_parition = input_rawhb_source + \
        "/year={}/month={}/day={}/*".format(year, month, day)
    input_session_data_source_with_partition = input_session_data_source + \
        "/year={}/month={}/day={{{},{}}}/".format(
            year, month, day, next_day)  # read data for two days
    sessionsDF = spark.read.json(input_session_data_source_with_partition)
    sessionsDF = sessionsDF.filter((col("end_time") >= "{}".format((report_date + timedelta(hours=8)).strftime('%Y-%m-%dT%H:%M:%SZ'))) & (
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
