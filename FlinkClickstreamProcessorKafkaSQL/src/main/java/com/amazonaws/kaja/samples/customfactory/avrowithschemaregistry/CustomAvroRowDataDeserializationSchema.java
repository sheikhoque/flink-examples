package com.amazonaws.kaja.samples.customfactory.avrowithschemaregistry;

import org.apache.avro.generic.GenericRecord;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.formats.avro.AvroDeserializationSchema;
import org.apache.flink.formats.avro.typeutils.AvroSchemaConverter;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.types.logical.RowType;

import java.io.IOException;
import java.util.Objects;

public class CustomAvroRowDataDeserializationSchema implements DeserializationSchema<RowData> {
    private static final long serialVersionUID = 1L;
    private final DeserializationSchema<GenericRecord> nestedSchema;
    private final TypeInformation<RowData> typeInfo;
    private final AvroToRowDataConverters.AvroToRowDataConverter runtimeConverter;

    public CustomAvroRowDataDeserializationSchema(RowType rowType, TypeInformation<RowData> typeInfo) {
        this(AvroDeserializationSchema.forGeneric(AvroSchemaConverter.convertToSchema(rowType)), AvroToRowDataConverters.createRowConverter(rowType), typeInfo);
    }

    public CustomAvroRowDataDeserializationSchema(DeserializationSchema<GenericRecord> nestedSchema, AvroToRowDataConverters.AvroToRowDataConverter runtimeConverter, TypeInformation<RowData> typeInfo) {
        this.nestedSchema = nestedSchema;
        this.typeInfo = typeInfo;
        this.runtimeConverter = runtimeConverter;
    }

    public void open(InitializationContext context) throws Exception {
        this.nestedSchema.open(context);
    }

    public RowData deserialize(byte[] message) throws IOException {
        try {
            GenericRecord deserialize = (GenericRecord)this.nestedSchema.deserialize(message);
            return (RowData)this.runtimeConverter.convert(deserialize);
        } catch (Exception var3) {
            throw new IOException("Failed to deserialize Avro record.", var3);
        }
    }

    public boolean isEndOfStream(RowData nextElement) {
        return false;
    }

    public TypeInformation<RowData> getProducedType() {
        return this.typeInfo;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            CustomAvroRowDataDeserializationSchema that = (CustomAvroRowDataDeserializationSchema)o;
            return this.nestedSchema.equals(that.nestedSchema) && this.typeInfo.equals(that.typeInfo);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.nestedSchema, this.typeInfo});
    }
}

