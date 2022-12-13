/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Helper methods for generated binding code. This class concentrates useful primitives generated code may call
 * to perform specific shared functions. This allows for generated classes to be leaner. Methods in this class follows
 * general API stability requirements of the Binding Specification.
 *
 * @author Robert Varga
 */
public final class CodeHelpers {
    private CodeHelpers() {
        // Hidden
    }

    /**
     * Require that an a value-related expression is true.
     *
     * @param expression Expression to evaluate
     * @param value Value being validated
     * @param options Valid value options checked
     * @throws IllegalArgumentException if expression is false
     */
    public static void validValue(final boolean expression, final Object value, final String options) {
        checkArgument(expression, "expected one of: %s \n%but was: %s", options, value);
    }

    /**
     * Return value and check whether specified value is null and if so throws exception. This method supports
     * require default getter methods.
     *
     * @param value Value itself
     * @param name Name of the value
     * @return Non-null value
     * @throws NoSuchElementException if value is null
     */
    public static <T> @NonNull T require(final @Nullable T value, final @NonNull String name) {
        if (value == null) {
            throw new NoSuchElementException("Value of " + name + " is not present");
        }
        return value;
    }

    /**
     * A shortcut for {@code Preconditions.checkNotNull(value, "Key component \"%s\" must not be null", name)}.
     *
     * @param value Value itself
     * @param name Name of the value
     * @return Non-null value
     * @throws NullPointerException if value is null
     */
    public static <T> @NonNull T requireKeyProp(final @Nullable T value, final @NonNull String name) {
        if (value == null) {
            throw new NullPointerException("Key component \"" + name + "\" may not be null");
        }
        return value;
    }

    /**
     * A shortcut for {@code Objects.requireNonNull(value, "Supplied value may not be null")}.
     *
     * @param value Value itself
     * @throws NullPointerException if value is null
     */
    public static void requireValue(final @Nullable Object value) {
        requireNonNull(value, "Supplied value may not be null");
    }

    /**
     * Append a {@code bits} individual value. If the value is {@code false}, this method does nothing.
     *
     * @param helper Helper to append to
     * @param name Name of the bit
     * @param value Value to append
     * @throws NullPointerException if any argument is {@code null}
     */
    public static void appendBit(final ToStringHelper helper, final @NonNull String name, final boolean value) {
        if (value) {
            helper.addValue(name);
        }
    }

    /**
     * Append a named value to a ToStringHelper. If the value is null, this method does nothing.
     *
     * @param helper Helper to append to
     * @param name Name of the value
     * @param value Value to append
     * @throws NullPointerException if the name or helper is null
     */
    public static void appendValue(final ToStringHelper helper, final @NonNull String name,
            final @Nullable Object value) {
        if (value != null) {
            helper.add(name, value);
        }
    }

    /**
     * Append a named value to a ToStringHelper. If the value is null, this method does nothing.
     *
     * @param helper Helper to append to
     * @param name Name of the value
     * @param value Value to append
     * @throws NullPointerException if the name or helper is null
     */
    public static void appendValue(final ToStringHelper helper, final String name, final byte[] value) {
        if (value != null) {
            helper.add(name, HexFormat.of().formatHex(value));
        }
    }

    /**
     * Append augmentation map of an Augmentable to a ToStringHelper. If augmentations are null or empt, this method
     * does nothing.
     *
     * @param helper Helper to append to
     * @param name Name of the augmentation value
     * @param augmentable Augmentable object to
     * @throws NullPointerException if any argument is null
     */
    public static void appendAugmentations(final ToStringHelper helper, final String name,
            final Augmentable<?> augmentable) {
        final var augments = augmentable.augmentations();
        if (!augments.isEmpty()) {
            helper.add(name, augments.values());
        }
    }

    /**
     * Compile a list of pattern regular expressions and return them as an array. The list must hold at least two
     * expressions.
     *
     * @param patterns Patterns to compile
     * @return Compiled patterns in an array
     * @throws NullPointerException if the list or any of its elements is null
     * @throws VerifyException if the list has fewer than two elements
     */
    public static Pattern @NonNull[] compilePatterns(final @NonNull List<String> patterns) {
        final int size = patterns.size();
        verify(size > 1, "Patterns has to have at least 2 elements");
        final Pattern[] result = new Pattern[size];
        for (int i = 0; i < size; ++i) {
            result[i] = Pattern.compile(patterns.get(i));
        }
        return result;
    }

