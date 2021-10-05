package com.vulinh;

import com.vulinh.annotation.IgnoreField;
import com.vulinh.annotation.UseCustomName;
import com.vulinh.annotation.UseTableAlias;
import com.vulinh.annotation.UseWrapMethod;
import com.vulinh.annotation.comparison.*;
import com.vulinh.data.BuilderException;
import com.vulinh.data.ComparisonType;
import com.vulinh.data.InvalidAnnotationCombinationException;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.vulinh.AnnotationUtils.checkInvalidAnnotationCombination;
import static com.vulinh.RetrospectionUtils.isValuePresent;
import static com.vulinh.data.ComparisonType.*;
import static com.vulinh.util.StringUtils.*;
import static java.lang.String.format;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Main class for this library.
 *
 * @author Nguyen Vu Linh
 */
public class QueryBuilder {

    /**
     * Create a single count query for a certain entity class, using the first character of class name as alias (lowercase).
     *
     * @param clazz Input entity class.
     * @param <T>   Entity type.
     * @return A string builder for single count query (for continuous string mutation) generated of provided class.
     */
    public <T> StringBuilder buildCountQuery(Class<T> clazz) {
        return buildCountQuery(clazz, null);
    }

    /**
     * Create a single count query for a certain entity class.
     *
     * @param clazz Input entity class.
     * @param alias The alias that represents the entity in field selection.
     * @param <T>   Entity type.
     * @return A string builder for single count query (for continuous string mutation) generated of provided class.
     */
    public <T> StringBuilder buildCountQuery(Class<T> clazz, String alias) {
        return buildSelectQuery(clazz, alias, true);
    }

    /**
     * Create a single select query for a certain entity class, using the first character of class name as alias (lowercase).
     *
     * @param clazz Input entity class.
     * @param <T>   Entity type.
     * @return A string builder for single select query (for continuous string mutation) generated of provided class.
     */
    public <T> StringBuilder buildSingleSelectQuery(Class<T> clazz) {
        return buildSingleSelectQuery(clazz, null);
    }

    /**
     * Create a single select query for a certain entity class.
     *
     * @param clazz Input entity class.
     * @param alias The alias that represents the entity in field selection.
     * @param <T>   Entity type.
     * @return A string builder for single select query (for continuous string mutation) generated of provided class.
     */
    public <T> StringBuilder buildSingleSelectQuery(Class<T> clazz, String alias) {
        return buildSelectQuery(clazz, alias, false);
    }

    /**
     * Create 'search' query of a given object.
     *
     * @param object    Input object.
     * @param presetHql Pre-built query to concatenate with result.
     * @param <T>       Object type.
     * @return A string builder for 'search' query (for continuous String mutation) generated of provided object.
     */
    public <T> StringBuilder buildQuery(T object, String presetHql) {
        StringBuilder query = new StringBuilder();

        checkEmptyAndStartSpace(query, presetHql);

        for (Field field : object.getClass().getDeclaredFields()) {
            if (isIgnorableField(object, field)) {
                continue;
            }

            // Check single comparison annotation
            checkInvalidAnnotationCombination(field);

            query.append(SPACED_AND);

            query.append(OPEN_PARENTHESIS);

            // Field manipulations
            realizeFieldManipulation(query, field);

            // Comparisons
            realizeComparisonType(query, field);

            query.append(CLOSE_PARENTHESIS);
        }

        return query;
    }

    private <T> StringBuilder buildSelectQuery(Class<T> clazz, String alias, boolean isCountQuery) {
        StringBuilder query = new StringBuilder("select ");

        String[] classNameParts = clazz.getName().split("\\.");

        String classNameWithoutPackage = classNameParts[classNameParts.length - 1];

        String actualAlias;
        if (isNotBlank(alias)) {
            actualAlias = alias;
        } else {
            actualAlias = String.valueOf(Character.toLowerCase(classNameWithoutPackage.charAt(0)));
        }

        // Open count parenthesis
        if (isCountQuery) {
            query.append("count")
                 .append(OPEN_PARENTHESIS);
        }

        query.append(actualAlias);

        // Close count parenthesis
        if (isCountQuery) {
            query.append(CLOSE_PARENTHESIS);
        }

        return query.append(" of ")
                    .append(classNameWithoutPackage)
                    .append(SPACE)
                    .append(actualAlias)
                    .append(" where 1 = 1"); // for later parts of query condition
    }

