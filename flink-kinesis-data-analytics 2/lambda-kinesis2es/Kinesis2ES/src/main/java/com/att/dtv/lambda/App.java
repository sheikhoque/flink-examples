package com.att.dtv.lambda;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.*;
import java.util.function.Function;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<KinesisEvent, Void> {

    private static final String SERVICE_NAME = "es";
    private static String region;
    private static String esEndpoint; // e.g. https://search-mydomain.us-west-1.es.amazonaws.com
    private static String index;

    static final AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();

    @Override
    public Void handleRequest(KinesisEvent event, Context context) {
        esEndpoint = System.getenv().get("ES_ENDPOINT");
        if (esEndpoint == null)
            paramsErrorHandler.apply("ES_ENDPOINT");

        index = System.getenv().getOrDefault("ES_INDEX", "raw") + "_" + buildIndexSuffix();
        region = System.getenv().getOrDefault("AWS_DEFAULT_REGION", "us-west-2");

        RestHighLevelClient esClient = esClient(SERVICE_NAME, region);

        if (event.getRecords().size() > 1) {
            BulkRequest request = new BulkRequest();
            for(KinesisEvent.KinesisEventRecord record : event.getRecords()) {
                request.add(new IndexRequest(index).source(record.getKinesis().getData().array(), XContentType.JSON));
            }
            try {
                esClient.bulk(request, RequestOptions.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                KinesisEvent.KinesisEventRecord record = event.getRecords().get(0);
                esClient.index(new IndexRequest(index).source(record.getKinesis().getData().array(), XContentType.JSON), RequestOptions.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private Function<String, String> paramsErrorHandler = variable -> {
        throw new IllegalArgumentException("Environment variable " + variable + " should be provided");
    };

    // Adds the interceptor to the ES REST client
    public static RestHighLevelClient esClient(String serviceName, String region) {
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName(serviceName);
        signer.setRegionName(region);
        HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(serviceName, signer, credentialsProvider);
        return new RestHighLevelClient(RestClient.builder(HttpHost.create(esEndpoint)).setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
    }


    public static String buildIndexSuffix() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE;
        LocalDate date = LocalDate.now();
        String convertedDate = date.format(dateTimeFormatter);
        return convertedDate.replace("-",".");
    }
}
