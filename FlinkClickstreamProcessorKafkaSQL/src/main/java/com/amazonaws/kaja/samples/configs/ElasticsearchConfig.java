package com.amazonaws.kaja.samples.configs;

public class ElasticsearchConfig implements ConnectorConfig{
    private String endpoint;
    private String index;
    private String docType;
    public ElasticsearchConfig(String endpoint, String index, String docType){
        this.endpoint = endpoint;
        this.index = index;
        this.docType = docType;
    }
    @Override
    public String toDDL() {
        return " 'connector' = 'elasticsearch-6' \n" +
                ",'hosts' = '"+this.endpoint+"' \n" +
                ",'index' = '"+this.index+"' \n" +
                ",'document-type' = '"+this.docType+"'";
    }
}
