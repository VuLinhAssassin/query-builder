package com.vulinh.querybuilder.annotation.comparison;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.vulinh.querybuilder.support.StringUtils.FROM_VALUE;
import static com.vulinh.querybuilder.support.StringUtils.TO_VALUE;

/**
 * Denote that the field in question must be compared using BETWEEN operator.
 *
 * @author Nguyen Vu Linh
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Between {

    /**
     * From inclusive field parameter.
     *
     * @return From-inclusive field parameter.
     */
    String fromInclusive() default FROM_VALUE;

    /**
     * To inclusive field parameter.
     *
     * @return To-inclusive field parameter.
     */
    String toInclusive() default TO_VALUE;
}