    /**
     * Check whether a specified string value matches a specified pattern. This method handles the distinction between
     * modeled XSD expression and enforcement {@link Pattern} which may reflect negation.
     *
     * @param value Value to be checked.
     * @param pattern Enforcement pattern
     * @param regex Source regular expression, as defined in YANG model
     * @throws IllegalArgumentException if the value does not match the pattern
     * @throws NullPointerException if any of the arguments are null
     */
    public static void checkPattern(final String value, final Pattern pattern, final String regex) {
        if (!pattern.matcher(value).matches()) {
            final String match = RegexPatterns.isNegatedPattern(pattern) ? "matches forbidden"
                : "does not match required";
            throw new IllegalArgumentException("Supplied value \"" + value + "\" " + match + " pattern \""
                    + regex + "\"");
        }
    }

    /**
     * Check whether a specified string value matches specified patterns. This method handles the distinction between
     * modeled XSD expression and enforcement {@link Pattern} which may reflect negation.
     *
     * @param value Value to be checked.
     * @param patterns Enforcement patterns
     * @param regexes Source regular expression, as defined in YANG model. Size and order must match patterns.
     * @throws IllegalArgumentException if the value does not match the pattern
     * @throws NullPointerException if any of the arguments are null
     * @throws VerifyException if the size of patterns and regexes does not match
     */
    public static void checkPattern(final String value, final Pattern[] patterns, final String[] regexes) {
        verify(patterns.length == regexes.length, "Patterns and regular expression lengths have to match");
        for (int i = 0; i < patterns.length; ++i) {
            checkPattern(value, patterns[i], regexes[i]);
        }
    }

    /**
     * Throw an IllegalArgument exception describing a length violation.
     *
     * @param expected String describing expected lengths
     * @param actual Actual observed object
     * @throws IllegalArgumentException always
     */
    public static void throwInvalidLength(final String expected, final Object actual) {
        throw new IllegalArgumentException("Invalid length: " + actual + ", expected: " + expected + ".");
    }

    /**
     * Throw an IllegalArgument exception describing a length violation.
     *
     * @param expected String describing expected lengths
     * @param actual Actual observed byte array
     * @throws IllegalArgumentException always
     */
    public static void throwInvalidLength(final String expected, final byte[] actual) {
        throwInvalidLength(expected, HexFormat.of().formatHex(actual));
    }

    /**
     * Throw an IllegalArgument exception describing a range violation.
     *
     * @param expected String describing expected ranges
     * @param actual Actual observed object
     * @throws IllegalArgumentException always
     */
    public static void throwInvalidRange(final String expected, final Object actual) {
        throw new IllegalArgumentException("Invalid range: " + actual + ", expected: " + expected + ".");
    }

    /**
     * Throw an IllegalArgument exception describing a range violation.
     *
     * @param expected String describing expected ranges
     * @param actual Actual observed value
     * @throws IllegalArgumentException always
     */
    public static void throwInvalidRange(final String expected, final int actual) {
        // Not a code duplication: provides faster string concat via StringBuilder.append(int)
        throw new IllegalArgumentException("Invalid range: " + actual + ", expected: " + expected + ".");
    }

    /**
     * Throw an IllegalArgument exception describing a range violation.
     *
     * @param expected String describing expected ranges
     * @param actual Actual observed value
     * @throws IllegalArgumentException always
     */
    public static void throwInvalidRange(final String expected, final long actual) {
        // Not a code duplication: provides faster string concat via StringBuilder.append(long)
        throw new IllegalArgumentException("Invalid range: " + actual + ", expected: " + expected + ".");
    }

    /**
     * Throw an IllegalArgument exception describing a range violation.
     *
     * @param expected Objects describing expected ranges
     * @param actual Actual observed byte array
     * @throws IllegalArgumentException always
     */
    public static void throwInvalidRange(final Object[] expected, final Object actual) {
        throwInvalidRange(Arrays.toString(expected), actual);
    }

