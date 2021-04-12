package com.att.dtv.kda.testutils;

import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.core.fs.FileStatus;

public class TestFileInputFormat extends TextInputFormat {


    public TestFileInputFormat() {
        super(null);
        super.enumerateNestedFiles = true;
    }

    public boolean acceptFile(FileStatus fileStatus) {
        final String name = fileStatus.getPath().getName();
        return !name.startsWith("_");
    }
}
