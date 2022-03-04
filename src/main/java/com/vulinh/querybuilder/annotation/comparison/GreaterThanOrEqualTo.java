package com.vulinh.querybuilder.annotation.comparison;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denote that the field in question must be compared using GREATER THAN OR EQUAL TO operator.
 *
 * @author Nguyen Vu Linh
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GreaterThanOrEqualTo {

}