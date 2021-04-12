package com.amazonaws.kaja.samples.customfactory.avrowithschemaregistry;

import org.apache.avro.generic.GenericFixed;
import org.apache.avro.generic.IndexedRecord;
import org.apache.flink.formats.avro.typeutils.AvroSchemaConverter;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.data.*;
import org.apache.flink.table.types.logical.ArrayType;
import org.apache.flink.table.types.logical.DecimalType;
import org.apache.flink.table.types.logical.LogicalType;
import org.apache.flink.table.types.logical.RowType;
import org.apache.flink.table.types.logical.utils.LogicalTypeUtils;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AvroToRowDataConverters {
    public AvroToRowDataConverters() {
    }

    public static AvroToRowDataConverters.AvroToRowDataConverter createRowConverter(RowType rowType) {
        AvroToRowDataConverters.AvroToRowDataConverter[] fieldConverters = (AvroToRowDataConverters.AvroToRowDataConverter[])rowType.getFields().stream().map(RowType.RowField::getType).map(AvroToRowDataConverters::createNullableConverter).toArray((x$0) -> {
            return new AvroToRowDataConverters.AvroToRowDataConverter[x$0];
        });
        int arity = rowType.getFieldCount();
        return (avroObject) -> {
            IndexedRecord record = (IndexedRecord)avroObject;
            GenericRowData row = new GenericRowData(arity);

            for(int i = 0; i < arity; ++i) {
                row.setField(i, fieldConverters[i].convert(record.get(i)));
            }

            return row;
        };
    }

    private static AvroToRowDataConverters.AvroToRowDataConverter createNullableConverter(LogicalType type) {
        AvroToRowDataConverters.AvroToRowDataConverter converter = createConverter(type);
        return (avroObject) -> {
            return avroObject == null ? null : converter.convert(avroObject);
        };
    }

    private static AvroToRowDataConverters.AvroToRowDataConverter createConverter(LogicalType type) {
        switch(type.getTypeRoot()) {
            case NULL:
                return (avroObject) -> {
                    return null;
                };
            case TINYINT:
                return (avroObject) -> {
                    return ((Integer)avroObject).byteValue();
                };
            case SMALLINT:
                return (avroObject) -> {
                    return ((Integer)avroObject).shortValue();
                };
            case BOOLEAN:
            case INTEGER:
            case INTERVAL_YEAR_MONTH:
            case BIGINT:
            case INTERVAL_DAY_TIME:
            case FLOAT:
            case DOUBLE:
                return (avroObject) -> {
                    return avroObject;
                };
            case DATE:
                return AvroToRowDataConverters::convertToDate;
            case TIME_WITHOUT_TIME_ZONE:
                return AvroToRowDataConverters::convertToTime;
            case TIMESTAMP_WITHOUT_TIME_ZONE:
                return AvroToRowDataConverters::convertToTimestamp;
            case CHAR:
            case VARCHAR:
                return (avroObject) -> {
                    return StringData.fromString(avroObject.toString());
                };
            case BINARY:
            case VARBINARY:
                return AvroToRowDataConverters::convertToBytes;
            case DECIMAL:
                return createDecimalConverter((DecimalType)type);
            case ARRAY:
                return createArrayConverter((ArrayType)type);
            case ROW:
                return createRowConverter((RowType)type);
            case MAP:
            case MULTISET:
                return createMapConverter(type);
            case RAW:
            default:
                throw new UnsupportedOperationException("Unsupported type: " + type);
        }
    }

    private static AvroToRowDataConverters.AvroToRowDataConverter createDecimalConverter(DecimalType decimalType) {
        int precision = decimalType.getPrecision();
        int scale = decimalType.getScale();
        return (avroObject) -> {
            byte[] bytes;
            if (avroObject instanceof GenericFixed) {
                bytes = ((GenericFixed)avroObject).bytes();
            } else if (avroObject instanceof ByteBuffer) {
                ByteBuffer byteBuffer = (ByteBuffer)avroObject;
                bytes = new byte[byteBuffer.remaining()];
                byteBuffer.get(bytes);
            } else {
                bytes = (byte[])((byte[])avroObject);
            }

            return DecimalData.fromUnscaledBytes(bytes, precision, scale);
        };
    }

    private static AvroToRowDataConverters.AvroToRowDataConverter createArrayConverter(ArrayType arrayType) {
        AvroToRowDataConverters.AvroToRowDataConverter elementConverter = createNullableConverter(arrayType.getElementType());
        Class<?> elementClass = LogicalTypeUtils.toInternalConversionClass(arrayType.getElementType());
        return (avroObject) -> {
            List<?> list = (List)avroObject;
            int length = list.size();
            Object[] array = (Object[])((Object[]) Array.newInstance(elementClass, length));

            for(int i = 0; i < length; ++i) {
                array[i] = elementConverter.convert(list.get(i));
            }

            return new GenericArrayData(array);
        };
    }

    private static AvroToRowDataConverters.AvroToRowDataConverter createMapConverter(LogicalType type) {
        AvroToRowDataConverters.AvroToRowDataConverter keyConverter = createConverter(DataTypes.STRING().getLogicalType());
        AvroToRowDataConverters.AvroToRowDataConverter valueConverter = createNullableConverter(AvroSchemaConverter.extractValueTypeToAvroMap(type));
        return (avroObject) -> {
            Map<?, ?> map = (Map)avroObject;
            Map<Object, Object> result = new HashMap();
            Iterator var5 = map.entrySet().iterator();

            while(var5.hasNext()) {
                Map.Entry<?, ?> entry = (Map.Entry)var5.next();
                Object key = keyConverter.convert(entry.getKey());
                Object value = valueConverter.convert(entry.getValue());
                result.put(key, value);
            }

            return new GenericMapData(result);
        };
    }

    private static TimestampData convertToTimestamp(Object object) {
        long millis;
        if (object instanceof Long) {
            millis = (Long)object;
        } else if (object instanceof Instant) {
            millis = ((Instant)object).toEpochMilli();
        } else {
            JodaConverter jodaConverter = JodaConverter.getConverter();
            if (jodaConverter == null) {
                throw new IllegalArgumentException("Unexpected object type for TIMESTAMP logical type. Received: " + object);
            }

            millis = jodaConverter.convertTimestamp(object);
        }

        return TimestampData.fromEpochMillis(millis);
    }

    private static int convertToDate(Object object) {
        if (object instanceof Integer) {
            return (Integer)object;
        } else if (object instanceof LocalDate) {
            return (int)((LocalDate)object).toEpochDay();
        } else {
            JodaConverter jodaConverter = JodaConverter.getConverter();
            if (jodaConverter != null) {
                return (int)jodaConverter.convertDate(object);
            } else {
                throw new IllegalArgumentException("Unexpected object type for DATE logical type. Received: " + object);
            }
        }
    }

    private static int convertToTime(Object object) {
        int millis;
        if (object instanceof Integer) {
            millis = (Integer)object;
        } else if (object instanceof LocalTime) {
            millis = ((LocalTime)object).get(ChronoField.MILLI_OF_DAY);
        } else {
            JodaConverter jodaConverter = JodaConverter.getConverter();
            if (jodaConverter == null) {
                throw new IllegalArgumentException("Unexpected object type for TIME logical type. Received: " + object);
            }

            millis = jodaConverter.convertTime(object);
        }

        return millis;
    }

    private static byte[] convertToBytes(Object object) {
        if (object instanceof GenericFixed) {
            return ((GenericFixed)object).bytes();
        } else if (object instanceof ByteBuffer) {
            ByteBuffer byteBuffer = (ByteBuffer)object;
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
            return bytes;
        } else {
            return (byte[])((byte[])object);
        }
    }

    @FunctionalInterface
    public interface AvroToRowDataConverter extends Serializable {
        Object convert(Object var1);
    }
}
