package com.vulinh.util;

import static java.util.Objects.isNull;

import com.vulinh.annotation.comparison.*;
import com.vulinh.data.InvalidAnnotationCombinationException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Utility class for doing some inspection on the annotations used by this library.
 *
 * @author Nguyen Vu Linh
 */
public final class AnnotationUtils {

    private AnnotationUtils() {
        throw new UnsupportedOperationException("Cannot instantiate utility class!");
    }

    /**
     * Check if a field is annotated by various annotations that form 'invalid' combination. For example, <code>@IsNotNull</code> cannot be paired with
     * <code>@IsNull</code>, because it will not make sense to have a field that has both 'is not null' and 'is null' at the same time. If such combinations
     * are found, a <code>InvalidAnnotationCombinationException</code> will be thrown.
     *
     * @param field The field to check.
     * @throws InvalidAnnotationCombinationException when an invalid combination of annotation is found.
     */
    public static void checkInvalidAnnotationCombination(Field field) {
        Annotation[] annotations = field.getAnnotations();

        for (int i = 0; i < annotations.length; i++) {
            Class<? extends Annotation> type = annotations[i].annotationType();

            for (int j = i + 1; j < annotations.length; j++) {
                Class<? extends Annotation> innerType = annotations[j].annotationType();

                if (INVALID_COMBINATIONS.contains(new Combination(type, innerType))) {
                    throw new InvalidAnnotationCombinationException(
                        field, type, innerType
                    );
                }
            }
        }
    }

    private static final Set<Combination> INVALID_COMBINATIONS;

    static {
        INVALID_COMBINATIONS = new HashSet<>();

        Class<?>[] comparisonAnnotations = new Class<?>[]{
            Between.class,
            GreaterThan.class,
            GreaterThanOrEqualTo.class,
            LessThan.class,
            LessThanOrEqualTo.class,
            IsNull.class,
            IsNotNull.class,
            NotEqual.class,
            Like.class,
            InRange.class,
            OutRange.class
        };

        for (Class<?> clazz : comparisonAnnotations) {
            for (Class<?> innerClazz : comparisonAnnotations) {
                // Same annotation cannot be used multiple times on a single field, as such, it doesn't matter
                if (!clazz.equals(innerClazz)) {
                    addSet(clazz, innerClazz);
                }
            }
        }
    }

    private static void addSet(Class<?> annotation1, Class<?> annotation2) {
        INVALID_COMBINATIONS.add(new Combination(annotation1, annotation2));
    }

    /**
     * Inner class Combination, consists of a pair of 'comparison' annotations. As private inner class, nothing outside this class can access it.
     */
    private static class Combination {

        private final Class<?> annotation1;
        private final Class<?> annotation2;

        Combination(Class<?> annotation1, Class<?> annotation2) {
            this.annotation1 = annotation1;
            this.annotation2 = annotation2;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (isNull(other) || getClass() != other.getClass()) {
                return false;
            }

            Combination that = (Combination) other;
            return Objects.equals(annotation1, that.annotation1) && Objects.equals(annotation2, that.annotation2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(annotation1, annotation2);
        }

        @Override
        public String toString() {
            return "Combination{" +
                "annotation1=" + annotation1 +
                ", annotation2=" + annotation2 +
                '}';
        }
    }
}