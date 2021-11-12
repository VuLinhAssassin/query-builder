package com.vulinh;

import com.vulinh.annotation.AsAlias;
import com.vulinh.annotation.AsIfself;
import com.vulinh.annotation.IgnoreField;
import com.vulinh.annotation.UseCustomName;
import com.vulinh.annotation.UseTableAlias;
import com.vulinh.annotation.UseWrapMethod;
import com.vulinh.annotation.UseWrapMethodValue;
import com.vulinh.annotation.comparison.Between;
import com.vulinh.annotation.comparison.GreaterThan;
import com.vulinh.annotation.comparison.GreaterThanOrEqualTo;
import com.vulinh.annotation.comparison.InRange;
import com.vulinh.annotation.comparison.IsNotNull;
import com.vulinh.annotation.comparison.IsNull;
import com.vulinh.annotation.comparison.LessThan;
import com.vulinh.annotation.comparison.LessThanOrEqualTo;
import com.vulinh.annotation.comparison.Like;
import com.vulinh.annotation.comparison.NotEqual;
import com.vulinh.annotation.comparison.NotLike;
import com.vulinh.annotation.comparison.OutRange;
import com.vulinh.data.BuilderException;
import com.vulinh.data.InvalidAnnotationCombinationException;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.vulinh.AnnotationUtils.checkInvalidAnnotationCombination;
import static com.vulinh.ComparisonSign.EQUAL_TO;
import static com.vulinh.ComparisonSign.GREATER_THAN;
import static com.vulinh.ComparisonSign.GREATER_THAN_OR_EQUAL_TO;
import static com.vulinh.ComparisonSign.IS_NOT_NULL;
import static com.vulinh.ComparisonSign.IS_NULL;
import static com.vulinh.ComparisonSign.LESS_THAN;
import static com.vulinh.ComparisonSign.LESS_THAN_OR_EQUAL_TO;
import static com.vulinh.ComparisonSign.LIKE;
import static com.vulinh.ComparisonSign.NOT_EQUAL;
import static com.vulinh.ComparisonSign.NOT_LIKE;
import static com.vulinh.RangeComparisonType.BETWEEN_RANGE;
import static com.vulinh.RangeComparisonType.IN_RANGE;
import static com.vulinh.RangeComparisonType.OUT_RANGE;
import static com.vulinh.RetrospectionUtils.isValuePresent;
import static com.vulinh.util.StringUtils.CLOSE_PARENTHESIS;
import static com.vulinh.util.StringUtils.COLON;
import static com.vulinh.util.StringUtils.COMMA;
import static com.vulinh.util.StringUtils.DOT;
import static com.vulinh.util.StringUtils.OPEN_PARENTHESIS;
import static com.vulinh.util.StringUtils.SPACE;
import static com.vulinh.util.StringUtils.SPACED_AND;
import static com.vulinh.util.StringUtils.SPACED_OR;
import static com.vulinh.util.StringUtils.isNotBlank;
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

        checkEmptyAndStartSpace(query, String.valueOf(presetHql));

        for (Field field : AnnotationUtils.getNonIgnorableAndNonNullFields(object)) {
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

    /**
     * Create select query that consists of many related entities, useful for query that retrieves data from more than one entities.
     *
     * @param clazz the provided DTO class.
     * @param <T>   the DTO type.
     * @return A string builder for DTO 'search' query (for continuous String mutation) generated of provided object.
     */
    public <T> StringBuilder buildMultiEntitiesSelectQuery(Class<T> clazz) {
        return buildMultiEntitiesSelectQuery(clazz, null);
    }

    /**
     * Create select query that consists of many related entities, useful for query that retrieves data from more than one entities.
     *
     * @param clazz    the provided DTO class.
     * @param followUp the follow-up CharSequence (can be anything related to String) after the select query, separated by a SPACE (" ") character.
     * @param <T>      the DTO type.
     * @return A string builder for DTO 'search' query (for continuous String mutation) generated of provided object.
     */
    public <T> StringBuilder buildMultiEntitiesSelectQuery(Class<T> clazz, CharSequence followUp) {
        StringBuilder query = new StringBuilder();

        query.append("select new ").append(clazz.getCanonicalName());

        query.append(OPEN_PARENTHESIS);

        List<Field> acceptedFields = AnnotationUtils.getNonIgnorableFields(clazz);

        for (Field field : acceptedFields) {
            query.append(getActualFieldName(field))
                 .append(COMMA)
                 .append(SPACE);
        }

        // End index is exclusive it seems
        query.delete(query.length() - 2, query.length())
             .append(CLOSE_PARENTHESIS);

        if (isNotBlank(followUp)) {
            query.append(SPACE).append(followUp);
        }

        return query;
    }

    private <T> StringBuilder buildSelectQuery(Class<T> clazz, CharSequence alias, boolean isCountQuery) {
        StringBuilder query = new StringBuilder("select ");

        String[] classNameParts = clazz.getName().split("\\.");

        String classNameWithoutPackage = classNameParts[classNameParts.length - 1];

        String actualAlias;
        if (isNotBlank(alias)) {
            actualAlias = String.valueOf(alias);
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

        return query.append(" from ")
                    .append(classNameWithoutPackage)
                    .append(SPACE)
                    .append(actualAlias)
                    .append(" where 1 = 1"); // for later parts of query condition
    }

    private static void realizeFieldManipulation(StringBuilder query, Field field) {
        // Opening method wrap
        if (field.isAnnotationPresent(UseWrapMethod.class)) {
            query.append(field.getAnnotation(UseWrapMethod.class).value())
                 .append(OPEN_PARENTHESIS);
        }

        // Table alias
        query.append(getActualFieldName(field));

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
        if (isNullComparison(query, field)) {
            return;
        }

        if (isRangeComparison(query, field)) {
            return;
        }

        processBinaryComparison(query, field);
    }

    private static void processBinaryComparison(StringBuilder query, Field field) {
        UseWrapMethodValue useWrapMethodValue = field.getAnnotation(UseWrapMethodValue.class);

        if (field.isAnnotationPresent(GreaterThan.class)) {
            fillBinaryOperator(query, field, GREATER_THAN, useWrapMethodValue);

            return;
        }

        if (field.isAnnotationPresent(GreaterThanOrEqualTo.class)) {
            fillBinaryOperator(query, field, GREATER_THAN_OR_EQUAL_TO, useWrapMethodValue);

            return;
        }

        if (field.isAnnotationPresent(LessThan.class)) {
            fillBinaryOperator(query, field, LESS_THAN, useWrapMethodValue);

            return;
        }

        if (field.isAnnotationPresent(LessThanOrEqualTo.class)) {
            fillBinaryOperator(query, field, LESS_THAN_OR_EQUAL_TO, useWrapMethodValue);

            return;
        }

        if (field.isAnnotationPresent(NotEqual.class)) {
            fillBinaryOperator(query, field, NOT_EQUAL, useWrapMethodValue);

            return;
        }

        if (field.isAnnotationPresent(Like.class)) {
            fillBinaryOperator(query, field, LIKE, useWrapMethodValue);

            return;
        }

        if (field.isAnnotationPresent(NotLike.class)) {
            fillBinaryOperator(query, field, NOT_LIKE, useWrapMethodValue);

            return;
        }

        fillBinaryOperator(query, field, EQUAL_TO, useWrapMethodValue);
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

    private static boolean isRangeComparison(StringBuilder query, Field field) {
        if (field.isAnnotationPresent(Between.class)) {
            StringBuilder actualFromInclusive = renderWrapMethodForRangeComparisonValue(field, BETWEEN_RANGE, true);
            StringBuilder actualToInclusive = renderWrapMethodForRangeComparisonValue(field, BETWEEN_RANGE, false);

            query.append(SPACE)
                 .append(ComparisonSign.BETWEEN.sign())
                 .append(SPACE)
                 .append(actualFromInclusive)
                 .append(SPACED_AND)
                 .append(actualToInclusive);

            return true;
        }

        if (field.isAnnotationPresent(InRange.class)) {
            InRange inRangeAnnotation = field.getAnnotation(InRange.class);

            StringBuilder actualFromInclusive = renderWrapMethodForRangeComparisonValue(field, IN_RANGE, true);
            StringBuilder actualToInclusive = renderWrapMethodForRangeComparisonValue(field, IN_RANGE, false);

            query.append(SPACE)
                 .append(inRangeAnnotation.inclusivity() ? GREATER_THAN_OR_EQUAL_TO.sign() : GREATER_THAN.sign())
                 .append(SPACE)
                 .append(actualFromInclusive)
                 .append(SPACED_AND)
                 .append(getActualFieldName(field))
                 .append(SPACE)
                 .append(inRangeAnnotation.inclusivity() ? LESS_THAN_OR_EQUAL_TO.sign() : LESS_THAN.sign())
                 .append(SPACE)
                 .append(actualToInclusive);

            return true;
        }

        if (field.isAnnotationPresent(OutRange.class)) {
            OutRange inRangeAnnotation = field.getAnnotation(OutRange.class);

            StringBuilder actualFromInclusive = renderWrapMethodForRangeComparisonValue(field, OUT_RANGE, true);
            StringBuilder actualToInclusive = renderWrapMethodForRangeComparisonValue(field, OUT_RANGE, false);

            query.append(SPACE)
                 .append(inRangeAnnotation.inclusivity() ? LESS_THAN_OR_EQUAL_TO.sign() : LESS_THAN.sign())
                 .append(SPACE)
                 .append(actualFromInclusive)
                 .append(SPACED_OR)
                 .append(getActualFieldName(field))
                 .append(SPACE)
                 .append(inRangeAnnotation.inclusivity() ? GREATER_THAN_OR_EQUAL_TO.sign() : GREATER_THAN.sign())
                 .append(SPACE)
                 .append(actualToInclusive);

            return true;
        }

        return false;
    }

    private static StringBuilder getActualFieldName(Field field) {
        StringBuilder fieldNameBuilder = new StringBuilder();

        if (field.isAnnotationPresent(UseTableAlias.class)) {
            fieldNameBuilder.append(field.getAnnotation(UseTableAlias.class).value())
                            .append(DOT);
        }

        if (field.isAnnotationPresent(UseCustomName.class)) {
            fieldNameBuilder.append(field.getAnnotation(UseCustomName.class).value());
        } else {
            fieldNameBuilder.append(field.getName());
        }

        if (field.isAnnotationPresent(AsIfself.class)) {
            fieldNameBuilder.append(" as ")
                            .append(field.getName());
        } else if (field.isAnnotationPresent(AsAlias.class)) {
            fieldNameBuilder.append(" as ")
                            .append(field.getAnnotation(AsAlias.class).value());
        }

        return fieldNameBuilder;
    }

    private static void fillBinaryOperator(StringBuilder query, Field field, ComparisonSign comparisonType, UseWrapMethodValue wrapMethodValue) {
        StringBuilder actualValueFieldName = renderWrapMethodForBinaryComparisonValue(field, wrapMethodValue);

        query.append(SPACE)
             .append(comparisonType.sign())
             .append(SPACE)
             .append(actualValueFieldName);
    }

    private static StringBuilder renderWrapMethodForBinaryComparisonValue(Field field, UseWrapMethodValue wrapMethodValue) {
        StringBuilder actualValueFieldName = new StringBuilder();

        if (nonNull(wrapMethodValue)) {
            actualValueFieldName.append(wrapMethodValue.value())
                                .append(OPEN_PARENTHESIS);
        }

        actualValueFieldName.append(COLON)
                            .append(field.getName());

        if (nonNull(wrapMethodValue)) {
            String after = wrapMethodValue.after();
            if (isNotBlank(after)) {
                actualValueFieldName.append(SPACE)
                                    .append(after);
            }

            actualValueFieldName.append(CLOSE_PARENTHESIS);
        }
        return actualValueFieldName;
    }

    private static StringBuilder renderWrapMethodForRangeComparisonValue(Field field, RangeComparisonType rangeComparisonType, boolean isFromInclusivePart) {
        StringBuilder actualValuePart = new StringBuilder();

        boolean isWrapMethodValueMarked = false;

        if (field.isAnnotationPresent(UseWrapMethodValue.class)) {
            isWrapMethodValueMarked = true;

            UseWrapMethodValue useWrapMethodValue = field.getAnnotation(UseWrapMethodValue.class);

            actualValuePart.append(useWrapMethodValue.value())
                           .append(OPEN_PARENTHESIS);
        }

        actualValuePart.append(COLON)
                       .append(parseValuePart(field, rangeComparisonType, isFromInclusivePart));

        if (isWrapMethodValueMarked) {
            UseWrapMethodValue useWrapMethodValue = field.getAnnotation(UseWrapMethodValue.class);

            String after = useWrapMethodValue.after();

            if (isNotBlank(after)) {
                actualValuePart.append(SPACE)
                               .append(after);
            }

            actualValuePart.append(CLOSE_PARENTHESIS);
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
     * must also have its value (non null).
     *
     * @param object The provided object.
     * @param <T>    The class type.
     * @return A list of fields that are not static AND not marked with <code>@IgnoreField</code>, in addition to having value (non null).
     */
    public static <T> List<Field> getNonIgnorableAndNonNullFields(T object) {
        if (isNull(object)) {
            throw new IllegalArgumentException("Use null object is not allowed!");
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

                if (INVALID_COMBINATIONS.contains(new Combination(type, innerType))) {
                    throw new InvalidAnnotationCombinationException(
                            field, type, innerType
                    );
                }
            }
        }
    }

    private static boolean isIgnorableField(Field field) {
        return isStatic(field.getModifiers()) || field.isAnnotationPresent(IgnoreField.class);
    }

    private static <T> boolean isIgnorableOrNullField(Field field, T object) {
        return isIgnorableField(field) || !isValuePresent(field, object);
    }

    private static final Set<Combination> INVALID_COMBINATIONS;

    static {
        INVALID_COMBINATIONS = new HashSet<>();

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
                AsAlias.class,
                AsIfself.class
        });
    }

    private static void addForbiddenCombinations(Class<?>[] annotations) {
        for (Class<?> clazz : annotations) {
            for (Class<?> innerClazz : annotations) {
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

/**
 * Some common comparison types.
 *
 * @author Nguyen Vu Linh
 */
enum ComparisonSign {
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL_TO(">="),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL_TO("<="),
    NOT_EQUAL("!="),
    EQUAL_TO("="),
    IS_NULL("is null"),
    IS_NOT_NULL("is not null"),
    BETWEEN("between"),
    LIKE("like"),
    NOT_LIKE("not like");

    private final String sign;

    ComparisonSign(String sign) {
        this.sign = sign;
    }

    /**
     * Return the 'sign' used for the comparison in question.
     *
     * @return Sign used for the comparison in question.
     */
    public String sign() {
        return sign;
    }
}

/**
 * Describe range comparison type.
 *
 * @author Nguyen Vu Linh
 */
enum RangeComparisonType {
    IN_RANGE, OUT_RANGE, BETWEEN_RANGE
}