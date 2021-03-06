package com.vulinh.querybuilder.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denote that this field has a named alias associated with it (field as alias).
 *
 * @author Nguyen Vu Linh
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface As {

    String value();
}