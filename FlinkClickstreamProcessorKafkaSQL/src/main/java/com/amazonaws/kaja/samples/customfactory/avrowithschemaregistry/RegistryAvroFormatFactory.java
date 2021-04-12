package com.amazonaws.kaja.samples.customfactory.avrowithschemaregistry;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.formats.avro.registry.confluent.ConfluentRegistryAvroDeserializationSchema;
import org.apache.flink.formats.avro.registry.confluent.ConfluentRegistryAvroSerializationSchema;
import org.apache.flink.formats.avro.typeutils.AvroSchemaConverter;
import org.apache.flink.table.api.ValidationException;
import org.apache.flink.table.connector.ChangelogMode;
import org.apache.flink.table.connector.format.DecodingFormat;
import org.apache.flink.table.connector.format.EncodingFormat;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.factories.DeserializationFormatFactory;
import org.apache.flink.table.factories.DynamicTableFactory;
import org.apache.flink.table.factories.FactoryUtil;
import org.apache.flink.table.factories.SerializationFormatFactory;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.logical.RowType;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class RegistryAvroFormatFactory implements DeserializationFormatFactory, SerializationFormatFactory {
    public static final String IDENTIFIER = "avro-confluent";

    public RegistryAvroFormatFactory() {
    }

    public DecodingFormat<DeserializationSchema<RowData>> createDecodingFormat(DynamicTableFactory.Context context, ReadableConfig formatOptions) {
        FactoryUtil.validateFactoryOptions(this, formatOptions);
        final String schemaRegistryURL = (String)formatOptions.get(RegistryAvroOptions.SCHEMA_REGISTRY_URL);
        return new DecodingFormat<DeserializationSchema<RowData>>() {
            public DeserializationSchema<RowData> createRuntimeDecoder(org.apache.flink.table.connector.source.DynamicTableSource.Context context, DataType producedDataType) {
                RowType rowType = (RowType)producedDataType.getLogicalType();
                TypeInformation<RowData> rowDataTypeInfo = (TypeInformation<RowData>) context.createTypeInformation(producedDataType);
                return new CustomAvroRowDataDeserializationSchema(ConfluentRegistryAvroDeserializationSchema.forGeneric(AvroSchemaConverter.convertToSchema(rowType), schemaRegistryURL), AvroToRowDataConverters.createRowConverter(rowType), rowDataTypeInfo);
            }

            public ChangelogMode getChangelogMode() {
                return ChangelogMode.insertOnly();
            }
        };
    }

    public EncodingFormat<SerializationSchema<RowData>> createEncodingFormat(DynamicTableFactory.Context context, ReadableConfig formatOptions) {
        FactoryUtil.validateFactoryOptions(this, formatOptions);
        final String schemaRegistryURL = (String)formatOptions.get(RegistryAvroOptions.SCHEMA_REGISTRY_URL);
        final Optional<String> subject = formatOptions.getOptional(RegistryAvroOptions.SCHEMA_REGISTRY_SUBJECT);
        if (!subject.isPresent()) {
            throw new ValidationException(String.format("Option %s.%s is required for serialization", "avro-confluent", RegistryAvroOptions.SCHEMA_REGISTRY_SUBJECT.key()));
        } else {
            return new EncodingFormat<SerializationSchema<RowData>>() {
                public SerializationSchema<RowData> createRuntimeEncoder(org.apache.flink.table.connector.sink.DynamicTableSink.Context context, DataType consumedDataType) {
                    RowType rowType = (RowType)consumedDataType.getLogicalType();
                    return new CustomAvroRowDataSerializationSchema(rowType, ConfluentRegistryAvroSerializationSchema.forGeneric((String)subject.get(), AvroSchemaConverter.convertToSchema(rowType), schemaRegistryURL), RowDataToAvroConverters.createConverter(rowType));
                }

                public ChangelogMode getChangelogMode() {
                    return ChangelogMode.insertOnly();
                }
            };
        }
    }

    public String factoryIdentifier() {
        return IDENTIFIER;
    }

    public Set<ConfigOption<?>> requiredOptions() {
        Set<ConfigOption<?>> options = new HashSet();
        options.add(RegistryAvroOptions.SCHEMA_REGISTRY_URL);
        return options;
    }

    public Set<ConfigOption<?>> optionalOptions() {
        Set<ConfigOption<?>> options = new HashSet();
        options.add(RegistryAvroOptions.SCHEMA_REGISTRY_SUBJECT);
        return options;
    }
}