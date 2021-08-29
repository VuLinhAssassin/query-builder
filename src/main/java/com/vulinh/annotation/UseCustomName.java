package com.vulinh.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denote that this field should use provided custom name.
 *
 * @author Nguyen Vu Linh
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UseCustomName {

    /**
     * The column name to use in place of field name.
     *
     * @return The column name to use in place of field name.
     */
    String value();
}