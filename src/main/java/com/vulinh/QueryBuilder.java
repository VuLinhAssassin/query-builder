package com.vulinh;

import static com.vulinh.data.ComparisonType.*;
import static com.vulinh.util.AnnotationUtils.checkInvalidAnnotationCombination;
import static com.vulinh.util.RetrospectionUtils.isValuePresent;
import static com.vulinh.util.StringUtils.CLOSE_PARENTHESIS;
import static com.vulinh.util.StringUtils.COLON;
import static com.vulinh.util.StringUtils.DOT;
import static com.vulinh.util.StringUtils.OPEN_PARENTHESIS;
import static com.vulinh.util.StringUtils.SPACE;
import static com.vulinh.util.StringUtils.SPACED_AND;
import static com.vulinh.util.StringUtils.SPACED_OR;
import static com.vulinh.util.StringUtils.isNotBlank;
import static java.lang.reflect.Modifier.isStatic;

import com.vulinh.annotation.IgnoreField;
import com.vulinh.annotation.UseCustomName;
import com.vulinh.annotation.UseTableAlias;
import com.vulinh.annotation.UseWrapMethod;
import com.vulinh.annotation.comparison.*;
import com.vulinh.data.ComparisonType;
import java.lang.reflect.Field;

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