    private static <T> boolean isIgnorableField(T object, Field field) {
        // Check if a field is marked as @IgnoreField or value not present (null); static field will also be ignored
        return isStatic(field.getModifiers()) || field.isAnnotationPresent(IgnoreField.class) || !isValuePresent(field, object);
    }

    private static void realizeFieldManipulation(StringBuilder query, Field field) {
        String fieldName = field.getName();

        // Opening method wrap
        if (field.isAnnotationPresent(UseWrapMethod.class)) {
            query.append(field.getAnnotation(UseWrapMethod.class).value())
                 .append(OPEN_PARENTHESIS);
        }

        // Table alias
        actuallyBuildFieldName(field, fieldName, query);

        // Closing method wrap
        if (field.isAnnotationPresent(UseWrapMethod.class)) {
            String afterWrapMethod = field.getAnnotation(UseWrapMethod.class).after();

            if (isNotBlank(afterWrapMethod)) {
                query.append(SPACE)
                     .append(afterWrapMethod);
            }

            query.append(CLOSE_PARENTHESIS);
        }
    }

    private static void realizeComparisonType(StringBuilder query, Field field) {
        String fieldName = field.getName();

        if (isNullComparison(query, field)) {
            return;
        }

        if (isRangeComparison(query, field, fieldName)) {
            return;
        }

        processBinaryComparison(query, field);
    }

    private static void processBinaryComparison(StringBuilder query, Field field) {
        if (field.isAnnotationPresent(GreaterThan.class)) {
            fillBinaryOperator(query, field, GREATER_THAN);

            return;
        }

        if (field.isAnnotationPresent(GreaterThanOrEqualTo.class)) {
            fillBinaryOperator(query, field, GREATER_THAN_OR_EQUAL_TO);

            return;
        }

        if (field.isAnnotationPresent(LessThan.class)) {
            fillBinaryOperator(query, field, LESS_THAN);

            return;
        }

        if (field.isAnnotationPresent(LessThanOrEqualTo.class)) {
            fillBinaryOperator(query, field, LESS_THAN_OR_EQUAL_TO);

            return;
        }

        if (field.isAnnotationPresent(NotEqual.class)) {
            fillBinaryOperator(query, field, NOT_EQUAL);

            return;
        }

        if (field.isAnnotationPresent(Like.class)) {
            fillBinaryOperator(query, field, LIKE);

            return;
        }

        if (field.isAnnotationPresent(NotLike.class)) {
            fillBinaryOperator(query, field, NOT_LIKE);

            return;
        }

        fillBinaryOperator(query, field, EQUAL_TO);
    }

    private static boolean isNullComparison(StringBuilder query, Field field) {
        if (field.isAnnotationPresent(IsNull.class)) {
            query.append(SPACE)
                 .append(IS_NULL.sign());

            return true;
        }

        if (field.isAnnotationPresent(IsNotNull.class)) {
            query.append(SPACE)
                 .append(IS_NOT_NULL.sign());

            return true;
        }
        return false;
    }

