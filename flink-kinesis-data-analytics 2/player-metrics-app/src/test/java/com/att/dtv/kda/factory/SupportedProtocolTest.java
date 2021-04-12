package com.att.dtv.kda.factory;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SupportedProtocolTest {

    @Test
    void testLOCALFS() throws URISyntaxException {
        URI uri = new URI("file:///conf/app_properties.json");
        SupportedProtocol supportedProtocol = new SupportedProtocol(uri);
        assertEquals(supportedProtocol.getProtocolType(), SupportedProtocol.TYPE.LOCALFS);
        assertEquals("file:///conf/app_properties.json", supportedProtocol.getEndpoint());
    }

    @Test
    void testRelativeLOCALFS() throws URISyntaxException {
        URI uri = new URI("file://./conf/app_properties.json");
        SupportedProtocol supportedProtocol = new SupportedProtocol(uri);
        String runtimePath = this.getClass().getClassLoader().getResource(".").getPath();
        assertEquals(supportedProtocol.getProtocolType(), SupportedProtocol.TYPE.LOCALFS);
        assertEquals("file://" + runtimePath + "/conf/app_properties.json", supportedProtocol.getEndpoint());
    }

    @Test
    void testHDFS() throws URISyntaxException {
        URI uri = new URI("hdfs://conf/app_properties.json");
        SupportedProtocol supportedProtocol = new SupportedProtocol(uri);
        assertEquals(supportedProtocol.getProtocolType(), SupportedProtocol.TYPE.HDFS);
        assertEquals("hdfs://conf/app_properties.json", supportedProtocol.getEndpoint());
    }

    @Test
    void testS3() throws URISyntaxException {
        URI uri = new URI("s3://conf/app_properties.json");
        SupportedProtocol supportedProtocol = new SupportedProtocol(uri);
        assertEquals(supportedProtocol.getProtocolType(), SupportedProtocol.TYPE.S3);
        assertEquals("s3://conf/app_properties.json", supportedProtocol.getEndpoint());
    }

    @Test
    void testS3a() throws URISyntaxException {
        URI uri = new URI("s3a://conf/");
        SupportedProtocol supportedProtocol = new SupportedProtocol(uri);
        assertEquals(supportedProtocol.getProtocolType(), SupportedProtocol.TYPE.S3);
        assertEquals("s3a://conf/", supportedProtocol.getEndpoint());
    }

    @Test
    void testHTTP() throws URISyntaxException {
        URI uri = new URI("http://conf/app_properties.json");
        SupportedProtocol supportedProtocol = new SupportedProtocol(uri);
        assertEquals(supportedProtocol.getProtocolType(), SupportedProtocol.TYPE.HTTP);
        assertEquals("http://conf/app_properties.json", supportedProtocol.getEndpoint());
    }

    @Test
    void testKINESIS() throws URISyntaxException {
        URI uri = new URI("kinesis://stream");
        SupportedProtocol supportedProtocol = new SupportedProtocol(uri);
        assertEquals(supportedProtocol.getProtocolType(), SupportedProtocol.TYPE.KINESIS);
        assertEquals("stream", supportedProtocol.getEndpoint());
    }

    @Test
    void testError() throws URISyntaxException {
        URI uri = new URI("fil://file");
        assertThrows(java.lang.IllegalArgumentException.class, () -> {
            new SupportedProtocol(uri);
        });
    }

    @Test
    void URItest() throws URISyntaxException {
        URI uri = new URI("file://./conf/app_properties.json");
        System.out.println(uri.getPath());
        System.out.println(uri.getRawPath());
        System.out.println(uri.getAuthority());
        System.out.println(uri.getRawSchemeSpecificPart());
    }
}