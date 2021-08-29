package com.vulinh.util;

import static java.lang.String.format;
import static java.util.Objects.nonNull;

import com.vulinh.data.BuilderException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 * Utility class for doing some retrospection by this library.
 *
 * @author Nguyen Vu Linh
 */
public final class RetrospectionUtils {

    private RetrospectionUtils() {
        throw new UnsupportedOperationException("Cannot instantiate utility class!");
    }

    /**
     * Invoke field's getter method to see if field contains value. Will throw exception if the provided object is not a valid Java object, or getter method is
     * missing.
     *
     * @param field  The field to test.
     * @param object The object that contains said field.
     * @param <T>    Object type.
     * @return <code>true</code> if said field has non-null value; <code>false</code> if otherwise.
     * @throws BuilderException when the provided object cannot be retrospected to invoke getter method.
     */
    public static <T> boolean isValuePresent(Field field, T object) {
        try {
            PropertyDescriptor descriptor = new PropertyDescriptor(field.getName(), object.getClass());
            return nonNull(descriptor.getReadMethod().invoke(object));
        } catch (Exception ex) {
            throw new BuilderException(
                format("Either class %s is not a valid bean or getter method not present for field %s", object.getClass(), field), ex
            );
        }
    }
}