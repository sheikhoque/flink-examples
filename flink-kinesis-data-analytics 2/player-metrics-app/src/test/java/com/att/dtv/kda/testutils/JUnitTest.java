package com.att.dtv.kda.testutils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class JUnitTest {

    @Test @Tag("Hello") public void test2() {
        System.out.println("Test");
    }

    @BeforeEach public void f(TestInfo info) {
        System.out.println(info.getTags());
    }
}