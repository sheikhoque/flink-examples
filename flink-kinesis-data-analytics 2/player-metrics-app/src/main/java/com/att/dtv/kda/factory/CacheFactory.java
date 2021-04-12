package com.att.dtv.kda.factory;

import com.att.dtv.kda.model.app.VideoPlayerStatsProperties;
import com.att.dtv.kda.process.URLMapper;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import org.apache.commons.csv.CSVFormat;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class CacheFactory implements Serializable {
    private static long serialVersionUID = 4423425297650414542L;
    private static Logger LOG = LoggerFactory.getLogger(CacheFactory.class);
    private static URLMapper urlMapper;

    public static URLMapper getURLMapper(VideoPlayerStatsProperties props) throws Exception {
        return getURLMapper(props.getMapFileLocation());
    }

    public static Map<Integer,String> getDMAMappings(VideoPlayerStatsProperties props) throws Exception {
        return getDMAMappings(props.getDmaFileLocation());
    }

    public static URLMapper getURLMapper(URI location) throws Exception {
        SupportedProtocol supportedProtocol = new SupportedProtocol(location);
        URI uri = URI.create(supportedProtocol.getEndpoint());
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(FileSystem.get(uri).open(new Path(uri))))) {
            URLMapper.Builder mapBuilder = new URLMapper.Builder();
            CSVFormat.EXCEL.withFirstRecordAsHeader().withQuote('"').parse(bufferedReader).forEach(r ->
                    mapBuilder.addCdnPatternPair(r.get(0), r.get(1)));
            LOG.info("CDN mapping initialized successfully.");

            urlMapper = mapBuilder.build();
            return urlMapper;
        } catch (Exception e){
            LOG.error("Error in reading the file: {}", e.getMessage());
            throw e;
        }
    }

    public static Map<Integer,String> getDMAMappings(URI location) throws Exception {
        SupportedProtocol supportedProtocol = new SupportedProtocol(location);
        URI uri = URI.create(supportedProtocol.getEndpoint());
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(FileSystem.get(uri).open(new Path(uri))))) {
            Map<Integer,String> dmaMappings = new HashMap<>();
            CSVFormat.EXCEL.withFirstRecordAsHeader().withQuote('"').parse(bufferedReader).forEach(r ->
                    dmaMappings.put(Integer.parseInt(r.get(0).trim()), r.get(1)));
            return dmaMappings;
        } catch (Exception e){
            LOG.error("Error in reading the file: {}", e.getMessage());
            throw e;
        }
    }

    public static DatabaseReader getGeoIpCityReader(VideoPlayerStatsProperties props) throws Exception {
        return loadGeoDB(props.getGeoIpCityDBLocation());
    }

    public static DatabaseReader getGeoIpISPReader(VideoPlayerStatsProperties props) throws Exception {
        return loadGeoDB(props.getGeoIpISPDBLocation());
    }

    public static DatabaseReader loadGeoDB(String location) throws Exception {
        SupportedProtocol supportedProtocol = new SupportedProtocol(new URI(location));
        URI uri = URI.create(supportedProtocol.getEndpoint());
        try {
            FileSystem fs = FileSystem.get(uri);
            Path path = new Path(uri);
            InputStream is = fs.open(path);
            DatabaseReader databaseReader = new DatabaseReader.Builder(is).withCache(new CHMCache()).build();
            LOG.info("GeoLocation database {} loaded successfully.", location);
            return databaseReader;
        } catch (Exception e) {
            LOG.error("Error in reading the file: {}", e.getMessage());
            throw e;
        }
    }

}
