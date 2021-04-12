package com.amazonaws.kaja.samples.customfactory.avrowithschemaregistry;

import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;

public class RegistryAvroOptions {
    public static final ConfigOption<String> SCHEMA_REGISTRY_URL = ConfigOptions.key("schema-registry.url").stringType().noDefaultValue().withDescription("Required URL to connect to schema registry service");
    public static final ConfigOption<String> SCHEMA_REGISTRY_SUBJECT = ConfigOptions.key("schema-registry.subject").stringType().noDefaultValue().withDescription("Subject name to write to the Schema Registry service, required for sink");

    private RegistryAvroOptions() {
    }
}