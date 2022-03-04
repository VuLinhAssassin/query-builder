package com.vulinh.querybuilder.support;

import java.lang.reflect.Field;

/**
 * Exception when an invalid combination of two comparison annotation is found.
 *
 * @author Nguyen Vu Linh
 */
public class InvalidAnnotationCombinationException extends BuilderException {

    private static final long serialVersionUID = -8463838637366319754L;

    public InvalidAnnotationCombinationException(Field field, Object annotation1, Object annotation2) {
        super(String.format("Field [%s] contained invalid annotation combination: [%s] and [%s]", field, annotation1, annotation2));
    }
}