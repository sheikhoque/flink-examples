import unittest, json, boto3

class MyTestCase(unittest.TestCase):
    aws_region = 'us-west-2'
    secret_name = 'maxmind_license_key'
    def test_secret_manager(self):
        session = boto3.session.Session(profile_name='att')
        client = session.client(service_name='secretsmanager', region_name=self.aws_region)
        get_secret_value_response = client.get_secret_value(SecretId=self.secret_name)
        print(get_secret_value_response['SecretString'])

if __name__ == '__main__':
    unittest.main()
