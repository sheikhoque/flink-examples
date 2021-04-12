package com.amazonaws.kaja.samples.customfactory.avrowithschemaregistry;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.formats.avro.AvroSerializationSchema;
import org.apache.flink.formats.avro.typeutils.AvroSchemaConverter;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.types.logical.RowType;

import java.util.Objects;

public class CustomAvroRowDataSerializationSchema implements SerializationSchema<RowData> {
    private static final long serialVersionUID = 1L;
    private final SerializationSchema<GenericRecord> nestedSchema;
    private final RowType rowType;
    private transient Schema schema;
    private final RowDataToAvroConverters.RowDataToAvroConverter runtimeConverter;

    public CustomAvroRowDataSerializationSchema(RowType rowType) {
        this(rowType, AvroSerializationSchema.forGeneric(AvroSchemaConverter.convertToSchema(rowType)), RowDataToAvroConverters.createConverter(rowType));
    }

    public CustomAvroRowDataSerializationSchema(RowType rowType, SerializationSchema<GenericRecord> nestedSchema, RowDataToAvroConverters.RowDataToAvroConverter runtimeConverter) {
        this.rowType = rowType;
        this.nestedSchema = nestedSchema;
        this.runtimeConverter = runtimeConverter;
    }

    public void open(InitializationContext context) throws Exception {
        this.schema = AvroSchemaConverter.convertToSchema(this.rowType);
        this.nestedSchema.open(context);
    }

    public byte[] serialize(RowData row) {
        try {
            GenericRecord record = (GenericRecord)this.runtimeConverter.convert(this.schema, row);
            return this.nestedSchema.serialize(record);
        } catch (Exception var3) {
            throw new RuntimeException("Failed to serialize row.", var3);
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            CustomAvroRowDataSerializationSchema that = (CustomAvroRowDataSerializationSchema)o;
            return this.nestedSchema.equals(that.nestedSchema) && this.rowType.equals(that.rowType);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.nestedSchema, this.rowType});
    }
}
