package com.vulinh.util;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.Comparator;

/**
 * A rudimentary implementation of famous Range API from Apache Common library (in case said library was not allowed on some very old projects).
 *
 * @param <E> Object type.
 * @author Nguyen Vu Linh
 */
@SuppressWarnings("unchecked")
public class Range<E> {

    /**
     * Check if the input value is between two bounds.
     *
     * @param value The input value.
     * @return <code>true</code> if value is between two bounds of this range (inclusive or not will depend on how the Range object is initialized);
     * <code>false</code> if otherwise.
     */
    public boolean isBetween(E value) {
        if (nonNull(comparator)) {
            return isInclusive
                ? comparator.compare(value, fromValue) >= 0 && comparator.compare(value, toValue) <= 0
                : comparator.compare(value, fromValue) > 0 && comparator.compare(value, toValue) < 0;
        }

        Comparable<E> comparableValue = (Comparable<E>) value;

        return isInclusive
            ? comparableValue.compareTo(fromValue) >= 0 && comparableValue.compareTo(toValue) <= 0
            : comparableValue.compareTo(fromValue) > 0 && comparableValue.compareTo(toValue) < 0;
    }

    /**
     * Check if the input value is outside two bounds.
     *
     * @param value The input value.
     * @return <code>true</code> if value is outside two bounds of this range (inclusive or not will depend on how the Range object is initialized);
     * <code>false</code> if otherwise.
     */
    public boolean isOutside(E value) {
        return !isBetween(value);
    }

    /**
     * Initialize a Range object with provided 'from' value and 'to' value. The value must also have its own <code>Comparable</code> implementation. This
     * initialization is not with inclusivity (do not check if future input value to compare is equal to either bound).
     *
     * @param fromValue The 'from' value.
     * @param toValue   The 'to' value.
     * @param <E>       value type.
     * @return An immutable Range object with two bounds (that has been checked to make sure that lower bound and upper bound are where they are supposed to
     * be), and without inclusivity.
     */
    public static <E> Range<E> of(E fromValue, E toValue) {
        return of(fromValue, toValue, null, false);
    }

    /**
     * Initialize a Range object with provided 'from' value and 'to' value. The value must also have its own <code>Comparable</code> implementation. The
     * inclusivity can be set with this method.
     *
     * @param fromValue   The 'from' value.
     * @param toValue     The 'to' value.
     * @param isInclusive If the Range object is with inclusivity or not.
     * @param <E>         value type.
     * @return An immutable Range object with two bounds (that has been checked to make sure that lower bound and upper bound are where they are supposed to
     * be). The inclusivity can be set with this method.
     */
    public static <E> Range<E> of(E fromValue, E toValue, boolean isInclusive) {
        return of(fromValue, toValue, null, isInclusive);
    }

    /**
     * Initialize a Range object with provided 'from' value and 'to' value, and this initialization accepts custom implementation of Comparator. This
     * initialization is not with inclusivity (do not check if future input value to compare is equal to either bound).
     *
     * @param fromValue  The 'from' value.
     * @param toValue    The 'to' value.
     * @param comparator Custom comparator that will be used to compare value with two bounds.
     * @param <E>        value type.
     * @return An immutable Range object with two bounds (that has been checked to make sure that lower bound and upper bound are where they are supposed to
     * be), and with preset inclusivity.
     */
    public static <E> Range<E> of(E fromValue, E toValue, Comparator<E> comparator) {
        return of(fromValue, toValue, comparator, false);
    }

    /**
     * Initialize a Range object with provided 'from' value and 'to' value, and this initialization accepts custom implementation of Comparator. The inclusivity
     * can be set with this method.
     *
     * @param fromValue   The 'from' value.
     * @param toValue     The 'to' value.
     * @param comparator  Custom comparator that will be used to compare value with two bounds.
     * @param isInclusive If the Range object is with inclusivity or not.
     * @param <E>         value type.
     * @return An immutable Range object with two bounds (that has been checked to make sure that lower bound and upper bound are where they are supposed to
     * be), and with preset inclusivity and custom comparator.
     */
    public static <E> Range<E> of(E fromValue, E toValue, Comparator<E> comparator, boolean isInclusive) {
        return new Range<>(fromValue, toValue, comparator, isInclusive);
    }

    /**
     * Generate a new Range object with new 'from' value. Others are taken from old object.
     *
     * @param fromValue New 'from' value.
     * @return A new Range object with new 'from' value.
     */
    public Range<E> withFromValue(E fromValue) {
        return new Range<>(fromValue, toValue, comparator, isInclusive);
    }

    /**
     * Generate a new Range object with new 'to' value. Others are taken from old object.
     *
     * @param toValue New 'to' value.
     * @return A new Range object with new 'to' value.
     */
    public Range<E> withToValue(E toValue) {
        return new Range<>(fromValue, toValue, comparator, isInclusive);
    }

    /**
     * Generate a new Range object with new two bound values. Others are taken from old object.
     *
     * @param fromValue New 'from' value.
     * @param toValue   New 'to' value.
     * @return A new Range object with new two bound values.
     */
    public Range<E> withBoundValues(E fromValue, E toValue) {
        return new Range<>(fromValue, toValue, comparator, isInclusive);
    }

    /**
     * Generate a new Range object with new custom comparator. Others are taken from old object.
     *
     * @param comparator New custom comparator.
     * @return A new Range object with new custom comparator.
     */
    public Range<E> withComparator(Comparator<E> comparator) {
        return new Range<>(fromValue, toValue, comparator, isInclusive);
    }

    /**
     * Generate a new Range object with new inclusivity setting. Others are taken from old object.
     *
     * @param isInclusive New inclusivity setting.
     * @return A new Range object with inclusivity setting.
     */
    public Range<E> withInclusivity(boolean isInclusive) {
        return new Range<>(fromValue, toValue, comparator, isInclusive);
    }

    private Range(E fromValue, E toValue, Comparator<E> comparator, boolean isInclusive) {
        if (isNull(fromValue)) {
            throw new IllegalArgumentException("fromValue cannot be null!");
        }

        if (isNull(toValue)) {
            throw new IllegalArgumentException("toValue cannot be null!");
        }

        this.comparator = comparator;
        this.isInclusive = isInclusive;

        if (nonNull(comparator)) {
            if (comparator.compare(fromValue, toValue) > 0) {
                this.fromValue = toValue;
                this.toValue = fromValue;
            } else {
                this.fromValue = fromValue;
                this.toValue = toValue;
            }
        } else if (fromValue instanceof Comparable && toValue instanceof Comparable) {
            Comparable<E> comparableFromValue = (Comparable<E>) fromValue;

            if (comparableFromValue.compareTo(toValue) > 0) {
                this.fromValue = toValue;
                this.toValue = fromValue;
            } else {
                this.fromValue = fromValue;
                this.toValue = toValue;
            }
        } else {
            throw new IllegalArgumentException("Both fromValue and toValue must implement Comparable if no Comparator is supplied!");
        }
    }

    @Override
    public String toString() {
        return "Range{" +
            "fromValue=" + fromValue +
            ", toValue=" + toValue +
            ", comparator=" + comparator +
            ", isInclusive=" + isInclusive +
            '}';
    }

    private final E             fromValue;
    private final E             toValue;
    private final Comparator<E> comparator;
    private final boolean       isInclusive;
}