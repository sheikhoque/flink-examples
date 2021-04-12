import requests, boto3, tarfile, io, os

def lambda_handler(event, context):
    #['GeoLite2-ASN', 'GeoLite2-City']
    maxmind_databases = os.environ['DB_LIST'].split(',')
    #'us-west-2'
    aws_region = os.environ['AWS_DEFAULT_REGION']
    #"maxmind_license_key"
    secret_name = os.environ['MAXMIND_SECRET_NAME']
    #"att-dtv-raptor-kda"
    bucket = os.environ['MAXMIND_S3_BUCKET']
    #"maxmind/"
    prefix = os.environ['MAXMIND_S3_PREFIX']
    license = get_license(secret_name, aws_region)
    for db_name in maxmind_databases:
        print(f"Updating database {db_name}")
        compressed = fetch_maxmind_db(db_name=db_name, license=license)
        fileobj, filename = unpack(compressed)
        s3_upload(fileobj=fileobj, bucket=bucket, key=prefix+filename)

    return {
        'message': "Maxmind databases updated"
    }

def fetch_maxmind_db(db_name, license):
    url = f"https://download.maxmind.com/app/geoip_download?edition_id={db_name}&license_key={license}&suffix=tar.gz"
    response = requests.get(url)
    return io.BytesIO(response.content)

def unpack(fileobj):
    tar = tarfile.open(fileobj=fileobj, mode='r:gz')
    for m in tar.getmembers():
        if m.name.endswith(".mmdb"):
            _, filename = os.path.split(m.name)
            fileobj = tar.extractfile(m)

    return (fileobj, filename)

def get_license(secret_name, aws_region):
    session = boto3.session.Session()
    client = session.client(service_name='secretsmanager', region_name=aws_region)
    get_secret_value_response = client.get_secret_value(SecretId=secret_name)
    if 'SecretString' in get_secret_value_response:
        text_secret_data = get_secret_value_response['SecretString']
    else:
        raise Exception("No license key secret found!")

    return text_secret_data

def s3_upload(fileobj, bucket, key):
    s3_client = boto3.client('s3')
    s3_client.upload_fileobj(fileobj, bucket, key)
