package com.amazonaws.kaja.samples.configs;

import java.util.Properties;

public final class TopicConfig implements ConnectorConfig{

    private String topicName;
    private String groupId;
    private String bootstrapServers;
    private String schemaUrl;
    public TopicConfig(String topicName, String groupId, String bootstrapServers, String schemaUrl){
        this.topicName = topicName;
        this.groupId = groupId;
        this.bootstrapServers = bootstrapServers;
        this.schemaUrl = schemaUrl;
    }

    public String toDDL() {
        return     " 'connector' = 'kafka' \n" +
                ",'topic' = '"+topicName+"' \n" +
                ",'scan.startup.mode' = 'latest-offset' \n" +
                ",'properties.group.id'= '"+groupId+"' \n" +
                ",'properties.bootstrap.servers' = '"+bootstrapServers+"' \n" +
                ",'format' = 'avro-confluent'\n"+
                ",'avro-confluent.schema-registry.url' = '"+schemaUrl+"'\n"+
                ",'avro-confluent.schema-registry.subject' = '"+topicName+"'\n";
    }
}
