package com.vulinh.querybuilder;

import com.vulinh.querybuilder.annotation.As;
import com.vulinh.querybuilder.annotation.AsItself;
import com.vulinh.querybuilder.annotation.CustomName;
import com.vulinh.querybuilder.annotation.TableAlias;
import com.vulinh.querybuilder.annotation.ValueWrapMethod;
import com.vulinh.querybuilder.annotation.WrapMethod;
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
import com.vulinh.querybuilder.annotation.comparison.NotLike;
import com.vulinh.querybuilder.annotation.comparison.OutRange;
import com.vulinh.querybuilder.support.AnnotationUtils;
import com.vulinh.querybuilder.support.ComparisonSign;
import com.vulinh.querybuilder.support.RangeComparisonType;
import com.vulinh.querybuilder.support.StringUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

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
    public <T> StringBuilder buildCountQuery(Class<T> clazz, CharSequence alias) {
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
    public <T> StringBuilder buildSingleSelectQuery(Class<T> clazz, CharSequence alias) {
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
    public <T> StringBuilder buildQuery(T object, CharSequence presetHql) {
        StringBuilder query = new StringBuilder();

        checkEmptyAndStartSpace(query, presetHql);

        for (Field field : AnnotationUtils.getNonIgnorableAndNonNullFields(object)) {
            // Check single comparison annotation
            AnnotationUtils.checkInvalidAnnotationCombination(field);

            query.append(StringUtils.SPACED_AND);

            query.append(StringUtils.OPEN_PARENTHESIS);

            // Field manipulations
            realizeFieldManipulation(query, field);

            // Comparisons
            realizeComparisonType(query, field);

            query.append(StringUtils.CLOSE_PARENTHESIS);
        }

        return query;
    }

    /**
     * Create select query that consists of many related entities, useful for query that retrieves data from more than one entity.
     *
     * @param clazz the provided DTO class.
     * @param <T>   the DTO type.
     * @return A string builder for DTO 'search' query (for continuous String mutation) generated of provided object.
     */
    public <T> StringBuilder buildMultiEntitiesSelectQuery(Class<T> clazz) {
        return buildMultiEntitiesSelectQuery(clazz, null);
    }

    /**
     * Create select query that consists of many related entities, useful for query that retrieves data from more than one entity.
     *
     * @param clazz    the provided DTO class.
     * @param followUp the follow-up CharSequence (can be anything related to String) after the select query, separated by a SPACE (" ") character.
     * @param <T>      the DTO type.
     * @return A string builder for DTO 'search' query (for continuous String mutation) generated of provided object.
     */
    public <T> StringBuilder buildMultiEntitiesSelectQuery(Class<T> clazz, CharSequence followUp) {
        StringBuilder query = new StringBuilder();

        query.append("select new ").append(clazz.getCanonicalName());

        query.append(StringUtils.OPEN_PARENTHESIS);

        List<Field> acceptedFields = AnnotationUtils.getNonIgnorableFields(clazz);

        if (acceptedFields.isEmpty()) {
            return query;
        }

        for (Field field : acceptedFields) {
            query.append(getActualFieldName(field))
                 .append(StringUtils.COMMA)
                 .append(StringUtils.SPACE);
        }

        // End index is exclusive it seems
        query.delete(query.length() - 2, query.length())
             .append(StringUtils.CLOSE_PARENTHESIS);

        if (StringUtils.isNotBlank(followUp)) {
            query.append(StringUtils.SPACE).append(followUp);
        }

        return query;
    }

    private <T> StringBuilder buildSelectQuery(Class<T> clazz, CharSequence alias, boolean isCountQuery) {
        StringBuilder query = new StringBuilder("select ");

        String[] classNameParts = clazz.getName().split("\\.");

        String classNameWithoutPackage = classNameParts[classNameParts.length - 1];

        CharSequence actualAlias;
        if (StringUtils.isNotBlank(alias)) {
            actualAlias = alias;
        } else {
            actualAlias = String.valueOf(Character.toLowerCase(classNameWithoutPackage.charAt(0)));
        }

        // Open count parenthesis
        if (isCountQuery) {
            query.append("count")
                 .append(StringUtils.OPEN_PARENTHESIS);
        }

        query.append(actualAlias);

        // Close count parenthesis
        if (isCountQuery) {
            query.append(StringUtils.CLOSE_PARENTHESIS);
        }

        return query.append(" from ")
                    .append(classNameWithoutPackage)
                    .append(StringUtils.SPACE)
                    .append(actualAlias)
                    .append(" where 1 = 1"); // for later parts of query condition
    }

    private static void realizeFieldManipulation(StringBuilder query, Field field) {
        // Opening method wrap
        if (field.isAnnotationPresent(WrapMethod.class)) {
            query.append(field.getAnnotation(WrapMethod.class).value())
                 .append(StringUtils.OPEN_PARENTHESIS);
        }

        // Table alias
        query.append(getActualFieldName(field));

        // Closing method wrap
        if (field.isAnnotationPresent(WrapMethod.class)) {
            String afterWrapMethod = field.getAnnotation(WrapMethod.class).after();

            if (StringUtils.isNotBlank(afterWrapMethod)) {
                query.append(StringUtils.SPACE)
                     .append(afterWrapMethod);
            }

            query.append(StringUtils.CLOSE_PARENTHESIS);
        }
    }

    private static void realizeComparisonType(StringBuilder query, Field field) {
        if (isNullComparison(query, field)) {
            return;
        }

        if (isRangeComparison(query, field)) {
            return;
        }

        processBinaryComparison(query, field);
    }

    private static void processBinaryComparison(StringBuilder query, Field field) {
        ValueWrapMethod valueWrapMethod = field.getAnnotation(ValueWrapMethod.class);

        if (field.isAnnotationPresent(GreaterThan.class)) {
            fillBinaryOperator(query, field, ComparisonSign.GREATER_THAN, valueWrapMethod);

            return;
        }

        if (field.isAnnotationPresent(GreaterThanOrEqualTo.class)) {
            fillBinaryOperator(query, field, ComparisonSign.GREATER_THAN_OR_EQUAL_TO, valueWrapMethod);

            return;
        }

        if (field.isAnnotationPresent(LessThan.class)) {
            fillBinaryOperator(query, field, ComparisonSign.LESS_THAN, valueWrapMethod);

            return;
        }

        if (field.isAnnotationPresent(LessThanOrEqualTo.class)) {
            fillBinaryOperator(query, field, ComparisonSign.LESS_THAN_OR_EQUAL_TO, valueWrapMethod);

            return;
        }

        if (field.isAnnotationPresent(NotEqual.class)) {
            fillBinaryOperator(query, field, ComparisonSign.NOT_EQUAL, valueWrapMethod);

            return;
        }

        if (field.isAnnotationPresent(Like.class)) {
            fillBinaryOperator(query, field, ComparisonSign.LIKE, valueWrapMethod);

            return;
        }

        if (field.isAnnotationPresent(NotLike.class)) {
            fillBinaryOperator(query, field, ComparisonSign.NOT_LIKE, valueWrapMethod);

            return;
        }

        fillBinaryOperator(query, field, ComparisonSign.EQUAL_TO, valueWrapMethod);
    }

    private static boolean isNullComparison(StringBuilder query, Field field) {
        if (field.isAnnotationPresent(IsNull.class)) {
            query.append(StringUtils.SPACE)
                 .append(ComparisonSign.IS_NULL.sign());

            return true;
        }

        if (field.isAnnotationPresent(IsNotNull.class)) {
            query.append(StringUtils.SPACE)
                 .append(ComparisonSign.IS_NOT_NULL.sign());

            return true;
        }
        return false;
    }

    private static boolean isRangeComparison(StringBuilder query, Field field) {
        if (field.isAnnotationPresent(Between.class)) {
            StringBuilder actualFromInclusive = renderWrapMethodForRangeComparisonValue(field, RangeComparisonType.BETWEEN_RANGE, true);
            StringBuilder actualToInclusive = renderWrapMethodForRangeComparisonValue(field, RangeComparisonType.BETWEEN_RANGE, false);

            query.append(StringUtils.SPACE)
                 .append(ComparisonSign.BETWEEN.sign())
                 .append(StringUtils.SPACE)
                 .append(actualFromInclusive)
                 .append(StringUtils.SPACED_AND)
                 .append(actualToInclusive);

            return true;
        }

        if (field.isAnnotationPresent(InRange.class)) {
            InRange inRangeAnnotation = field.getAnnotation(InRange.class);

            StringBuilder actualFromInclusive = renderWrapMethodForRangeComparisonValue(field, RangeComparisonType.IN_RANGE, true);
            StringBuilder actualToInclusive = renderWrapMethodForRangeComparisonValue(field, RangeComparisonType.IN_RANGE, false);

            query.append(StringUtils.SPACE)
                 .append(inRangeAnnotation.inclusivity() ? ComparisonSign.GREATER_THAN_OR_EQUAL_TO.sign() : ComparisonSign.GREATER_THAN.sign())
                 .append(StringUtils.SPACE)
                 .append(actualFromInclusive)
                 .append(StringUtils.SPACED_AND)
                 .append(getActualFieldName(field))
                 .append(StringUtils.SPACE)
                 .append(inRangeAnnotation.inclusivity() ? ComparisonSign.LESS_THAN_OR_EQUAL_TO.sign() : ComparisonSign.LESS_THAN.sign())
                 .append(StringUtils.SPACE)
                 .append(actualToInclusive);

            return true;
        }

        if (field.isAnnotationPresent(OutRange.class)) {
            OutRange inRangeAnnotation = field.getAnnotation(OutRange.class);

            StringBuilder actualFromInclusive = renderWrapMethodForRangeComparisonValue(field, RangeComparisonType.OUT_RANGE, true);
            StringBuilder actualToInclusive = renderWrapMethodForRangeComparisonValue(field, RangeComparisonType.OUT_RANGE, false);

            query.append(StringUtils.SPACE)
                 .append(inRangeAnnotation.inclusivity() ? ComparisonSign.LESS_THAN_OR_EQUAL_TO.sign() : ComparisonSign.LESS_THAN.sign())
                 .append(StringUtils.SPACE)
                 .append(actualFromInclusive)
                 .append(StringUtils.SPACED_OR)
                 .append(getActualFieldName(field))
                 .append(StringUtils.SPACE)
                 .append(inRangeAnnotation.inclusivity() ? ComparisonSign.GREATER_THAN_OR_EQUAL_TO.sign() : ComparisonSign.GREATER_THAN.sign())
                 .append(StringUtils.SPACE)
                 .append(actualToInclusive);

            return true;
        }

        return false;
    }

    private static StringBuilder getActualFieldName(Field field) {
        StringBuilder fieldNameBuilder = new StringBuilder();

        if (field.isAnnotationPresent(TableAlias.class)) {
            fieldNameBuilder.append(field.getAnnotation(TableAlias.class).value())
                            .append(StringUtils.DOT);
        }

        if (field.isAnnotationPresent(CustomName.class)) {
            fieldNameBuilder.append(field.getAnnotation(CustomName.class).value());
        } else {
            fieldNameBuilder.append(field.getName());
        }

        if (field.isAnnotationPresent(AsItself.class)) {
            fieldNameBuilder.append(StringUtils.SPACED_AS)
                            .append(field.getName());
        } else if (field.isAnnotationPresent(As.class)) {
            fieldNameBuilder.append(StringUtils.SPACED_AS)
                            .append(field.getAnnotation(As.class).value());
        }

        return fieldNameBuilder;
    }

    private static void fillBinaryOperator(StringBuilder query, Field field, ComparisonSign comparisonType, ValueWrapMethod wrapMethodValue) {
        StringBuilder actualValueFieldName = renderWrapMethodForBinaryComparisonValue(field, wrapMethodValue);

        query.append(StringUtils.SPACE)
             .append(comparisonType.sign())
             .append(StringUtils.SPACE)
             .append(actualValueFieldName);
    }

    private static StringBuilder renderWrapMethodForBinaryComparisonValue(Field field, ValueWrapMethod wrapMethodValue) {
        StringBuilder actualValueFieldName = new StringBuilder();

        if (Objects.nonNull(wrapMethodValue)) {
            actualValueFieldName.append(wrapMethodValue.value())
                                .append(StringUtils.OPEN_PARENTHESIS);
        }

        actualValueFieldName.append(StringUtils.COLON)
                            .append(field.getName());

        if (Objects.nonNull(wrapMethodValue)) {
            String after = wrapMethodValue.after();
            if (StringUtils.isNotBlank(after)) {
                actualValueFieldName.append(StringUtils.SPACE)
                                    .append(after);
            }

            actualValueFieldName.append(StringUtils.CLOSE_PARENTHESIS);
        }
        return actualValueFieldName;
    }

    private static StringBuilder renderWrapMethodForRangeComparisonValue(Field field, RangeComparisonType rangeComparisonType, boolean isFromInclusivePart) {
        StringBuilder actualValuePart = new StringBuilder();

        boolean isWrapMethodValueMarked = false;

        if (field.isAnnotationPresent(ValueWrapMethod.class)) {
            isWrapMethodValueMarked = true;

            ValueWrapMethod valueWrapMethod = field.getAnnotation(ValueWrapMethod.class);

            actualValuePart.append(valueWrapMethod.value())
                           .append(StringUtils.OPEN_PARENTHESIS);
        }

        actualValuePart.append(StringUtils.COLON)
                       .append(parseValuePart(field, rangeComparisonType, isFromInclusivePart));

        if (isWrapMethodValueMarked) {
            ValueWrapMethod valueWrapMethod = field.getAnnotation(ValueWrapMethod.class);

            String after = valueWrapMethod.after();

            if (StringUtils.isNotBlank(after)) {
                actualValuePart.append(StringUtils.SPACE)
                               .append(after);
            }

            actualValuePart.append(StringUtils.CLOSE_PARENTHESIS);
        }

        return actualValuePart;
    }

    private static String parseValuePart(Field field, RangeComparisonType rangeComparisonType, boolean isFromInclusivePart) {
        switch (rangeComparisonType) {
            case BETWEEN_RANGE: {
                Between annotation = field.getAnnotation(Between.class);
                return isFromInclusivePart ? annotation.fromInclusive() : annotation.toInclusive();
            }

            case IN_RANGE: {
                InRange annotation = field.getAnnotation(InRange.class);
                return isFromInclusivePart ? annotation.fromField() : annotation.toField();
            }

            default: {
                OutRange annotation = field.getAnnotation(OutRange.class);
                return isFromInclusivePart ? annotation.fromField() : annotation.toField();
            }
        }
    }

    private static void checkEmptyAndStartSpace(StringBuilder query, CharSequence presetQuery) {
        if (StringUtils.isNotBlank(presetQuery)) {
            query.append(presetQuery);
        }
    }
}