    private static boolean isRangeComparison(StringBuilder query, Field field, String fieldName) {
        if (field.isAnnotationPresent(Between.class)) {
            Between betweenAnnotation = field.getAnnotation(Between.class);
            query.append(SPACE)
                 .append(BETWEEN.sign())
                 .append(SPACE)
                 .append(COLON)
                 .append(betweenAnnotation.fromInclusive())
                 .append(SPACED_AND)
                 .append(COLON)
                 .append(betweenAnnotation.toInclusive());

            return true;
        }

        if (field.isAnnotationPresent(InRange.class)) {
            InRange inRangeAnnotation = field.getAnnotation(InRange.class);

            query.append(SPACE)
                 .append(inRangeAnnotation.inclusivity() ? GREATER_THAN_OR_EQUAL_TO.sign() : GREATER_THAN.sign())
                 .append(SPACE)
                 .append(COLON)
                 .append(inRangeAnnotation.fromField())
                 .append(SPACED_AND)
                 .append(getActualFieldNameForRangeComparison(field, fieldName))
                 .append(SPACE)
                 .append(inRangeAnnotation.inclusivity() ? LESS_THAN_OR_EQUAL_TO.sign() : LESS_THAN.sign())
                 .append(SPACE)
                 .append(COLON)
                 .append(inRangeAnnotation.toField());

            return true;
        }

        if (field.isAnnotationPresent(OutRange.class)) {
            OutRange inRangeAnnotation = field.getAnnotation(OutRange.class);

            query.append(SPACE)
                 .append(inRangeAnnotation.inclusivity() ? LESS_THAN_OR_EQUAL_TO.sign() : LESS_THAN.sign())
                 .append(SPACE)
                 .append(COLON)
                 .append(inRangeAnnotation.fromField())
                 .append(SPACED_OR)
                 .append(getActualFieldNameForRangeComparison(field, fieldName))
                 .append(SPACE)
                 .append(inRangeAnnotation.inclusivity() ? GREATER_THAN_OR_EQUAL_TO.sign() : GREATER_THAN.sign())
                 .append(SPACE)
                 .append(COLON)
                 .append(inRangeAnnotation.toField());

            return true;
        }

        return false;
    }

    private static String getActualFieldNameForRangeComparison(Field field, String fieldName) {
        return actuallyBuildFieldName(field, fieldName, new StringBuilder()).toString();
    }

    private static StringBuilder actuallyBuildFieldName(Field field, String fieldName, StringBuilder fieldNameBuilder) {
        if (field.isAnnotationPresent(UseTableAlias.class)) {
            fieldNameBuilder.append(field.getAnnotation(UseTableAlias.class).value())
                            .append(DOT);
        }

        if (field.isAnnotationPresent(UseCustomName.class)) {
            fieldNameBuilder.append(field.getAnnotation(UseCustomName.class).value());
        } else {
            fieldNameBuilder.append(fieldName);
        }

        return fieldNameBuilder;
    }

    private static void fillBinaryOperator(StringBuilder query, Field field, ComparisonType comparisonType) {
        query.append(SPACE)
             .append(comparisonType.sign())
             .append(SPACE)
             .append(COLON)
             .append(field.getName());
    }

    private static void checkEmptyAndStartSpace(StringBuilder query, String presetQuery) {
        if (isNotBlank(presetQuery)) {
            query.append(presetQuery);
        }
    }
}

/**
 * Utility class for doing some inspection on the annotations used by this library.
 *
 * @author Nguyen Vu Linh
 */
final class AnnotationUtils {

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

        if (annotations.length <= 1) {
            return;
        }

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

/**
 * Utility class for doing some retrospection by this library.
 *
 * @author Nguyen Vu Linh
 */
final class RetrospectionUtils {

    private RetrospectionUtils() {
        throw new UnsupportedOperationException("Cannot instantiate utility class!");
    }

    /**
     * Invoke field's getter method to see if field contains value. Will throw exception if the provided object is not a valid Java object, or getter method is
     * missing.
     *
     * @param field  The field to test.
     * @param object The object that contains said field.
     * @param <T>    Object type.
     * @return <code>true</code> if said field has non-null value; <code>false</code> if otherwise.
     * @throws BuilderException when the provided object cannot be retrospected to invoke getter method.
     */
    public static <T> boolean isValuePresent(Field field, T object) {
        try {
            PropertyDescriptor descriptor = new PropertyDescriptor(field.getName(), object.getClass());
            return nonNull(descriptor.getReadMethod().invoke(object));
        } catch (Exception ex) {
            throw new BuilderException(
                    format("Either class %s is not a valid bean or getter method not present for field %s", object.getClass(), field), ex
            );
        }
    }
}