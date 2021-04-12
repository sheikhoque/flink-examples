    /*
    * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
    *
    * Licensed under the Apache License, Version 2.0 (the "License"). You may
    * not use this file except in compliance with the License. A copy of the
    * License is located at
    *
    *    http://aws.amazon.com/apache2.0/
    *
    * or in the "license" file accompanying this file. This file is distributed
    * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
    * express or implied. See the License for the specific language governing
    * permissions and limitations under the License.
    */
    package com.att.dtv.kda.sinks;
    import com.amazonaws.auth.AWSCredentialsProvider;
    import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
    import com.google.common.base.Supplier;
    import org.apache.commons.lang3.StringUtils;
    import org.apache.flink.api.common.functions.RuntimeContext;
    import org.apache.flink.api.java.tuple.Tuple3;
    import org.apache.flink.api.java.tuple.Tuple4;
    import org.apache.flink.streaming.connectors.elasticsearch.ActionRequestFailureHandler;
    import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkBase;
    import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
    import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer;
    import org.apache.flink.streaming.connectors.elasticsearch6.ElasticsearchSink;
    import org.apache.flink.util.ExceptionUtils;
    import org.apache.http.*;
    import org.apache.http.auth.AuthScheme;
    import org.apache.http.auth.AuthState;
    import org.apache.http.auth.UsernamePasswordCredentials;
    import org.apache.http.client.protocol.HttpClientContext;
    import org.apache.http.impl.auth.BasicSchemeFactory;
    import org.apache.http.params.BasicHttpParams;
    import org.apache.http.protocol.HttpContext;
    import org.elasticsearch.action.ActionRequest;
    import org.elasticsearch.action.index.IndexRequest;
    import org.elasticsearch.client.Requests;
    import org.elasticsearch.common.breaker.CircuitBreakingException;
    import org.elasticsearch.common.util.concurrent.EsRejectedExecutionException;
    import org.elasticsearch.common.xcontent.XContentType;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import vc.inreach.aws.request.AWSSigner;
    import vc.inreach.aws.request.AWSSigningRequestInterceptor;
    import java.io.IOException;
    import java.io.Serializable;
    import java.net.SocketTimeoutException;
    import java.time.LocalDateTime;
    import java.time.ZoneOffset;
    import java.util.Arrays;
    import java.util.List;
    public class AmazonElasticsearchSink {
        private static final String ES_SERVICE_NAME = "es";
        private static final String ES_502_ERROR_MSG="502 Bad Gateway";
        private static final String ES_CBE_DATA_TOO_LARGE="Data too large, data for";
        private static final String ES_CBE_WRITE_TO_READONLY_INDEX="FORBIDDEN/5/index read-only";
        private static final String TYPE = "_doc";
        private static final Logger LOG = LoggerFactory.getLogger(AmazonElasticsearchSink.class);
        public static <T extends Tuple4<String, String, String, String>> ElasticsearchSink<T> buildElasticsearchSink(String elasticsearchEndpoint, String region,
                                                                                                                     String userName, String password, int esFlushSize, int esBackOffDelayMillis, int esBackOffRetryNumber, int esFlushMaxActions) {
            LOG.info("AmazonElasticsearchSink.buildElasticsearchSink - started!");
            final List<HttpHost> httpHosts = Arrays.asList(HttpHost.create(elasticsearchEndpoint));
            final SerializableAWSSigningRequestInterceptor requestInterceptor = new SerializableAWSSigningRequestInterceptor(
                    region);
            final SerializableBasicAuthRequestInceptor serializableBasicAuthRequestInceptor = new SerializableBasicAuthRequestInceptor(userName, password);
            ElasticsearchSink.Builder<T> esSinkBuilder = new ElasticsearchSink.Builder<>(httpHosts,
                    new ElasticsearchSinkFunction<T>() {
                        public IndexRequest createIndexRequest(T element) {
                            if (element.f0 == null || element.f0.isEmpty()) {
                                return Requests.indexRequest().index(element.f3 + (element.f2 == null ? "" : "_" + element.f2)).type(TYPE).source(element.f1,
                                        XContentType.JSON);
                            } else {
                                return Requests.indexRequest().index(element.f3 + (element.f2 == null ? "" : "_" + element.f2)).type(TYPE).id(element.f0).source(element.f1,
                                        XContentType.JSON);
                            }
                        }
                        @Override
                        public void process(T element, RuntimeContext ctx, RequestIndexer indexer) {
                            indexer.add(createIndexRequest(element));
                            LOG.debug("Sent data to indexer: {}", element);
                        }
                    });
            esSinkBuilder.setBulkFlushMaxActions(esFlushMaxActions);
            //esSinkBuilder.setBulkFlushInterval(FLUSH_INTERVAL_MILLIS);
            esSinkBuilder.setBulkFlushMaxSizeMb(esFlushSize);
            esSinkBuilder.setBulkFlushBackoff(true);
            esSinkBuilder.setBulkFlushBackoffRetries(esBackOffRetryNumber);
            esSinkBuilder.setBulkFlushBackoffDelay(esBackOffDelayMillis);
            esSinkBuilder.setBulkFlushBackoffType(ElasticsearchSinkBase.FlushBackoffType.EXPONENTIAL);
            if(StringUtils.EMPTY.equalsIgnoreCase(userName) && StringUtils.EMPTY.equalsIgnoreCase(password)){
                esSinkBuilder.setRestClientFactory(restClientBuilder -> restClientBuilder
                        .setHttpClientConfigCallback(callback -> callback.addInterceptorLast(requestInterceptor)));
            }else{
                esSinkBuilder.setRestClientFactory(restClientBuilder ->
                        restClientBuilder.setHttpClientConfigCallback(callback->
                                callback.addInterceptorLast(serializableBasicAuthRequestInceptor)));
            }
            esSinkBuilder.setFailureHandler(new ActionRequestFailureHandler()  {
                private static final long serialVersionUID = -7193150610642632242L;
                @Override
                public void onFailure(ActionRequest action, Throwable failure, int restStatusCode, RequestIndexer indexer) throws Throwable {
                    if (ExceptionUtils.findThrowable(failure, EsRejectedExecutionException.class).isPresent()
                            || ExceptionUtils.findThrowable(failure, SocketTimeoutException.class).isPresent()
                            || ExceptionUtils.findThrowable(failure, CircuitBreakingException.class).isPresent()
                            || ExceptionUtils.findThrowableWithMessage(failure, ES_502_ERROR_MSG).isPresent()
                            || ExceptionUtils.findThrowableWithMessage(failure, ES_CBE_DATA_TOO_LARGE).isPresent()
                    ) {
                        LOG.error("Error from ES - Retry: {}\n Response Code: {}\n Stack: {}", failure.getMessage(), restStatusCode, failure.getStackTrace());
                        indexer.add(action);
                    } else if(ExceptionUtils.findThrowableWithMessage(failure, ES_CBE_WRITE_TO_READONLY_INDEX).isPresent()){
                        return;
                    }else {
                        LOG.error("Error from ES - throw: {}\n Response Code: {}\n Stack: {} , throwing the error to let application fail.", failure.getMessage(), restStatusCode, failure.getStackTrace());
                        throw failure;
                    }
                }
            });
            ElasticsearchSink<T> elasticsearchSink = esSinkBuilder.build();
            elasticsearchSink.disableFlushOnCheckpoint();
            return elasticsearchSink;

        }
        static class SerializableBasicAuthRequestInceptor implements HttpRequestInterceptor,Serializable {
            private final String userName;
            private final String password;
            private transient HttpRequestInterceptor httpRequestInterceptor;
            private transient UsernamePasswordCredentials credentials;
            public SerializableBasicAuthRequestInceptor(String userName, String passWord){
                this.userName = userName;
                this.password= passWord;
            }
            @Override
            public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
                if(httpRequestInterceptor==null){
                    credentials = new UsernamePasswordCredentials(userName, password);
                    httpRequestInterceptor = new BasicInterceptor(credentials);
                }
                httpRequestInterceptor.process(httpRequest,httpContext);
            }
        }
        static class SerializableAWSSigningRequestInterceptor implements HttpRequestInterceptor, Serializable {
            private final String region;
            private transient AWSSigningRequestInterceptor requestInterceptor;
            public SerializableAWSSigningRequestInterceptor(String region) {
                this.region = region;
            }
            @Override
            public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
                if (requestInterceptor == null) {
                    final Supplier<LocalDateTime> clock = () -> LocalDateTime.now(ZoneOffset.UTC);
                    final AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();
                    final AWSSigner awsSigner = new AWSSigner(credentialsProvider, region, ES_SERVICE_NAME, clock);
                    requestInterceptor = new AWSSigningRequestInterceptor(awsSigner);
                }
                requestInterceptor.process(httpRequest, httpContext);
            }
        }
        private static class BasicInterceptor implements HttpRequestInterceptor {
            private UsernamePasswordCredentials credentials;
            public BasicInterceptor(UsernamePasswordCredentials credentials) {
                this.credentials = credentials;
            }
            @Override
            public void process(HttpRequest request, HttpContext context)throws HttpException, IOException {
                AuthState authState = (AuthState) context.getAttribute(HttpClientContext.TARGET_AUTH_STATE);
                if (authState != null && authState.getAuthScheme() == null) {
                    AuthScheme scheme = new BasicSchemeFactory().newInstance(new BasicHttpParams());
                    authState.setAuthScheme(scheme);
                    authState.setCredentials(credentials);
                }
            }
        }
    }
