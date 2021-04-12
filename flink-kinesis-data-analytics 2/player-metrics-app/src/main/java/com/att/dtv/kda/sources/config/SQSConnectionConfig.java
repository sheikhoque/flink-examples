package com.att.dtv.kda.sources.config;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class SQSConnectionConfig implements Serializable {

    private static Logger LOG = LoggerFactory.getLogger(SQSConnectionConfig.class);
    private  transient AmazonSQS sqs ;
    private  String queueName;
    private  String queueUrl;
    private  int longPollingSecs=0;

    private SQSConnectionConfig(){}

    public static class SQSConnectionConfigBuilder {
        private  String queueName;
        private int longPollingSecs;
        public SQSConnectionConfigBuilder withQueueName(String queueName){
            this.queueName = queueName;
            return this;
        }

        public SQSConnectionConfigBuilder withLongPolling(int secs){
            this.longPollingSecs = secs;
            return this;
        }

        public SQSConnectionConfig build(){
            SQSConnectionConfig sqsConnectionConfig = new SQSConnectionConfig();
            sqsConnectionConfig.queueName = this.queueName;
            sqsConnectionConfig.longPollingSecs = this.longPollingSecs;
            return sqsConnectionConfig;
        }

    }

    public boolean createConnection() throws Exception {
        try{
            sqs = AmazonSQSClientBuilder.defaultClient();
            queueUrl = sqs.getQueueUrl(this.queueName).getQueueUrl();
            return true;
        }catch(Exception e){
            throw new Exception("Unable to connect to SQS Queue ",e);
        }
    }


    public List<Message> receiveMessageRequest(){
        synchronized (this){
            try{
                ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(this.queueUrl);
                if(longPollingSecs>0){
                    receiveMessageRequest.withWaitTimeSeconds(longPollingSecs);
                }
                return sqs.receiveMessage(receiveMessageRequest).getMessages();
            }catch(Exception e){
                LOG.error("SQSConnectionConfigError: Error in receiving messages from sqs queue",e);
                return Collections.emptyList();
            }
        }

    }

    public boolean deleteMessage(String messageReceiptHandle){
        try{
            sqs.deleteMessage(new DeleteMessageRequest(this.queueUrl, messageReceiptHandle));
            return true;
        }catch(Exception e){
            LOG.error("SQSConnectionConfigError: Error in deleting messages from sqs queue",e);
            return false;
        }
    }

    @Override
    public String toString() {
        return "SQSConnectionConfig{" +
                "queueName='" + queueName + '\'' +
                ", queueUrl='" + queueUrl + '\'' +
                '}';
    }
}
