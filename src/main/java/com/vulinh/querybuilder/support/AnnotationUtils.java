package com.vulinh.querybuilder.support;

import com.vulinh.querybuilder.annotation.As;
import com.vulinh.querybuilder.annotation.AsItself;
import com.vulinh.querybuilder.annotation.IgnoreField;
import com.vulinh.querybuilder.annotation.comparison.Between;
import com.vulinh.querybuilder.annotation.comparison.GreaterThan;
import com.vulinh.querybuilder.annotation.comparison.GreaterThanOrEqualTo;
import com.vulinh.querybuilder.annotation.comparison.InRange;
import com.vulinh.querybuilder.annotation.comparison.IsNotNull;
import com.vulinh.querybuilder.annotation.comparison.IsNull;
import com.vulinh.querybuilder.annotation.comparison.LessThan;
import com.vulinh.querybuilder.annotation.comparison.LessThanOrEqualTo;
import com.vulinh.querybuilder.annotation.comparison.Like;
import com.vulinh.querybuilder.annotation.comparison.NotEqual;
import com.vulinh.querybuilder.annotation.comparison.OutRange;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Utility class for doing some inspection on the annotations used by this library.
 *
 * @author Nguyen Vu Linh
 */
public class AnnotationUtils {

    private AnnotationUtils() {
        throw new UnsupportedOperationException("Cannot instantiate utility class!");
    }

    /**
     * Get non-ignorable fields from provided class, namely fields that are neither static nor annotated with <code>@IgnoreField</code>.
     *
     * @param clazz The provided class.
     * @param <T>   The class type.
     * @return A list of fields that are not static AND not marked with <code>@IgnoreField</code>
     */
    public static <T> List<Field> getNonIgnorableFields(Class<T> clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();

        List<Field> acceptedFields = new ArrayList<>();

        for (Field field : declaredFields) {
            if (!isIgnorableField(field)) {
                acceptedFields.add(field);
            }
        }

        return acceptedFields;
    }

    /**
     * Get non-ignorable fields from provided object, namely fields that are neither static nor annotated with <code>@IgnoreField</code>. In addition, the field
     * must also have its value (non-null).
     *
     * @param object The provided object.
     * @param <T>    The class type.
     * @return A list of fields that are not static AND not marked with <code>@IgnoreField</code>, in addition to having value (non-null).
     */
    public static <T> List<Field> getNonIgnorableAndNonNullFields(T object) {
        if (Objects.isNull(object)) {
            throw new BuilderException("Use null object is not allowed!");
        }

        Field[] declaredFields = object.getClass().getDeclaredFields();

        List<Field> acceptedFields = new ArrayList<>();

        for (Field field : declaredFields) {
            if (!isIgnorableOrNullField(field, object)) {
                acceptedFields.add(field);
            }
        }

        return acceptedFields;
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

        if (annotations.length <= 1) {
            return;
        }

        for (int i = 0; i < annotations.length; i++) {
            Class<? extends Annotation> type = annotations[i].annotationType();

            for (int j = i + 1; j < annotations.length; j++) {
                Class<? extends Annotation> innerType = annotations[j].annotationType();

                if (INVALID_COMBINATIONS.contains(new AnnotationUtils.Combination(type, innerType))) {
                    throw new InvalidAnnotationCombinationException(
                        field, type, innerType
                    );
                }
            }
        }
    }

    private static boolean isIgnorableField(Field field) {
        return Modifier.isStatic(field.getModifiers()) || field.isAnnotationPresent(IgnoreField.class);
    }

    private static <T> boolean isIgnorableOrNullField(Field field, T object) {
        return isIgnorableField(field) || !RetrospectionUtils.isValuePresent(field, object);
    }

    private static final Set<AnnotationUtils.Combination> INVALID_COMBINATIONS;

    static {
        INVALID_COMBINATIONS = new HashSet<>();

        // Annotations for comparison operations
        addForbiddenCombinations(new Class<?>[]{
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
        });

        addForbiddenCombinations(new Class<?>[]{
            As.class,
            AsItself.class
        });
    }

    private static void addForbiddenCombinations(Class<?>[] annotations) {
        for (Class<?> clazz : annotations) {
            for (Class<?> innerClazz : annotations) {
                // As there is no @Repeatable annotation, there can be no same annotations at the same field.
                if (!clazz.equals(innerClazz)) {
                    addSet(clazz, innerClazz);
                }
            }
        }
    }

    private static void addSet(Class<?> annotation1, Class<?> annotation2) {
        INVALID_COMBINATIONS.add(new AnnotationUtils.Combination(annotation1, annotation2));
    }

    /**
     * Inner class Combination, consists of a pair of 'comparison' annotations. As private inner class, nothing outside this class can access it.
     */
    static class Combination {

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

            if (Objects.isNull(other) || getClass() != other.getClass()) {
                return false;
            }

            AnnotationUtils.Combination that = (AnnotationUtils.Combination) other;
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
