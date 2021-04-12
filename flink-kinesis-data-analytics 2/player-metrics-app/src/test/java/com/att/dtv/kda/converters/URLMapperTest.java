package com.att.dtv.kda.converters;

import com.att.dtv.kda.process.URLMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class URLMapperTest {

    URLMapper m;
    @BeforeAll
    void init() throws IOException {
        Reader in = new FileReader(this.getClass().getClassLoader().getResource("conf/cdn_mappings.csv").getPath());
        Iterable<CSVRecord> records = CSVFormat.EXCEL.withFirstRecordAsHeader().withQuote('"').parse(in);
        URLMapper.Builder builder = new URLMapper.Builder();
        for (CSVRecord record: records) {
            builder.addCdnPatternPair(record.get(0), record.get(1));
        }
        m = builder.build();
    }

    @Test
    void findCdnName() throws URISyntaxException {

        String url1 = "http://C4--147-21-8-0-24-8609.dfwlive-v1-c1p1-sponsored.dfw.vcdn.att-idns.net/Content/HLS.abre/Live/channel(BTVHD-8609.dfw.1080)/index_mobile.m3u8";
        String url2 = "http://csm-e-dfwprd.tls1.yospace.com/csm/extlive/aegdfwprd01,/Content/HLS.abre/Live/channel(WESPNHD-1964.dfw.720)/index_mobile.m3u8?yo.up=http://C4--147-21-8-0-24-1964.dfwlive-v1-c1p1-sponsored.dfw.vcdn.att-idns.net/Content/HLS.abre/Live/channel(WESPNHD-1964.dfw.720)/&comscore_impl_type=a&hdid=Vhn5eVidLLBWlcc0eK/S2g==&dvadid=B372946C-7741-4085-A327-A9DA22B833E6&u=Vhn5eVidLLBWlcc0eK/S2g==&yo.sl=3&d=ios_mobile&comscore_platform=ios&yo.po=3&ltlg=33.93%2C-118.39&ut=gmott&comscore_device=iPhone10,4&_fw_did_idfa=B372946C-7741-4085-A327-A9DA22B833E6&attnid=dfw001&_fw_nielsen_app_id=P7CFE36DB-A8D9-4801-95FE-51C29101342C&nielsen_dev_group=devgrp,PHN&is_lat=0&nielsen_platform=plt,MBL";
        String cdn1 = m.parse(url1).f0;
        String cdn2 = m.parse(url2).f0;
        assertEquals("INHOUSE", cdn1);
        assertEquals("INHOUSE", cdn2);
    }

    @Test
    void findPodName() throws URISyntaxException {
        String url1 = "http://C4--147-21-8-0-24-8609.dfwlive-v1-c1p1-sponsored.dfw.vcdn.att-idns.net/Content/HLS.abre/Live/channel(BTVHD-8609.dfw.1080)/index_mobile.m3u8";
        String url2 = "http://csm-e-dfwprd.tls1.yospace.com/csm/extlive/aegdfwprd01,/Content/HLS.abre/Live/channel(WESPNHD-1964.dfw.720)/index_mobile.m3u8?yo.up=http://C4--147-21-8-0-24-1964.dfwlive-v1-c1p1-sponsored.dfw.vcdn.att-idns.net/Content/HLS.abre/Live/channel(WESPNHD-1964.dfw.720)/&comscore_impl_type=a&hdid=Vhn5eVidLLBWlcc0eK/S2g==&dvadid=B372946C-7741-4085-A327-A9DA22B833E6&u=Vhn5eVidLLBWlcc0eK/S2g==&yo.sl=3&d=ios_mobile&comscore_platform=ios&yo.po=3&ltlg=33.93%2C-118.39&ut=gmott&comscore_device=iPhone10,4&_fw_did_idfa=B372946C-7741-4085-A327-A9DA22B833E6&attnid=dfw001&_fw_nielsen_app_id=P7CFE36DB-A8D9-4801-95FE-51C29101342C&nielsen_dev_group=devgrp,PHN&is_lat=0&nielsen_platform=plt,MBL";
        String wrong_url = "http://csm-e-dfwprd.tls1.yospace.com/csm/extlive/aegdfwprd01,/Content/HLS.abre/Live/channel(WESPNHD-1964.dfw.720)/index_mobile.m3u8?yo.up=http://C4--147-21-8-0-24-1964.dfwlive-v1.sponsored.dfw.vcdn.att-idns.net/-c1p1-Content/HLS.abre/Live/channel(WESPNHD-1964.dfw.720)/&comscor-c1p1-e_impl_type=a&hdid=Vhn5eVidLLBWlcc0eK/S2g==&dvadid=B372946C-7741-4085-A327-A9DA22B833E6&u=Vhn5eVidLLBWlcc0eK/S2g==&yo.sl=3&d=ios_mobile&comscore_platform=ios&yo.po=3&ltlg=33.93%2C-118.39&ut=gmott&comscore_device=iPhone10,4&_fw_did_idfa=B372946C-7741-4085-A327-A9DA22B833E6&attnid=dfw001&_fw_nielsen_app_id=P7CFE36DB-A8D9-4801-95FE-51C29101342C&nielsen_dev_group=devgrp,PHN&is_lat=0&nielsen_platform=plt,MBL";
        String pod1 = m.parse(url1).f1;
        String pod2 = m.parse(url2).f1;
        String pod3 = m.parse(wrong_url).f1;
        assertEquals("Cluster1 POD1", pod1);
        assertEquals("Cluster1 POD1", pod2);
        assertNotEquals("Cluster1 POD1", pod3);
    }

    @Test @Disabled //scratch
    void testYospaceRegex() throws URISyntaxException {
        String urlStr = "http://csm-e-dfwprd.tls1.yospace.com/csm/extlive/aegdfwprd01,/Content/HLS.abre/Live/channel(WESPNHD-1964.dfw.720)/index_mobile.m3u8?yo.up=https%3A%2F%2FC4--147-21-8-0-24-1964.dfwlive-v1-c1p1-sponsored.dfw.vcdn.att-idns.net";
        String query = new URI(urlStr).getQuery();
        Matcher matcher = Pattern.compile("&?yo\\.(a|u)p=").matcher("");
        matcher.reset(query);
        if (matcher.find()) {
            System.out.println(query.substring(matcher.end()));
        }
    }

    @Test
    void testYospace() throws URISyntaxException {
        String urlStr = "http://csm-e-cedpusexaws204j8-5pdum28dek5d.bln1.yospace.com/csm/extlive/aegdfwprd01,/Content/DASH.abre/Live/channel(CHDR-586.dfw.1080)/manifest_tv.mpd?yo.lp=true&yo.up=http://C4--147-21-160-0-24-586.dfwlive-v1-c4p2-sponsored.dfw.vcdn.att-idns.net/Content/DASH.abre/Live/channel(CHDR-586.dfw.1080)/&d=osprey&comscore_device=Android_WNC_AT%26T_TV&comscore_platform=OTT&comscore_impl_type=a&u=gv8U5JeKYXijLeamz0J9mQ%3D%3D&nielsen_dev_group=devgrp%2CSTV&_fw_nielsen_app_id=P7CFE36DB-A8D9-4801-95FE-51C29101342C&nielsen_platform=plt%2COTT&ut=gmott&hhid=gv8U5JeKYXijLeamz0J9mQ%3D%3D&is_lat=0&yo.fr=true&yo.d.cp=true&yo.po=-1&yo.br=false&attnid=dfw001&_fw_did_google_advertising_id=4000e127-6efb-4be6-a39c-4e4fa9ec87e7&dvadid=4000e127-6efb-4be6-a39c-4e4fa9ec87e7&yo.d.bc=-1";
        String cdn = m.parse(urlStr).f0;
        String pod = m.parse(urlStr).f1;
        String host = m.parse(urlStr).f2;
        assertEquals("INHOUSE", cdn);
        assertEquals("Cluster4 POD2", pod);
        assertEquals("csm-e-cedpusexaws204j8-5pdum28dek5d.bln1.yospace.com", host);
        System.out.println("CDN:" + cdn);
        System.out.println("POD:" + pod);
        System.out.println("HOST:" + host);
    }

    @Test
    void test() throws URISyntaxException {
        String urlStr = "";
        String cdn = m.parse(urlStr).f0;
        String pod = m.parse(urlStr).f1;
        System.out.println("CDN:" + cdn);
        System.out.println("POD:" + pod);
    }
}