package com.vulinh.querybuilder.annotation;

import com.vulinh.querybuilder.support.StringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denote that this field's 'value' part (the part after colon) must be wrapped by either an SQL or an HQL method, for example: date(), cast().
 *
 * @author Nguyen Vu Linh
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ValueWrapMethod {

    /**
     * Name for method that wraps the value part.
     *
     * @return Name for method that wraps the field.
     */
    String value();

    /**
     * Additional info for wrap method, for example: <code>cast(field as java.lang.String)</code>.
     *
     * @return Additional info for wrap method. By default, this field is empty.
     */
    String after() default StringUtils.EMPTY;
}
