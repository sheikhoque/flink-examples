package com.att.dtv.kda.factory;

import com.att.dtv.kda.model.app.ApplicationProperties;

import java.net.URISyntaxException;
import java.util.Properties;

public class KdsEsConfig extends ApplicationProperties {
    public KdsEsConfig(Properties appProperties) throws URISyntaxException {
        super(appProperties);
    }

}
