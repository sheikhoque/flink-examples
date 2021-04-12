import boto3, os

def lambda_handler(event, context):
    print("Event received: " + str(event))
    CMD = {'GeoLite2-City.mmdb': 1, 'GeoLite2-ASN.mmdb': 2}
    SQS_QUEUE_NAME = os.getenv("QUEUE_NAME")
    sqs_client = boto3.client('sqs')
    url = sqs_client.get_queue_url(QueueName=SQS_QUEUE_NAME).get('QueueUrl')

    for record in event['Records']:
        if record['eventSource'] == 'aws:s3' and record['eventName'] in ["ObjectCreated:Put", "ObjectCreated:CompleteMultipartUpload"]:
            bucket = record['s3']['bucket']['name']
            key = record['s3']['object']['key']
            _, filename = os.path.split(key)
            print("Database file:" + filename)
            if filename in CMD:
                data = f"{{'code': {CMD[filename]}, 'arg': 's3://{bucket}/{key}'}}"
                resp = sqs_client.send_message(QueueUrl=url, MessageBody=data)
                print(resp)

    return {'message': resp}
