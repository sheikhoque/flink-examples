package com.att.dtv.kda.converters;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;

public class GeoIPTest {
    @Test
    void test() {
        try {
            DatabaseReader reader = new DatabaseReader.Builder(this.getClass().getResourceAsStream("/GeoLite2-City.mmdb")).withCache(new CHMCache()).build();
            InetAddress ipAddress = InetAddress.getByName("72.208.44.48");
            CityResponse response = reader.city(ipAddress);

            Country country = response.getCountry();
            System.out.println(country.getIsoCode());            // 'US'
            System.out.println(country.getName());               // 'United States'

            Subdivision subdivision = response.getMostSpecificSubdivision();
            System.out.println(subdivision.getName());    // 'Minnesota'
            System.out.println(subdivision.getIsoCode()); // 'MN'

            City city = response.getCity();
            System.out.println(city.getName()); // 'Minneapolis'

            Postal postal = response.getPostal();
            System.out.println(postal.getCode()); // '55455'

            Location location = response.getLocation();
            System.out.println(location.getLatitude());  // 44.9733
            System.out.println(location.getLongitude()); // -93.2323

        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeoIp2Exception e) {
            e.printStackTrace();
        }

    }
}
