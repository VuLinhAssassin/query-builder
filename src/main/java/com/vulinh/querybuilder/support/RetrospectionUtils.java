package com.vulinh.querybuilder.support;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Utility class for doing some retrospection by this library.
 *
 * @author Nguyen Vu Linh
 */
class RetrospectionUtils {

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
        return Objects.nonNull(getFieldValue(field, object));
    }

    /**
     * Try invoking reader method for provided <code>field</code> in given object to get its value.
     *
     * @param field  The field to invoke reader to get its value.
     * @param object The object that contains said field.
     * @param <T>    Object type.
     * @return Possible field value within given object.
     */
    public static <T> Object getFieldValue(Field field, T object) {
        try {
            PropertyDescriptor descriptor = new PropertyDescriptor(field.getName(), object.getClass());
            return descriptor.getReadMethod().invoke(object);
        } catch (Exception ex) {
            throw new BuilderException(
                String.format("Failed to invoke reader method from field %s in class %s", field, object.getClass()), ex
            );
        }
    }
}
