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
package com.att.dtv.kda.process;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLMapper implements Serializable {

    private static long serialVersionUID = 56890688400290374L;
    private static Logger LOG = LoggerFactory.getLogger(URLMapper.class);


    private Pattern podPattern = Pattern.compile("(?<pod>-c\\dp\\d-)");
    private Map<String, Object> cdnMap;

    private URLMapper(Map<String, Object> map) {
        this.cdnMap = map;
    }

    public static class Builder {

        Map<String, Object> cdnMap = new HashMap<>();
        public Builder addCdnPatternPair(String key, String cdn) {
            if (cdn == null || key == null || cdn.equals("") || key.equals(""))
                throw new IllegalArgumentException("Neither cdn value nor key string can be null or empty");
            // if it is URL query parameter then lets construct matcher object for it
            if (cdn.startsWith("&")) {
                cdnMap.putIfAbsent(key, Pattern.compile(cdn));
            } else {
                cdnMap.putIfAbsent(key, cdn);
            }
            return this;
        }

        public URLMapper build() {
            URLMapper mapper = new URLMapper(cdnMap);
            return mapper;
        }
    }

    public Tuple3<String, String, String> parse(String url) {
        String cdn = "N/A";
        String pod = "N/A";
        String host = "N/A";
        Tuple3<String, String, String> result = Tuple3.of(cdn, pod, host);
        URI uri;

        if (url == null || StringUtils.EMPTY.equals(url))
            return result;
        else
            try {
                uri = new URI(url);
            } catch (URISyntaxException e) {
                return result;
            }


        host = uri.getHost();
        if(StringUtils.isBlank(host)){
            LOG.debug("URLMapperError: host empty {}", url);
            return result;
        }
        String topLevelDomain = getTopLevelDomain(host);

        if (cdnMap.get(topLevelDomain) != null) {
            if (cdnMap.get(topLevelDomain) instanceof String) {
                cdn = (String)cdnMap.get(topLevelDomain);
                result = Tuple3.of(cdn, getPodName(host), host);
            }
            else if (cdnMap.get(topLevelDomain) instanceof Pattern) {
                Pattern pattern = (Pattern)cdnMap.get(topLevelDomain);
                String refUrl = getParamValue(uri.getQuery(), pattern);
                result = parse(refUrl);
                result = Tuple3.of(result.f0, result.f1, host);
            }
        }

        return result;
    }

    private String getParamValue(String query, Pattern pattern) {
        int start = regexIndexOf(query, pattern);
        if (start == -1)
            return null;
        int end = query.indexOf('&', start);
        end = (end == -1 ? query.length() : end);
        return query.substring(start, end);
    }

    private String getTopLevelDomain(String address) {
        int foundCount = 0;
        for(int i = address.length() - 1; i >= 0; i--)
            if (address.charAt(i) == '.')
                if (++foundCount == 2)
                    return address.substring(i + 1);
        return null;
    }

    private String getPodName(String url) {
        Matcher m = podPattern.matcher(url);
        String podName = null;
        if (m.find()) {
            podName = m.group("pod");
        }
        return podName == null ? "N/A" : podName.replaceAll("-", "")
                .replace("c", "Cluster").replace("p", " POD");
    }

    private int regexIndexOf(String string, Pattern pattern) {
        Matcher matcher = pattern.matcher(string);
        if (matcher.find()) {
            return matcher.end();
        }
        else
            return -1;
    }
}
