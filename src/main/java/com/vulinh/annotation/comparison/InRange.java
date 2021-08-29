package com.vulinh.annotation.comparison;

import static com.vulinh.util.StringUtils.FROM_VALUE;
import static com.vulinh.util.StringUtils.TO_VALUE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denote that the field in question must be checked whether it is within a certain range, inclusively or not.
 *
 * @author Nguyen Vu Linh
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InRange {

    /**
     * From range field parameter.
     *
     * @return From range field parameter.
     */
    String fromField() default FROM_VALUE;

    /**
     * To range field parameter.
     *
     * @return To range field parameter.
     */
    String toField() default TO_VALUE;

    /**
     * Determine whether the 'border' value is included (OR EQUAL TO operator).
     *
     * @return <code>true</code> if 'border' value is included in comparison; <code>false if otherwise</code>.
     */
    boolean inclusivity() default false;
}