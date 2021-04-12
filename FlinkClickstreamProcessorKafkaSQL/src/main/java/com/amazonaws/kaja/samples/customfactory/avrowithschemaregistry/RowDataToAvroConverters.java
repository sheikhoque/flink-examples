package com.amazonaws.kaja.samples.customfactory.avrowithschemaregistry;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.apache.flink.formats.avro.typeutils.AvroSchemaConverter;
import org.apache.flink.table.data.*;
import org.apache.flink.table.types.logical.ArrayType;
import org.apache.flink.table.types.logical.LogicalType;
import org.apache.flink.table.types.logical.RowType;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RowDataToAvroConverters {
    public RowDataToAvroConverters() {
    }

    public static RowDataToAvroConverters.RowDataToAvroConverter createConverter(LogicalType type) {
        final RowDataToAvroConverters.RowDataToAvroConverter converter;
        switch(type.getTypeRoot()) {
            case NULL:
                converter = new RowDataToAvroConverters.RowDataToAvroConverter() {
                    private static final long serialVersionUID = 1L;

                    public Object convert(Schema schema, Object object) {
                        return null;
                    }
                };
                break;
            case TINYINT:
                converter = new RowDataToAvroConverters.RowDataToAvroConverter() {
                    private static final long serialVersionUID = 1L;

                    public Object convert(Schema schema, Object object) {
                        return ((Byte)object).intValue();
                    }
                };
                break;
            case SMALLINT:
                converter = new RowDataToAvroConverters.RowDataToAvroConverter() {
                    private static final long serialVersionUID = 1L;

                    public Object convert(Schema schema, Object object) {
                        return ((Short)object).intValue();
                    }
                };
                break;
            case BOOLEAN:
            case INTEGER:
            case INTERVAL_YEAR_MONTH:
            case BIGINT:
            case INTERVAL_DAY_TIME:
            case FLOAT:
            case DOUBLE:
            case TIME_WITHOUT_TIME_ZONE:
            case DATE:
                converter = new RowDataToAvroConverters.RowDataToAvroConverter() {
                    private static final long serialVersionUID = 1L;

                    public Object convert(Schema schema, Object object) {
                        return object;
                    }
                };
                break;
            case CHAR:
            case VARCHAR:
                converter = new RowDataToAvroConverters.RowDataToAvroConverter() {
                    private static final long serialVersionUID = 1L;

                    public Object convert(Schema schema, Object object) {
                        return new Utf8(object.toString());
                    }
                };
                break;
            case BINARY:
            case VARBINARY:
                converter = new RowDataToAvroConverters.RowDataToAvroConverter() {
                    private static final long serialVersionUID = 1L;

                    public Object convert(Schema schema, Object object) {
                        return ByteBuffer.wrap((byte[])((byte[])object));
                    }
                };
                break;
            case TIMESTAMP_WITHOUT_TIME_ZONE:
                converter = new RowDataToAvroConverters.RowDataToAvroConverter() {
                    private static final long serialVersionUID = 1L;

                    public Object convert(Schema schema, Object object) {
                        return ((TimestampData)object).toInstant().toEpochMilli();
                    }
                };
                break;
            case DECIMAL:
                converter = new RowDataToAvroConverters.RowDataToAvroConverter() {
                    private static final long serialVersionUID = 1L;

                    public Object convert(Schema schema, Object object) {
                        return ByteBuffer.wrap(((DecimalData)object).toUnscaledBytes());
                    }
                };
                break;
            case ARRAY:
                converter = createArrayConverter((ArrayType)type);
                break;
            case ROW:
                converter = createRowConverter((RowType)type);
                break;
            case MAP:
            case MULTISET:
                converter = createMapConverter(type);
                break;
            case RAW:
            default:
                throw new UnsupportedOperationException("Unsupported type: " + type);
        }

        return new RowDataToAvroConverters.RowDataToAvroConverter() {
            private static final long serialVersionUID = 1L;

            public Object convert(Schema schema, Object object) {
                if (object == null) {
                    return null;
                } else {
                    Schema actualSchema;
                    if (schema.getType() == Schema.Type.UNION) {
                        List<Schema> types = schema.getTypes();
                        int size = types.size();
                        if (size == 2 && ((Schema)types.get(1)).getType() == Schema.Type.NULL) {
                            actualSchema = (Schema)types.get(0);
                        } else {
                            if (size != 2 || ((Schema)types.get(0)).getType() != Schema.Type.NULL) {
                                throw new IllegalArgumentException("The Avro schema is not a nullable type: " + schema.toString());
                            }

                            actualSchema = (Schema)types.get(1);
                        }
                    } else {
                        actualSchema = schema;
                    }

                    return converter.convert(actualSchema, object);
                }
            }
        };
    }

    private static RowDataToAvroConverters.RowDataToAvroConverter createRowConverter(RowType rowType) {
        final RowDataToAvroConverters.RowDataToAvroConverter[] fieldConverters = (RowDataToAvroConverters.RowDataToAvroConverter[])rowType.getChildren().stream().map(RowDataToAvroConverters::createConverter).toArray((x$0) -> {
            return new RowDataToAvroConverters.RowDataToAvroConverter[x$0];
        });
        LogicalType[] fieldTypes = (LogicalType[])rowType.getFields().stream().map(RowType.RowField::getType).toArray((x$0) -> {
            return new LogicalType[x$0];
        });
        final RowData.FieldGetter[] fieldGetters = new RowData.FieldGetter[fieldTypes.length];

        final int length;
        int length1;
        for(length1 = 0; length1 < fieldTypes.length; ++length1) {
            fieldGetters[length1] = RowData.createFieldGetter(fieldTypes[length1], length1);
        }

        length1 = rowType.getFieldCount();
        length = length1;
        return new RowDataToAvroConverters.RowDataToAvroConverter() {
            private static final long serialVersionUID = 1L;

            public Object convert(Schema schema, Object object) {
                RowData row = (RowData)object;
                List<Schema.Field> fields = schema.getFields();
                GenericRecord record = new GenericData.Record(schema);

                for(int i = 0; i < length; ++i) {
                    Schema.Field schemaField = (Schema.Field)fields.get(i);
                    Object avroObject = fieldConverters[i].convert(schemaField.schema(), fieldGetters[i].getFieldOrNull(row));
                    record.put(i, avroObject);
                }

                return record;
            }
        };
    }

    private static RowDataToAvroConverters.RowDataToAvroConverter createArrayConverter(ArrayType arrayType) {
        LogicalType elementType = arrayType.getElementType();
        final ArrayData.ElementGetter elementGetter = ArrayData.createElementGetter(elementType);
        final RowDataToAvroConverters.RowDataToAvroConverter elementConverter = createConverter(arrayType.getElementType());
        return new RowDataToAvroConverters.RowDataToAvroConverter() {
            private static final long serialVersionUID = 1L;

            public Object convert(Schema schema, Object object) {
                Schema elementSchema = schema.getElementType();
                ArrayData arrayData = (ArrayData)object;
                List<Object> list = new ArrayList();

                for(int i = 0; i < arrayData.size(); ++i) {
                    list.add(elementConverter.convert(elementSchema, elementGetter.getElementOrNull(arrayData, i)));
                }

                return list;
            }
        };
    }

    private static RowDataToAvroConverters.RowDataToAvroConverter createMapConverter(LogicalType type) {
        LogicalType valueType = AvroSchemaConverter.extractValueTypeToAvroMap(type);
        final ArrayData.ElementGetter valueGetter = ArrayData.createElementGetter(valueType);
        final RowDataToAvroConverters.RowDataToAvroConverter valueConverter = createConverter(valueType);
        return new RowDataToAvroConverters.RowDataToAvroConverter() {
            private static final long serialVersionUID = 1L;

            public Object convert(Schema schema, Object object) {
                Schema valueSchema = schema.getValueType();
                MapData mapData = (MapData)object;
                ArrayData keyArray = mapData.keyArray();
                ArrayData valueArray = mapData.valueArray();
                Map<Object, Object> map = new HashMap(mapData.size());

                for(int i = 0; i < mapData.size(); ++i) {
                    String key = keyArray.getString(i).toString();
                    Object value = valueConverter.convert(valueSchema, valueGetter.getElementOrNull(valueArray, i));
                    map.put(key, value);
                }

                return map;
            }
        };
    }

    @FunctionalInterface
    public interface RowDataToAvroConverter extends Serializable {
        Object convert(Schema var1, Object var2);
    }
}

