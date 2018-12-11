package org.messtin.lock.common.util;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProtostuffUtils {
    private static volatile LinkedBuffer buffer =
            LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
    private static Map<Class<?>, Schema<?>> schemaCache = new ConcurrentHashMap<>();

    private ProtostuffUtils() {
    }

    public static <T> byte[] serialize(T object) {
        Class<T> clazz = (Class<T>) object.getClass();
        Schema<T> schema = getSchema(clazz);
        byte[] data;
        try {
            data = ProtostuffIOUtil.toByteArray(object, schema, buffer);
        } finally {
            buffer.clear();
        }
        return data;
    }

    public static <T> T deserialize(byte[] data, Class<T> clazz) {
        Schema<T> schema = getSchema(clazz);
        T obj = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(data, obj, schema);
        return obj;
    }

    public static <T> Schema<T> getSchema(Class<T> clazz) {
        if (schemaCache.containsKey(clazz)) {
            return (Schema<T>) schemaCache.get(clazz);
        }
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        schemaCache.put(clazz, schema);
        return schema;
    }
}