    /**
     * Throw an IllegalArgument exception describing a range violation of an Uint64 type.
     *
     * @param expected String describing expected ranges
     * @param actual Actual observed value
     * @throws IllegalArgumentException always
     */
    public static void throwInvalidRangeUnsigned(final String expected, final long actual) {
        throw new IllegalArgumentException("Invalid range: " + Long.toUnsignedString(actual) + ", expected: " + expected
            + ".");
    }

    /**
     * Check whether specified List is null and if so return an immutable list instead. This method supports
     * non-null default getter methods.
     *
     * @param <T> list element type
     * @param input input list, may be null
     * @return Input list or an empty list.
     */
    public static <T> @NonNull List<T> nonnull(final @Nullable List<T> input) {
        return input != null ? input : ImmutableList.of();
    }

    /**
     * Check whether specified Map is null and if so return an immutable map instead. This method supports
     * non-null default getter methods.
     *
     * @param <K> key type
     * @param <V> value type
     * @param input input map, may be null
     * @return Input map or an empty map.
     */
    public static <K, V> @NonNull Map<K, V> nonnull(final @Nullable Map<K, V> input) {
        return input != null ? input : ImmutableMap.of();
    }

    /**
     * Check whether specified List is empty and if so return null, otherwise return input list. This method supports
     * Builder/implementation list handover.
     *
     * @param <T> list element type
     * @param input input list, may be null
     * @return Input list or null.
     */
    public static <T> @Nullable List<T> emptyToNull(final @Nullable List<T> input) {
        return input != null && input.isEmpty() ? null : input;
    }

    /**
     * Check whether specified Map is empty and if so return null, otherwise return input map. This method supports
     * Builder/implementation list handover.
     *
     * @param <K> key type
     * @param <V> value type
     * @param input input map, may be null
     * @return Input map or null.
     */
    public static <K, V> @Nullable Map<K, V> emptyToNull(final @Nullable Map<K, V> input) {
        return input != null && input.isEmpty() ? null : input;
    }

    /**
     * Return hash code of a single-property wrapper class. Since the wrapper is not null, we really want to discern
     * this object being present, hence {@link Objects#hashCode()} is not really useful we would end up with {@code 0}
     * for both non-present and present-with-null objects.
     *
     * @param obj Internal object to hash
     * @return Wrapper object hash code
     */
    public static int wrapperHashCode(final @Nullable Object obj) {
        return wrapHashCode(Objects.hashCode(obj));
    }

    /**
     * Return hash code of a single-property wrapper class. Since the wrapper is not null, we really want to discern
     * this object being present, hence {@link Arrays#hashCode()} is not really useful we would end up with {@code 0}
     * for both non-present and present-with-null objects.
     *
     * @param obj Internal object to hash
     * @return Wrapper object hash code
     */
    public static int wrapperHashCode(final byte @Nullable[] obj) {
        return wrapHashCode(Arrays.hashCode(obj));
    }

    /**
     * The constant '31' is the result of folding this code:
     * <pre>
     *   <code>
     *     final int prime = 31;
     *     int result = 1;
     *     result = result * prime + Objects.hashCode(obj);
     *     return result;
     *   </code>
     * </pre>
     * when hashCode is returned as 0, such as due to obj being null or its hashCode being 0.
     *
     * @param hash Wrapped object hash
     * @return Wrapper object hash
     */
    private static int wrapHashCode(final int hash) {
        return hash == 0 ? 31 : hash;
    }

    /**
     * Check that the specified {@link EnumTypeObject} object is not {@code null}. This method is meant to be used with
     * {@code ofName(String)} and {@code ofValue(int)} static factory methods.
     *
     * @param obj enumeration object, possibly null
     * @param name User-supplied enumeration name
     * @return Enumeration object
     * @throws IllegalArgumentException if {@code obj} is null
     */
    public static <T extends EnumTypeObject> @NonNull T checkEnum(final @Nullable T obj, final String name) {
        if (obj == null) {
            throw new IllegalArgumentException("\"" + name + "\" is not a valid name");
        }
        return obj;
    }

