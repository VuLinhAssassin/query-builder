package com.vulinh.annotation;

import com.vulinh.util.StringUtils;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denote that this field must be wrapped by either an SQL or an HQL method, for example: date(), cast().
 *
 * @author Nguyen Vu Linh
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UseWrapMethod {

    /**
     * Name for method that wraps the field.
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