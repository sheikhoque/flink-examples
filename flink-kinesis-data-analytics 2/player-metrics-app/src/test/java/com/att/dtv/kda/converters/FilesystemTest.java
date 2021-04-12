package com.att.dtv.kda.converters;

import org.apache.flink.core.fs.FSDataInputStream;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.Path;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

public class FilesystemTest {

    @Test
    void testURI() throws Exception {
        String url = this.getClass().getClassLoader().getResource("conf/cdn_mappings.csv").getPath();
        //String url = "s3://att-dtv-raptor-kda/flink_app_load/cdn_mappings.csv";
        URI uri = URI.create(url);
        FileSystem fs = FileSystem.get(uri);
        FSDataInputStream is = fs.open(new Path(uri));
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        reader.lines().forEach(l -> System.out.println(l));
    }
}
