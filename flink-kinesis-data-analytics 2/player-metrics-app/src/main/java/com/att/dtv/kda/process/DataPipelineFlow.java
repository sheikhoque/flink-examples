package com.att.dtv.kda.process;

import com.att.dtv.kda.converters.SessionStatsToESModelConverter;
import com.att.dtv.kda.factory.CacheFactory;
import com.att.dtv.kda.factory.SinkFactory;
import com.att.dtv.kda.factory.SourceFactory;
import com.att.dtv.kda.model.app.ControlCommand;
import com.att.dtv.kda.model.app.Heartbeat;
import com.att.dtv.kda.model.app.SessionStatsAggr;
import com.att.dtv.kda.model.app.VideoPlayerStatsProperties;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.IspResponse;
import com.maxmind.geoip2.record.Location;
import com.maxmind.geoip2.record.Subdivision;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.att.dtv.kda.util.IPAddressUtil;
import java.net.InetAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class DataPipelineFlow {
    private StreamExecutionEnvironment streamExecutionEnvironment;
    private VideoPlayerStatsProperties videoPlayerStatsProperties;
    private DataStream<Heartbeat> inputStream;
    private SinkFunction<Tuple4<String, String, String, String>> bufferSink;
    private BroadcastStream<ControlCommand> controlStream;
    final static GsonBuilder builder = new GsonBuilder();
    final static Gson gson = builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();


    private static Logger LOG = LoggerFactory.getLogger(DataPipelineFlow.class);

    public DataPipelineFlow(StreamExecutionEnvironment streamExecutionEnvironment, VideoPlayerStatsProperties videoPlayerStatsProperties) {

        this.videoPlayerStatsProperties = videoPlayerStatsProperties;
        this.streamExecutionEnvironment = streamExecutionEnvironment;
    }

    public void prepareFlow() {
        controlStream = SourceFactory.getBroadcastStream(streamExecutionEnvironment, videoPlayerStatsProperties, ControlCommand.class);
        inputStream = SourceFactory.getInputDataStream(streamExecutionEnvironment, videoPlayerStatsProperties, Heartbeat.class).uid("appSource01").name("appSource");
        this.bufferSink = SinkFactory.getIntervalStatsSink(videoPlayerStatsProperties);

        SingleOutputStreamOperator<SessionStatsAggr> transformedData = dataTransformationStage(videoPlayerStatsProperties, inputStream);

        transformedData.map(getBufferModelMapper()).uid("ssa-model-convertor-buffer-id").name("ssa-model-convertor-buffer")
                .addSink(this.bufferSink).uid("ssa-buffer-sink-id").name("ssa-buffer-sink");
    }

    private SingleOutputStreamOperator<SessionStatsAggr> dataTransformationStage(VideoPlayerStatsProperties videoPlayerStatsProperties, DataStream<Heartbeat> heartbeatDataStream) {

        KeySelector<Heartbeat, Tuple3<String, Long, String>> keyBuilder = new KeySelector<Heartbeat, Tuple3<String, Long, String>>() {
            private static final long serialVersionUID = 6598119592182548520L;

            final HashMap<Integer, Integer> magicMap = new HashMap<Integer, Integer>() {{
                put(140,2097580);
                put(182,3503854);
                put(43,5428623);
                put(94,18521428);
                put(228,26914990);
                put(50,29855788);
                put(239,32948112);
                put(61,56210681);
                put(42,64364827);
                put(147,65627103);
                put(198,69396315);
                put(235,69401723);
                put(20,69691622);
                put(57,75488933);
                put(214,78825635);
                put(117,82827264);
                put(83,95622262);
                put(180,98125124);
                put(8,112353616);
                put(12,115086292);
                put(168,128199202);
                put(215,147517151);
                put(32,148860017);
                put(122,178906418);
                put(238,180317704);
                put(161,210245741);
                put(104,237817639);
                put(67,258682419);
                put(123,264170005);
                put(21,265861453);
                put(66,273065364);
                put(101,276367494);
                put(23,285723425);
                put(28,287053231);
                put(211,289005894);
                put(98,299941960);
                put(187,303717906);
                put(225,311812214);
                put(206,329129516);
                put(106,329797270);
                put(33,346896314);
                put(202,348561091);
                put(119,359415915);
                put(184,376274229);
                put(14,377896702);
                put(81,380540589);
                put(56,384742802);
                put(231,385455960);
                put(186,391819874);
                put(135,395464661);
                put(54,428798744);
                put(188,432375624);
                put(30,433239486);
                put(236,445458408);
                put(163,458671133);
                put(197,481847493);
                put(9,493616324);
                put(96,500538128);
                put(29,504060717);
                put(46,511810872);
                put(207,526412691);
                put(31,529660809);
                put(130,533112838);
                put(195,536528952);
                put(232,543365662);
                put(175,576042870);
                put(35,577246183);
                put(242,588478639);
                put(245,601825466);
                put(210,605870395);
                put(152,615457449);
                put(120,640238040);
                put(112,656974510);
                put(5,658920078);
                put(80,671397825);
                put(62,672737577);
                put(144,673846728);
                put(148,674649281);
                put(95,678085277);
                put(250,679696933);
                put(79,706068596);
                put(139,716077879);
                put(192,719526926);
                put(16,731707611);
                put(183,735046619);
                put(164,735558176);
                put(213,737339338);
                put(90,759555692);
                put(49,774677215);
                put(252,781570165);
                put(151,787214020);
                put(220,808213909);
                put(158,810211850);
                put(167,816709645);
                put(38,820832149);
                put(121,836997596);
                put(216,846920622);
                put(145,863528300);
                put(73,881658028);
                put(176,889053983);
                put(203,896696660);
                put(255,926444864);
                put(102,936522523);
                put(19,946540470);
                put(243,961327101);
                put(124,964680086);
                put(18,971647191);
                put(88,979918470);
                put(162,994634124);
                put(65,1002578245);
                put(27,1005535393);
                put(115,1013636260);
                put(241,1024410127);
                put(227,1057220810);
                put(44,1059801238);
                put(34,1072330806);
                put(126,1091133510);
                put(93,1094290573);
                put(237,1095691232);
                put(75,1117316237);
                put(157,1123110065);
                put(72,1131735311);
                put(133,1143116181);
                put(53,1181307056);
                put(212,1185874086);
                put(141,1194265185);
                put(146,1241181845);
                put(230,1241342326);
                put(179,1243356416);
                put(201,1244290470);
                put(155,1250253480);
                put(171,1258974034);
                put(59,1264374138);
                put(7,1270567584);
                put(208,1277023795);
                put(89,1278190940);
                put(68,1297135353);
                put(159,1299073627);
                put(47,1305971615);
                put(55,1306263860);
                put(136,1370511396);
                put(39,1388136349);
                put(114,1397602629);
                put(4,1404676353);
                put(224,1409113152);
                put(209,1424603091);
                put(25,1426600130);
                put(254,1431024862);
                put(3,1447778791);
                put(15,1454331743);
                put(244,1474113044);
                put(86,1494221479);
                put(129,1503357142);
                put(100,1515636689);
                put(10,1575118165);
                put(189,1591300793);
                put(138,1606505881);
                put(217,1607976218);
                put(191,1614916052);
                put(142,1615685514);
                put(87,1654281790);
                put(234,1654310571);
                put(78,1657411866);
                put(92,1679867256);
                put(64,1699278377);
                put(199,1711678314);
                put(2,1715700668);
                put(107,1716893413);
                put(60,1767619766);
                put(37,1790589615);
                put(110,1799067770);
                put(125,1806625550);
                put(45,1834171637);
                put(132,1868410928);
                put(71,1907555171);
                put(165,1911173458);
                put(223,1916592505);
                put(51,1917216694);
                put(154,1924552570);
                put(36,1925959354);
                put(1,2009592756);
                put(246,2019734703);
                put(40,2041127623);
                put(127,2045299627);
                put(118,2048785862);
                put(221,2059047679);
                put(178,2059807265);
                put(103,2088535793);
                put(204,2093916770);
                put(128,2099185517);
                put(109,2109592745);
                put(58,2119202330);
                put(116,2125509303);
                put(181,2130437910);
                put(113,-2142836841);
                put(0,-2089875627);
                put(17,-2022260829);
                put(173,-2021103479);
                put(137,-1983913244);
                put(156,-1976027977);
                put(69,-1959224209);
                put(185,-1920925863);
                put(248,-1912877633);
                put(13,-1901551410);
                put(150,-1879956614);
                put(26,-1836268903);
                put(91,-1821146062);
                put(85,-1818537951);
                put(11,-1749553303);
                put(131,-1739195452);
                put(172,-1706598974);
                put(226,-1674332378);
                put(177,-1648266088);
                put(193,-1643503671);
                put(219,-1641524631);
                put(160,-1637466872);
                put(200,-1636813928);
                put(41,-1622912537);
                put(196,-1605760322);
                put(108,-1604408065);
                put(190,-1590866521);
                put(153,-1585955732);
                put(134,-1562825290);
                put(233,-1543390876);
                put(63,-1498415675);
                put(166,-1491055200);
                put(253,-1458160847);
                put(143,-1457887676);
                put(205,-1436929999);
                put(149,-1433461843);
                put(229,-1390677152);
                put(170,-1354876795);
                put(22,-1266833763);
                put(194,-1236491690);
                put(74,-1228195400);
                put(84,-1202926508);
                put(169,-1180689811);
                put(222,-1156851494);
                put(24,-1102643666);
                put(99,-1101901791);
                put(48,-1037311534);
                put(52,-976679309);
                put(249,-957434940);
                put(251,-906229738);
                put(76,-882022510);
                put(6,-830400012);
                put(82,-828592331);
                put(218,-765686332);
                put(77,-762152117);
                put(70,-755305593);
                put(111,-693080568);
                put(174,-662365401);
                put(247,-602275081);
                put(240,-349413100);
                put(97,-289301222);
                put(105,-104082194);
            }};

            @Override
            public Tuple3<String, Long, String> getKey(Heartbeat hb) {
                int shardNum = Integer.valueOf(hb.getPartitionId().substring(8)) % 256;
                return new Tuple3(hb.getViewerId(), hb.getSessionId(), hb.getPartitionId()) {
                    @Override
                    public int hashCode() {
                        return magicMap.get(shardNum);
                    }
                };
            }
        };

        TypeInformation<SessionStatsAggr> typeHint =
                TypeInformation.of(new TypeHint<SessionStatsAggr>() {
                });
        SingleOutputStreamOperator<SessionStatsAggr> output = heartbeatDataStream.assignTimestampsAndWatermarks(new PeriodicWatermarkAssigner(videoPlayerStatsProperties.getSessionTimeout()))
                .filter(hb -> hb != null).name("filterNotNull")
                .keyBy(keyBuilder)
                .process(new HeartbeatProcessFunction(videoPlayerStatsProperties))
                .uid("id01").name("metricsProcessing")
                .returns(typeHint)
                .connect(controlStream)
                .process(new BroadcastProcessFunction<SessionStatsAggr, ControlCommand, SessionStatsAggr>() {

                    URLMapper urlMapper;
                    Map<Integer,String> dmaMapper;
                    DatabaseReader geoIpCityReader;
                    DatabaseReader geoIpISPReader;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        LOG.debug("Initializing GeoLocation\\CDN cache");
                        super.open(parameters);
                        urlMapper = CacheFactory.getURLMapper(videoPlayerStatsProperties);
                        dmaMapper = CacheFactory.getDMAMappings(videoPlayerStatsProperties);
                        geoIpCityReader = CacheFactory.getGeoIpCityReader(videoPlayerStatsProperties);
                        geoIpISPReader = CacheFactory.getGeoIpISPReader(videoPlayerStatsProperties);
                        LOG.debug("GeoLocation\\CDN cache initialized");
                    }

                    @Override
                    public void processElement(SessionStatsAggr value, ReadOnlyContext ctx, Collector<SessionStatsAggr> out) throws Exception {
                        Tuple3<String, String, String> cdnDetails;
                        Heartbeat hb;
                        byte[] rawIp;
//                        try {
                        if ((hb = value.getMetaHeartbeat()) != null) {
                            try {
                                cdnDetails = urlMapper.parse(hb.getUrl());
                                value.setCdn(cdnDetails.f0);
                                value.setPod(cdnDetails.f1);
                                value.setHostName(cdnDetails.f2);
                            } catch (Exception e) {
                                LOG.warn("Skipping CDN/POD enrichment due to  error {}", e.getMessage());
                                value.setCdn("N/A");
                                value.setPod("N/A");
                            }
                            try {
                                if (hb.getIpAddress() != null && (rawIp = IPAddressUtil.textToNumericFormatV4(hb.getIpAddress())) != null) {
                                    // we know that textToNumericFormatV4 returns correct IPv4 address and rawIp is not null
                                    InetAddress ipAddress = InetAddress.getByAddress(rawIp);
                                    if (!(ipAddress.isLoopbackAddress() || ipAddress.isSiteLocalAddress() || ipAddress.isLinkLocalAddress())) {
                                        CityResponse cityResponse = geoIpCityReader.city(ipAddress);
                                        Subdivision subdivision = cityResponse.getMostSpecificSubdivision();
                                        value.setCountryISOCode(cityResponse.getCountry().getIsoCode());
                                        value.setSubdivisionISOCode(subdivision.getIsoCode());
                                        value.setCity(cityResponse.getCity().getName());
                                        value.setPostalCode(cityResponse.getPostal().getCode());
                                        Location location = cityResponse.getLocation();
                                        Integer dmaCode = location.getMetroCode();
                                        String dma = dmaCode == null ? null : dmaMapper.get(dmaCode);
                                        value.setDmaCode(dmaCode == null ? null : dmaCode);
                                        value.setDma(dma == null ? null : dma);
                                        if (hb.getLatitude() == 0.0 || hb.getLongitude() == 0.0) {
                                            hb.setLongitude(location.getLongitude());
                                            hb.setLatitude(location.getLatitude());
                                        }

                                        IspResponse ispResponse = geoIpISPReader.isp(ipAddress);
                                        value.setIsp(ispResponse.getIsp());
                                        value.setAsn(ispResponse.getAutonomousSystemNumber());
                                        value.setAso(ispResponse.getAutonomousSystemOrganization());
                                        LOG.debug("Maxmind ISP:"+ispResponse.getIsp()+" ASN:"+ispResponse.getAutonomousSystemNumber()+" ASO:"+ispResponse.getAutonomousSystemOrganization());
                                        LOG.debug("GeoLocation enrichment performed");
                                    }
                                }
                            } catch (Exception e) {
                                LOG.warn("Skipping GeoLocation enrichment due to error {}", e.getMessage());
                            }
                        }
                        value.setKdaAppVersion(videoPlayerStatsProperties.getVersion());
                        out.collect(value);
//                        } catch(Exception e){
//                            LOG.error("processElement error {}.\nCaused by: {}.\nStack trace: {}.\nDocument: {}", e.getMessage(), e.getCause(), e.getStackTrace(), value.toString());
//                        }
                    }
                    @Override
                    public void processBroadcastElement(ControlCommand controlCommand, Context ctx, Collector<SessionStatsAggr> out) throws Exception {
                        LOG.info("Control command received");
                        switch (controlCommand.getCode()) {
                            case 1:
                                LOG.info("Refresh cache on demand");
                                urlMapper = CacheFactory.getURLMapper(controlCommand.getArgument() == null ? videoPlayerStatsProperties.getMapFileLocation(): URI.create(controlCommand.getArgument()));
                                break;
                            case 2:
                                if (geoIpCityReader != null)
                                    geoIpCityReader.close();
                                geoIpCityReader = CacheFactory.loadGeoDB(controlCommand.getArgument() == null ? videoPlayerStatsProperties.getGeoIpCityDBLocation(): controlCommand.getArgument());
                                break;
                            case 3:
                                if (geoIpISPReader != null)
                                    geoIpISPReader.close();
                                geoIpISPReader = CacheFactory.loadGeoDB(controlCommand.getArgument() == null ? videoPlayerStatsProperties.getGeoIpISPDBLocation(): controlCommand.getArgument());
                            default:
                                break;
                        }
                    }

                    @Override
                    public void close() throws Exception {
                        LOG.debug("Clean GeoLocation cache");
                        if (geoIpCityReader != null)
                            geoIpCityReader.close();
                        if (geoIpISPReader != null)
                            geoIpISPReader.close();
                        super.close();
                    }
                }).uid("id02").name("geoLocationEnrichment")
                .returns(typeHint);

        return output;
    }


    private static MapFunction<SessionStatsAggr, Tuple4<String, String, String, String>> getBufferModelMapper() {
        return new MapFunction<SessionStatsAggr, Tuple4<String, String, String, String>>() {
            private static final long serialVersionUID = 4561647090360706L;

            @Override
            public Tuple4<String, String, String, String> map(SessionStatsAggr value) throws Exception {
                return Tuple4.of(
                        SessionStatsToESModelConverter.buildId(value),
                        gson.toJson(value),
                        SessionStatsToESModelConverter.buildSessionSuffix(value),StringUtils.EMPTY);
            }
        };
    }


}
