import botocore.session

session = botocore.session.get_session()
session.set_credentials(<access_key>,<secret_key>)
client = session.create_client('kinesisanalyticsv2', region_name='us-west-2')

response = client.update_application(ApplicationName=<application_name>,
                                     CurrentApplicationVersionId=<version_no>,
                                      ApplicationConfigurationUpdate=
                                      {
                                              "FlinkApplicationConfigurationUpdate": {
                                                  "CheckpointConfigurationUpdate": {
                                                      "CheckpointingEnabledUpdate": True,
                                                      "CheckpointIntervalUpdate": 60000,
                                                      "ConfigurationTypeUpdate": "CUSTOM",
                                                      "MinPauseBetweenCheckpointsUpdate": 6000
                                                  }
                                              }
                                      }
                        )

print(response)



