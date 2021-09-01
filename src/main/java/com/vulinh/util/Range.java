package com.vulinh.util;

import static java.util.Objects.nonNull;

import java.util.Comparator;
import java.util.Objects;

/**
 * A rudimentary implementation of famous Range API from Apache Common library (in case said library was not allowed on some very old project).
 *
 * @param <E> object type.
 * @author Nguyen Vu Linh
 */
public class Range<E extends Comparable<? super E>> {

    /**
     * Check if the input value is between two bounds.
     *
     * @param value The input value.
     * @return <code>true</code> if value is between two bounds of this range (inclusive or not depending on how the Range object is initialized);
     * <code>false</code> if otherwise.
     */
    public boolean isBetween(E value) {
        if (nonNull(comparator)) {
            return isInclusive
                ? comparator.compare(value, fromValue) >= 0 && comparator.compare(value, toValue) <= 0
                : comparator.compare(value, fromValue) > 0 && comparator.compare(value, toValue) < 0;
        }

        return isInclusive
            ? value.compareTo(fromValue) >= 0 && value.compareTo(toValue) <= 0
            : value.compareTo(fromValue) > 0 && value.compareTo(toValue) < 0;
    }

    /**
     * Check if the input value is outside two bounds.
     *
     * @param value The input value.
     * @return <code>true</code> if value is outside two bounds of this range (inclusive or not depending on how the Range object is initialized);
     * <code>false</code> if otherwise.
     */
    public boolean isOutside(E value) {
        return !isBetween(value);
    }

    /**
     * Initialize a Range object with provided 'from' value and 'to' value. The value must also has its own <code>Comparable</code> implementation. This
     * initialization is not with inclusivity (do not check if future input value to compare is equal to either bound).
     *
     * @param fromValue The 'from' value.
     * @param toValue   The 'to' value.
     * @param <E>       value type.
     * @return An immutable Range object with two bounds (that has been checked to make sure that lower bound and upper bound are where they are supposed to
     * be), and without inclusivity.
     */
    public static <E extends Comparable<? super E>> Range<E> of(E fromValue, E toValue) {
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
    public static <E extends Comparable<? super E>> Range<E> of(E fromValue, E toValue, boolean isInclusive) {
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
    public static <E extends Comparable<? super E>> Range<E> of(E fromValue, E toValue, Comparator<E> comparator) {
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
    public static <E extends Comparable<? super E>> Range<E> of(E fromValue, E toValue, Comparator<E> comparator, boolean isInclusive) {
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

    private Range(E fromValue, E toValue, Comparator<? super E> comparator, boolean isInclusive) {
        this.comparator = comparator;
        this.isInclusive = isInclusive;

        if ((nonNull(comparator) && comparator.compare(fromValue, toValue) > 0) || (fromValue.compareTo(toValue) > 0)) {
            this.fromValue = toValue;
            this.toValue = fromValue;
        } else {
            this.fromValue = fromValue;
            this.toValue = toValue;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Range)) {
            return false;
        }

        Range<?> range = (Range<?>) o;
        return isInclusive == range.isInclusive && Objects.equals(fromValue, range.fromValue) && Objects.equals(toValue, range.toValue)
            && Objects.equals(comparator, range.comparator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromValue, toValue, comparator, isInclusive);
    }

    private final E                     fromValue;
    private final E                     toValue;
    private final Comparator<? super E> comparator;
    private final boolean               isInclusive;
}