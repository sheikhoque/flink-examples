package com.att.dtv.kda.factory;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class SupportedProtocol {

    public enum TYPE {LOCALFS, HDFS, S3, HTTP, HTTPS, KINESIS, FIREHOSE, ES, ES_SECURED, DUMMY, UNSUPPORTED, SQS};

    private final List<String> supportedSchemas = Arrays.asList("file", "hdfs", "s3","s3a", "http", "https", "es", "ess", "kinesis", "firehose", "dummy","sqs");
    private TYPE type;
    private String endpoint;

    public SupportedProtocol(URI uri) {
        if (uri == null)
            throw new IllegalArgumentException("URI can't be empty or null");
        else if (uri.getScheme() == null || !supportedSchemas.contains(uri.getScheme()))
            throw new IllegalArgumentException("URI should have one of following schemas: file, hdfs, s3, http[s], es[s], kinesis, firehose, dummy, sqs");
        else if (uri.getPath() == null)
            throw new IllegalArgumentException("URI should have path component");

        switch (uri.getScheme()) {
            case "file":
                type = TYPE.LOCALFS;
                String path = uri.getPath();
                if (".".equals(uri.getAuthority())) {
                    //the path is relative to runtime directory
                    URL url = SupportedProtocol.class.getClassLoader().getResource(".");
                    path = url.getPath() + path;
                }
                endpoint = "file://" + path;
                break;
            case "http":
                type = TYPE.HTTP;
                endpoint = uri.toString();
                break;
            case "https":
                type = TYPE.HTTPS;
                endpoint = uri.toString();
                break;
            case "hdfs":
                type = TYPE.HDFS;
                endpoint = uri.toString();
                break;
            case "s3a":
            case "s3":
                type = TYPE.S3;
                endpoint = uri.toString();
                break;
            case "kinesis":
                type = TYPE.KINESIS;
                endpoint = uri.getAuthority();
                break;
            case "firehose":
                type = TYPE.FIREHOSE;
                endpoint = uri.getAuthority();
                break;
            case "es":
                type = TYPE.ES;
                endpoint = "http:" + uri.getRawSchemeSpecificPart();
                break;
            case "ess":
                type = TYPE.ES_SECURED;
                endpoint = "https:" + uri.getRawSchemeSpecificPart();
                break;
            case "sqs":
                type = TYPE.SQS;
                endpoint = uri.getAuthority();
                break;
            case "dummy":
                type = TYPE.DUMMY;
                endpoint = "dummy";
                break;
            default:
                type = TYPE.UNSUPPORTED;
                endpoint = "dummy";
        }
    }

    public TYPE getProtocolType() {
        return type;
    }

    public String getEndpoint() {
        return endpoint;
    }


}