    /**
     * Check that the specified {@link EnumTypeObject} object is not {@code null}. This method is meant to be used with
     * {@code ofName(String)} and {@code ofValue(int)} static factory methods.
     *
     * @param obj enumeration object, possibly null
     * @param value User-supplied enumeration value
     * @return Enumeration object
     * @throws IllegalArgumentException if {@code obj} is null
     */
    public static <T extends EnumTypeObject> @NonNull T checkEnum(final @Nullable T obj, final int value) {
        if (obj == null) {
            throw new IllegalArgumentException(value + " is not a valid value");
        }
        return obj;
    }

    /**
     * Utility method for checking whether a target object is a compatible {@link BindingContract}.
     *
     * @param requiredClass Required BindingContract class
     * @param obj Object to check, may be null
     * @return Object cast to required class, if its implemented class matches requirement, null otherwise
     * @throws NullPointerException if {@code requiredClass} is null
     */
    public static <T extends BindingContract<?>> @Nullable T checkCast(final @NonNull Class<T> requiredClass,
            final @Nullable Object obj) {
        return obj instanceof BindingContract<?> contract && requiredClass.equals(contract.implementedInterface())
            ? requiredClass.cast(obj) : null;
    }

    /**
     * Utility method for checking whether a target object is compatible.
     *
     * @param requiredClass Required class
     * @param fieldName name of the field being filled
     * @param obj Object to check, may be null
     * @return Object cast to required class, if its class matches requirement, or null
     * @throws IllegalArgumentException if {@code obj} is not an instance of {@code requiredClass}
     * @throws NullPointerException if {@code requiredClass} or {@code fieldName} is null
     */
    public static <T> @Nullable T checkFieldCast(final @NonNull Class<T> requiredClass, final @NonNull String fieldName,
            final @Nullable Object obj) {
        try {
            return requiredClass.cast(obj);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Invalid input value for property \"" + fieldName + "\"", e);
        }
    }

    /**
     * Utility method for checking whether the items of target list is compatible.
     *
     * @param requiredClass Required item class
     * @param fieldName name of the field being filled
     * @param list List, which items should be checked
     * @return Type-checked List
     * @throws IllegalArgumentException if a list item is not instance of {@code requiredClass}
     * @throws NullPointerException if {@code requiredClass} or {@code fieldName} is null
     */
    @SuppressWarnings("unchecked")
    public static <T> @Nullable List<T> checkListFieldCast(final @NonNull Class<?> requiredClass,
            final @NonNull String fieldName, final @Nullable List<?> list) {
        checkCollectionField(requiredClass, fieldName, list);
        return (List<T>) list;
    }

    /**
     * Utility method for checking whether the items of target set is compatible.
     *
     * @param requiredClass Required item class
     * @param fieldName name of the field being filled
     * @param set Set, which items should be checked
     * @return Type-checked Set
     * @throws IllegalArgumentException if a set item is not instance of {@code requiredItemClass}
     * @throws NullPointerException if {@code requiredClass} or {@code fieldName} is null
     */
    @SuppressWarnings("unchecked")
    public static <T> @Nullable Set<T> checkSetFieldCast(final @NonNull Class<?> requiredClass,
            final @NonNull String fieldName, final @Nullable Set<?> set) {
        checkCollectionField(requiredClass, fieldName, set);
        return (Set<T>) set;
    }

    @SuppressFBWarnings(value = "DCN_NULLPOINTER_EXCEPTION",
        justification = "Internal NPE->IAE conversion")
    private static void checkCollectionField(final @NonNull Class<?> requiredClass,
            final @NonNull String fieldName, final @Nullable Collection<?> collection) {
        if (collection != null) {
            try {
                collection.forEach(item -> requiredClass.cast(requireNonNull(item)));
            } catch (ClassCastException | NullPointerException e) {
                throw new IllegalArgumentException("Invalid input item for property \"" + requireNonNull(fieldName)
                    + "\"", e);
            }
        }
    }
}
