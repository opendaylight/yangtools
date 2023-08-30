/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.BaseIdentity;
import org.opendaylight.yangtools.binding.BindingContract;
import org.opendaylight.yangtools.binding.BitsTypeObject;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.binding.EnumTypeObject;
import org.opendaylight.yangtools.binding.ScalarTypeObject;
import org.opendaylight.yangtools.binding.UnsafeSecret;
import org.opendaylight.yangtools.binding.contract.RegexPatterns;
import org.opendaylight.yangtools.binding.impl.TheUnsafeSecret;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;

/**
 * Helper methods for generated binding code. This class concentrates useful primitives generated code may call
 * to perform specific shared functions. This allows for generated classes to be leaner. Methods in this class follows
 * general API stability requirements of the Binding Specification.
 */
public final class CodeHelpers {
    /**
     * Compare {@link Augmentation} by their canonical class name.
     */
    private static final Comparator<Augmentation<?>> AUGMENTATION_BY_CANONICAL_NAME =
        Comparator.comparing(aug -> aug.implementedInterface().getCanonicalName());

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
     * Return value and check whether specified value is {@code null} and if so throws exception. This method supports
     * require default getter methods.
     *
     * @param value Value itself
     * @param name Name of the value
     * @return Non-null value
     * @throws NoSuchElementException if value is {@code null}
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
     * @throws NullPointerException if value is {@code null}
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
     * @param <T> Value type
     * @param value Value itself
     * @return Non-null value
     * @throws NullPointerException if value is {@code null}
     */
    public static <T> @NonNull T requireValue(final @Nullable T value) {
        return requireNonNull(value, "Supplied value may not be null");
    }

    /**
     * A shortcut for {@link #requireValue(Object)} combined with {@link #checkScale(Decimal64, int)}.
     *
     * @param value Value itself
     * @param expectedScale the expected scale
     * @return Non-null value
     * @throws NullPointerException if value is {@code null}
     * @throws IllegalArgumentException if the value has unexpected scale
     */
    public static @NonNull Decimal64 requireValue(final @Nullable Decimal64 value, final int expectedScale) {
        final var ret = requireValue(value);
        checkScale(ret, expectedScale);
        return ret;
    }

    /**
     * Compile a list of pattern regular expressions and return them as an array. The list must hold at least two
     * expressions.
     *
     * @param patterns Patterns to compile
     * @return Compiled patterns in an array
     * @throws NullPointerException if the list or any of its elements is {@code null}
     * @throws VerifyException if the list has fewer than two elements
     */
    public static Pattern @NonNull[] compilePatterns(final @NonNull List<String> patterns) {
        final int size = patterns.size();
        verify(size > 1, "Patterns has to have at least 2 elements");
        final var result = new Pattern[size];
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
     * @throws NullPointerException if any of the arguments is {@code null}
     */
    public static void checkPattern(final String value, final Pattern pattern, final String regex) {
        if (!pattern.matcher(value).matches()) {
            final var match = RegexPatterns.isNegatedPattern(pattern) ? "matches forbidden"
                : "does not match required";
            throw new IllegalArgumentException(
                "Supplied value \"" + value + "\" " + match + " pattern \"" + regex + "\"");
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
     * @throws NullPointerException if any of the arguments is {@code null}
     * @throws VerifyException if the size of patterns and regexes does not match
     */
    public static void checkPattern(final String value, final Pattern[] patterns, final String[] regexes) {
        verify(patterns.length == regexes.length, "Patterns and regular expression lengths have to match");
        for (int i = 0; i < patterns.length; ++i) {
            checkPattern(value, patterns[i], regexes[i]);
        }
    }

    /**
     * Check whether a {@link Decimal64} value has the expected scale.
     *
     * @param value value to be checked.
     * @param expectedScale the expected scale
     * @return unscaled value
     * @throws IllegalArgumentException if the value has unexpected scale
     */
    public static long checkScale(final Decimal64 value, final int expectedScale) {
        final var scale = value.scale();
        if (scale != expectedScale) {
            throw new IllegalArgumentException(
                "Invalid " + value + " scale: " + scale + ", expected " + expectedScale + ".");
        }
        return value.unscaledValue();
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
     * Throw an IllegalArgument exception describing a range violation of an Uint64 type.
     *
     * @param expected String describing expected ranges
     * @param actual Actual observed value
     * @throws IllegalArgumentException always
     */
    public static void throwInvalidRangeUnsigned(final String expected, final long actual) {
        throw new IllegalArgumentException(
            "Invalid range: " + Long.toUnsignedString(actual) + ", expected: " + expected + ".");
    }

    /**
     * Check whether specified List is {@code null} and if so return an immutable list instead. This method supports
     * non-null default getter methods.
     *
     * @param <T> list element type
     * @param input input list, may be {@code null}
     * @return Input list or an empty list.
     */
    public static <T> @NonNull List<T> nonnull(final @Nullable List<T> input) {
        return input != null ? input : List.of();
    }

    /**
     * Check whether specified Map is {@code null} and if so return an immutable map instead. This method supports
     * non-null default getter methods.
     *
     * @param <K> key type
     * @param <V> value type
     * @param input input map, may be {@code null}
     * @return Input map or an empty map.
     */
    public static <K, V> @NonNull Map<K, V> nonnull(final @Nullable Map<K, V> input) {
        return input != null ? input : Map.of();
    }

    /**
     * Check whether specified List is empty and if so return {@code null}, otherwise return input list. This method
     * supports Builder/implementation list handover.
     *
     * @param <T> list element type
     * @param input input list, may be {@code null}
     * @return Input list or {@code null}.
     */
    public static <T> @Nullable List<T> emptyToNull(final @Nullable List<T> input) {
        return input != null && input.isEmpty() ? null : input;
    }

    /**
     * Check whether specified Map is empty and if so return {@code null}, otherwise return input map. This method
     * supports Builder/implementation list handover.
     *
     * @param <K> key type
     * @param <V> value type
     * @param input input map, may be {@code null}
     * @return Input map or {@code null}.
     */
    public static <K, V> @Nullable Map<K, V> emptyToNull(final @Nullable Map<K, V> input) {
        return input != null && input.isEmpty() ? null : input;
    }

    /**
     * Return hash code of a single-property wrapper class. Since the wrapper is not {@code null}, we really want to
     * discern this object being present, hence {@link Objects#hashCode()} is not really useful we would end up with
     * {@code 0} for both non-present and present-with-null objects.
     *
     * @param obj Internal object to hash
     * @return Wrapper object hash code
     */
    public static int wrapperHashCode(final @Nullable Object obj) {
        return wrapHashCode(Objects.hashCode(obj));
    }

    /**
     * Return hash code of a single-property wrapper class. Since the wrapper is not {@code null}, we really want to
     * discern this object being present, hence {@link Arrays#hashCode()} is not really useful we would end up with
     * {@code 0} for both non-present and present-with-null objects.
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
     * when hashCode is returned as 0, such as due to obj being {@code null} or its hashCode being 0.
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
     * @param obj enumeration object, possibly {@code null}
     * @param name User-supplied enumeration name
     * @return Enumeration object
     * @throws IllegalArgumentException if {@code obj} is {@code null}
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
     * @param obj enumeration object, possibly {@code null}
     * @param value User-supplied enumeration value
     * @return Enumeration object
     * @throws IllegalArgumentException if {@code obj} is {@code null}
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
     * @param obj Object to check, may be {@code null}
     * @return Object cast to required class, if its implemented class matches requirement, {@code null} otherwise
     * @throws NullPointerException if {@code requiredClass} is {@code null}
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
     * @param obj Object to check, may be {@code null}
     * @return Object cast to required class, if its class matches requirement, or {@code null}
     * @throws IllegalArgumentException if {@code obj} is not an instance of {@code requiredClass}
     * @throws NullPointerException if {@code requiredClass} or {@code fieldName} is {@code null}
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
     * @throws NullPointerException if {@code requiredClass} or {@code fieldName} is {@code null}
     */
    @SuppressWarnings("unchecked")
    public static <T> @Nullable List<T> checkListFieldCast(final @NonNull Class<T> requiredClass,
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
     * @throws IllegalArgumentException if a set item is not instance of {@code requiredClass}
     * @throws NullPointerException if {@code requiredClass} or {@code fieldName} is {@code null}
     */
    @SuppressWarnings("unchecked")
    public static <T> @Nullable Set<T> checkSetFieldCast(final @NonNull Class<T> requiredClass,
            final @NonNull String fieldName, final @Nullable Set<?> set) {
        checkCollectionField(requiredClass, fieldName, set);
        return (Set<T>) set;
    }

    private static void checkCollectionField(final @NonNull Class<?> requiredClass, final @NonNull String fieldName,
            final @Nullable Collection<?> collection) {
        if (collection != null) {
            try {
                collection.forEach(item -> requiredClass.cast(requireNonNull(item)));
            } catch (ClassCastException | NullPointerException e) {
                throw new IllegalArgumentException(
                    "Invalid input item for property \"" + requireNonNull(fieldName) + "\"", e);
            }
        }
    }

    /**
     * Check if the proposed string is empty.
     *
     * @param str the string
     * @return an {@link Empty} instance
     * @throws IllegalArgumentException if {code str} is non-empty
     * @throws NullPointerException if {@code str} is {@code null}
     */
    public static @NonNull Empty emptyFor(final String str) {
        if (str.isEmpty()) {
            return Empty.value();
        }
        throw new IllegalArgumentException("Invalid value " + str);
    }

    /**
     * {@return a clone of input bytes or {@code null}}
     * @param bytes input bytes
     */
    public static byte @Nullable [] copyArray(final byte @Nullable [] bytes) {
        return bytes == null ? null : bytes.clone();
    }

    /**
     * Verify that a provided {@link UnsafeSecret} matches the expected instance.
     *
     * @param secret provided secret
     * @throws LinkageError if a mismatch is detected
     * @since 15.1.0
     */
    @NonNullByDefault
    public static void verifySecret(final UnsafeSecret secret) {
        if (!secret.equals(TheUnsafeSecret.INSTANCE)) {
            throw new LinkageError("UnsafeSecret mismatch: expecting " + TheUnsafeSecret.INSTANCE + ", got " + secret);
        }
    }

    //
    ////
    ////// JavaContract.javaHC support methods
    ////
    //

    /**
     * Reference implementation of {@link JavaContract#javaHC()}.
     *
     * @param hashCodes component hash codes
     * @return the hash code
     * @since 16.0.0
     */
    @NonNullByDefault
    public static int jcHC(final int... hashCodes) {
        return nonzero(sumPropHashCodes(hashCodes));
    }

    /**
     * {@return the equivalent of {@link #jcHC(int...)} for two hash codes}
     * @param hashCode0 first hash code
     * @param hashCode1 second hash code
     * @since 16.0.0
     */
    public static int jcHC(final int hashCode0, final int hashCode1) {
        return nonzero(sumPropHashCodes(hashCode0, hashCode1));
    }

    /**
     * {@return the equivalent of {@link #jcHC(int...)} for three hash codes}
     * @param hashCode0 first hash code
     * @param hashCode1 second hash code
     * @param hashCode2 third hash code
     * @since 16.0.0
     */
    public static int jcHC(final int hashCode0, final int hashCode1, final int hashCode2) {
        return nonzero(sumPropHashCodes(hashCode0, hashCode1, hashCode2));
    }

    /**
     * {@return the equivalent of {@link #jcHC(int...)} for four hash codes}
     * @param hashCode0 first hash code
     * @param hashCode1 second hash code
     * @param hashCode2 third hash code
     * @param hashCode3 fourth hash code
     * @since 16.0.0
     */
    public static int jcHC(final int hashCode0, final int hashCode1, final int hashCode2, final int hashCode3) {
        return nonzero(sumPropHashCodes(hashCode0, hashCode1, hashCode2, hashCode3));
    }

    /**
     * {@return the equivalent of {@link #jcHC(int...)} for five hash codes}
     * @param hashCode0 first hash code
     * @param hashCode1 second hash code
     * @param hashCode2 third hash code
     * @param hashCode3 fourth hash code
     * @param hashCode4 fifth hash code
     * @since 16.0.0
     */
    public static int jcHC(final int hashCode0, final int hashCode1, final int hashCode2, final int hashCode3,
            final int hashCode4) {
        return nonzero(sumPropHashCodes(hashCode0, hashCode1, hashCode2, hashCode3, hashCode4));
    }

    /**
     * {@return the equivalent of {@link #jcHC(int...)} for six hash codes}
     * @param hashCode0 first hash code
     * @param hashCode1 second hash code
     * @param hashCode2 third hash code
     * @param hashCode3 fourth hash code
     * @param hashCode4 fifth hash code
     * @param hashCode5 sixth hash code
     * @since 16.0.0
     */
    public static int jcHC(final int hashCode0, final int hashCode1, final int hashCode2, final int hashCode3,
            final int hashCode4, final int hashCode5) {
        return nonzero(sumPropHashCodes(hashCode0, hashCode1, hashCode2, hashCode3, hashCode4, hashCode5));
    }

    /**
     * {@return the equivalent of {@link #jcHC(int...)} for seven hash codes}
     * @param hashCode0 first hash code
     * @param hashCode1 second hash code
     * @param hashCode2 third hash code
     * @param hashCode3 fourth hash code
     * @param hashCode4 fifth hash code
     * @param hashCode5 sixth hash code
     * @param hashCode6 seventh hash code
     * @since 16.0.0
     */
    public static int jcHC(final int hashCode0, final int hashCode1, final int hashCode2, final int hashCode3,
            final int hashCode4, final int hashCode5, final int hashCode6) {
        return nonzero(sumPropHashCodes(hashCode0, hashCode1, hashCode2, hashCode3, hashCode4, hashCode5, hashCode6));
    }

    /**
     * {@return the equivalent of {@link #jcHC(int...)} for eight hash codes}
     * @param hashCode0 first hash code
     * @param hashCode1 second hash code
     * @param hashCode2 third hash code
     * @param hashCode3 fourth hash code
     * @param hashCode4 fifth hash code
     * @param hashCode5 sixth hash code
     * @param hashCode6 seventh hash code
     * @param hashCode7 eighth hash code
     * @since 16.0.0
     */
    public static int jcHC(final int hashCode0, final int hashCode1, final int hashCode2, final int hashCode3,
            final int hashCode4, final int hashCode5, final int hashCode6, final int hashCode7) {
        return nonzero(sumPropHashCodes(hashCode0, hashCode1, hashCode2, hashCode3, hashCode4, hashCode5, hashCode6,
            hashCode7));
    }

    /**
     * {@return the equivalent of {@link #jcHC(int...)} for nine hash codes}
     * @param hashCode0 first hash code
     * @param hashCode1 second hash code
     * @param hashCode2 third hash code
     * @param hashCode3 fourth hash code
     * @param hashCode4 fifth hash code
     * @param hashCode5 sixth hash code
     * @param hashCode6 seventh hash code
     * @param hashCode7 eighth hash code
     * @param hashCode8 ninth hash code
     * @since 16.0.0
     */
    public static int jcHC(final int hashCode0, final int hashCode1, final int hashCode2, final int hashCode3,
            final int hashCode4, final int hashCode5, final int hashCode6, final int hashCode7, final int hashCode8) {
        return nonzero(sumPropHashCodes(hashCode0, hashCode1, hashCode2, hashCode3, hashCode4, hashCode5, hashCode6,
            hashCode7, hashCode8));
    }

    /**
     * {@return the equivalent of {@link #jcHC(int...)} for ten hash codes}
     * @param hashCode0 first hash code
     * @param hashCode1 second hash code
     * @param hashCode2 third hash code
     * @param hashCode3 fourth hash code
     * @param hashCode4 fifth hash code
     * @param hashCode5 sixth hash code
     * @param hashCode6 seventh hash code
     * @param hashCode7 eighth hash code
     * @param hashCode8 ninth hash code
     * @param hashCode9 tenth hash code
     * @since 16.0.0
     */
    public static int jcHC(final int hashCode0, final int hashCode1, final int hashCode2, final int hashCode3,
            final int hashCode4, final int hashCode5, final int hashCode6, final int hashCode7, final int hashCode8,
            final int hashCode9) {
        return nonzero(sumPropHashCodes(hashCode0, hashCode1, hashCode2, hashCode3, hashCode4, hashCode5, hashCode6,
            hashCode7, hashCode8, hashCode9));
    }

    /**
     * Reference implementation of {@link JavaDataContainer#javaHC()} which is also {@link Augmentable}.
     *
     * @param augmentable the {@link Augmentable} instance
     * @param hashCodes component hash codes
     * @return the hash code
     * @since 16.0.0
     */
    @NonNullByDefault
    public static int jcHC(final Augmentable<?> augmentable, final int... hashCodes) {
        return nonzero(hashAugmentations(augmentable) + sumPropHashCodes(hashCodes));
    }

    /**
     * {@return the equivalent of {@link #jcHC(Augmentable, int...)} for two hash codes}
     * @param hashCode0 first hash code
     * @param hashCode1 second hash code
     * @since 16.0.0
     */
    @NonNullByDefault
    public static int jcHC(final Augmentable<?> augmentable, final int hashCode0, final int hashCode1) {
        return nonzero(hashAugmentations(augmentable) + sumPropHashCodes(hashCode0, hashCode1));
    }

    /**
     * {@return the equivalent of {@link #jcHC(Augmentable, int...)} for three hash codes}
     * @param hashCode0 first hash code
     * @param hashCode1 second hash code
     * @param hashCode2 third hash code
     * @since 16.0.0
     */
    @NonNullByDefault
    public static int jcHC(final Augmentable<?> augmentable, final int hashCode0, final int hashCode1,
            final int hashCode2) {
        return nonzero(hashAugmentations(augmentable) + sumPropHashCodes(hashCode0, hashCode1, hashCode2));
    }

    /**
     * {@return the equivalent of {@link #jcHC(Augmentable, int...)} for four hash codes}
     * @param hashCode0 first hash code
     * @param hashCode1 second hash code
     * @param hashCode2 third hash code
     * @param hashCode3 fourth hash code
     * @since 16.0.0
     */
    @NonNullByDefault
    public static int jcHC(final Augmentable<?> augmentable, final int hashCode0, final int hashCode1,
            final int hashCode2, final int hashCode3) {
        return nonzero(hashAugmentations(augmentable) + sumPropHashCodes(hashCode0, hashCode1, hashCode2, hashCode3));
    }

    /**
     * {@return the equivalent of {@link #jcHC(Augmentable, int...)} for five hash codes}
     * @param hashCode0 first hash code
     * @param hashCode1 second hash code
     * @param hashCode2 third hash code
     * @param hashCode3 fourth hash code
     * @param hashCode4 fifth hash code
     * @since 16.0.0
     */
    @NonNullByDefault
    public static int jcHC(final Augmentable<?> augmentable, final int hashCode0, final int hashCode1,
            final int hashCode2, final int hashCode3, final int hashCode4) {
        return nonzero(hashAugmentations(augmentable)
            + sumPropHashCodes(hashCode0, hashCode1, hashCode2, hashCode3, hashCode4));
    }

    /**
     * {@return the equivalent of {@link #jcHC(Augmentable, int...)} for six hash codes}
     * @param hashCode0 first hash code
     * @param hashCode1 second hash code
     * @param hashCode2 third hash code
     * @param hashCode3 fourth hash code
     * @param hashCode4 fifth hash code
     * @param hashCode5 sixth hash code
     * @since 16.0.0
     */
    @NonNullByDefault
    public static int jcHC(final Augmentable<?> augmentable, final int hashCode0, final int hashCode1,
            final int hashCode2, final int hashCode3, final int hashCode4, final int hashCode5) {
        return nonzero(hashAugmentations(augmentable)
            + sumPropHashCodes(hashCode0, hashCode1, hashCode2, hashCode3, hashCode4, hashCode5));
    }

    /**
     * {@return the equivalent of {@link #jcHC(Augmentable, int...)} for seven hash codes}
     * @param hashCode0 first hash code
     * @param hashCode1 second hash code
     * @param hashCode2 third hash code
     * @param hashCode3 fourth hash code
     * @param hashCode4 fifth hash code
     * @param hashCode5 sixth hash code
     * @param hashCode6 seventh hash code
     * @since 16.0.0
     */
    @NonNullByDefault
    public static int jcHC(final Augmentable<?> augmentable, final int hashCode0, final int hashCode1,
            final int hashCode2, final int hashCode3, final int hashCode4, final int hashCode5, final int hashCode6) {
        return nonzero(hashAugmentations(augmentable)
            + sumPropHashCodes(hashCode0, hashCode1, hashCode2, hashCode3, hashCode4, hashCode5, hashCode6));
    }

    /**
     * {@return the equivalent of {@link #jcHC(Augmentable, int...)} for eight hash codes}
     * @param hashCode0 first hash code
     * @param hashCode1 second hash code
     * @param hashCode2 third hash code
     * @param hashCode3 fourth hash code
     * @param hashCode4 fifth hash code
     * @param hashCode5 sixth hash code
     * @param hashCode6 seventh hash code
     * @param hashCode7 eighth hash code
     * @since 16.0.0
     */
    @NonNullByDefault
    public static int jcHC(final Augmentable<?> augmentable, final int hashCode0, final int hashCode1,
            final int hashCode2, final int hashCode3, final int hashCode4, final int hashCode5, final int hashCode6,
            final int hashCode7) {
        return nonzero(hashAugmentations(augmentable)
            + sumPropHashCodes(hashCode0, hashCode1, hashCode2, hashCode3, hashCode4, hashCode5, hashCode6, hashCode7));
    }

    /**
     * {@return the equivalent of {@link #jcHC(Augmentable, int...)} for nine hash codes}
     * @param hashCode0 first hash code
     * @param hashCode1 second hash code
     * @param hashCode2 third hash code
     * @param hashCode3 fourth hash code
     * @param hashCode4 fifth hash code
     * @param hashCode5 sixth hash code
     * @param hashCode6 seventh hash code
     * @param hashCode7 eighth hash code
     * @param hashCode8 ninth hash code
     * @since 16.0.0
     */
    @NonNullByDefault
    public static int jcHC(final Augmentable<?> augmentable, final int hashCode0, final int hashCode1,
            final int hashCode2, final int hashCode3, final int hashCode4, final int hashCode5, final int hashCode6,
            final int hashCode7, final int hashCode8) {
        return nonzero(hashAugmentations(augmentable)
            + sumPropHashCodes(hashCode0, hashCode1, hashCode2, hashCode3, hashCode4, hashCode5, hashCode6,
                hashCode7, hashCode8));
    }

    /**
     * {@return the equivalent of {@link #jcHC(Augmentable, int...)} for ten hash codes}
     * @param hashCode0 first hash code
     * @param hashCode1 second hash code
     * @param hashCode2 third hash code
     * @param hashCode3 fourth hash code
     * @param hashCode4 fifth hash code
     * @param hashCode5 sixth hash code
     * @param hashCode6 seventh hash code
     * @param hashCode7 eighth hash code
     * @param hashCode8 ninth hash code
     * @param hashCode9 tenth hash code
     * @since 16.0.0
     */
    @NonNullByDefault
    public static int jcHC(final Augmentable<?> augmentable, final int hashCode0, final int hashCode1,
            final int hashCode2, final int hashCode3, final int hashCode4, final int hashCode5, final int hashCode6,
            final int hashCode7, final int hashCode8, final int hashCode9) {
        return nonzero(hashAugmentations(augmentable)
            + sumPropHashCodes(hashCode0, hashCode1, hashCode2, hashCode3, hashCode4, hashCode5, hashCode6,
                hashCode7, hashCode8, hashCode9));
    }

    /**
     * {@return the equivalent of {@code bindingHashCode(augmentable, new int[0])}}
     * @param augmentable the {@link Augmentable} instance
     * @since 16.0.0
     */
    @NonNullByDefault
    public static int jcHC0(final Augmentable<?> augmentable) {
        return nonzero(1 + hashAugmentations(augmentable));
    }

    /**
     * {@return the equivalent of {@code bindingHashCode(Objects.hashCode(prop))}}
     * @param prop single property
     * @since 16.0.0
     */
    @NonNullByDefault
    public static int jcHC1(final @Nullable Object prop) {
        return nonzero(31 + Objects.hashCode(prop));
    }

    /**
     * {@return the equivalent of {@code bindingHashCode(Arrays.hashCode(prop))}}
     * @param prop single property
     * @since 16.0.0
     */
    @NonNullByDefault
    public static int jcHC1(final byte @Nullable [] prop) {
        return nonzero(31 + Arrays.hashCode(prop));
    }

    /**
     * {@return the equivalent of {@code bindingHashCode(augmentable, Objects.hashCode(prop))}}
     * @param prop single property
     * @since 16.0.0
     */
    @NonNullByDefault
    public static int jcHC1(final Augmentable<?> augmentable, final @Nullable Object prop) {
        return nonzero(31 + hashAugmentations(augmentable) + Objects.hashCode(prop));
    }

    /**
     * {@return the equivalent of {@code bindingHashCode(augmentable, Arrays.hashCode(prop))}}
     * @param prop single property
     * @since 16.0.0
     */
    @NonNullByDefault
    public static int jcHC1(final Augmentable<?> augmentable, final byte @Nullable [] prop) {
        return nonzero(31 + hashAugmentations(augmentable) + Arrays.hashCode(prop));
    }

    @NonNullByDefault
    public static int jcHCN(final @Nullable Object... props) {
        return nonzero(hashProperties(props));
    }

    @NonNullByDefault
    public static int jcHCN(final @Nullable Object prop0, final @Nullable Object prop1) {
        return nonzero(sumPropHashCodes(Objects.hashCode(prop0), Objects.hashCode(prop1)));
    }

    @NonNullByDefault
    public static int jcHCN(final byte[] @Nullable... props) {
        return nonzero(hashProperties(props));
    }

    @NonNullByDefault
    public static int jcHCN(final byte @Nullable [] prop0, final byte @Nullable [] prop1) {
        return nonzero(sumPropHashCodes(Arrays.hashCode(prop0), Arrays.hashCode(prop1)));
    }

    @NonNullByDefault
    public static int jcHCN(final Augmentable<?> augmentable, final @Nullable Object... props) {
        return nonzero(hashAugmentations(augmentable) + hashProperties(props));
    }

    @NonNullByDefault
    public static int jcHCN(final Augmentable<?> augmentable, final byte[] @Nullable... props) {
        return nonzero(hashAugmentations(augmentable) + hashProperties(props));
    }

    @NonNullByDefault
    private static int hashAugmentations(final Augmentable<?> augmentable) {
        final var augmentations = augmentable.augmentations();
        return augmentations.isEmpty() ? 0 : hashAugmentations(augmentations.values());
    }

    @NonNullByDefault
    private static int hashAugmentations(final Collection<? extends Augmentation<?>> augmentations) {
        int result = 0;
        for (var augmentation : augmentations) {
            result += augmentation.hashCode();
        }
        return result;
    }

    @NonNullByDefault
    private static int hashProperties(final byte[] @Nullable [] props) {
        int result = 1;
        for (var prop : props) {
            result = 31 * result + Arrays.hashCode(prop);
        }
        return result;
    }

    @NonNullByDefault
    private static int hashProperties(final @Nullable Object[] props) {
        int result = 1;
        for (var prop : props) {
            result = 31 * result + Objects.hashCode(prop);
        }
        return result;
    }

    @NonNullByDefault
    private static int sumPropHashCodes(final int... hashCodes) {
        int result = 1;
        for (var hashCode : hashCodes) {
            result = 31 * result + hashCode;
        }
        return result;
    }

    @NonNullByDefault
    private static int sumPropHashCodes(final int hashCode0, final int hashCode1) {
        return 31 * 31 + 31 * hashCode0 + hashCode1;
    }

    @NonNullByDefault
    private static int sumPropHashCodes(final int hashCode0, final int hashCode1, final int hashCode2) {
        return 31 * 31 * 31 + 31 * 31 * hashCode0 + 31 * hashCode1 + hashCode2;
    }

    @NonNullByDefault
    private static int sumPropHashCodes(final int hashCode0, final int hashCode1, final int hashCode2,
            final int hashCode3) {
        return 31 * 31 * 31 * 31 + 31 * 31 * 31 * hashCode0 + 31 * 31 * hashCode1 + 31 * hashCode2 + hashCode3;
    }

    /**
     * Mask {@code hash == 0} for the purposes of {@link JavaContract#javaHC()}. The value we report when
     * {@code hash == 0} is completely arbitrary.
     *
     * @param hash computed hash value
     * @return {@code hash} if it is not zero, a non-zero integer otherwise
     */
    // as long as all implementations are consistent.has
    private static int nonzero(final int hash) {
        // We pick -1 for two reasons:
        // - easily recognizable value and bit pattern (0xFFFFFFFF)
        // - uses iconst_m1 instead of bipush
        return hash == 0 ? -1 : hash;
    }

    //
    ////
    ////// JavaContract.javaTS support methods
    ////
    //

    /**
     * {@return the {@link JavaDataContainer#javaTS()} for container which cannot have any properties}
     * @param clazz the container class
     * @since 16.0.0
     */
    @NonNullByDefault
    public static String jcTS0(final Class<?> clazz) {
        return clazz.getSimpleName() + "{}";
    }

    /**
     * {@return the {@link JavaDataContainer#javaTS()} for container which cannot have any properties, but is
     * {@link Augmentable}}
     * @param augmentable the augmentable instance
     * @since 16.0.0
     */
    @NonNullByDefault
    public static <T extends Augmentable<T> & DataContainer> String jcTS0(final T augmentable) {
        final var clazz = augmentable.implementedInterface();
        final var augmentations = augmentable.augmentations();
        return augmentations.isEmpty() ? jcTS0(clazz) : jcTSB(clazz, augmentations).build();
    }

    /**
     * {@return the {@link JavaDataContainer#javaTS()} for container which can have one property}
     * @param clazz the container class
     * @param name the property name
     * @param value property value, or {@code null} if absent
     * @since 16.0.0
     */
    @NonNullByDefault
    public static String jcTS1(final Class<?> clazz, final String name, final @Nullable Object value) {
        return value == null ? jcTS0(clazz) : jcTSB(clazz).addProp(name, value).build();
    }

    /**
     * {@return the {@link JavaDataContainer#javaTS()} for container which can have one {@code type binary} property}
     * @param clazz the container class
     * @param name the property name
     * @param value the binary property value, or {@code null} if absent
     * @since 16.0.0
     */
    @NonNullByDefault
    public static String jcTS1(final Class<?> clazz, final String name, final byte @Nullable [] value) {
        return value == null ? jcTS0(clazz) : jcTSB(clazz).addProp(name, value).build();
    }

    /**
     * {@return the {@link JavaDataContainer#javaTS()} for container which can have one property
     * and is {@link Augmentable}}}
     * @param augmentable the augmentable instance
     * @param name the property name
     * @param value the property value, or {@code null} if absent
     * @since 16.0.0
     */
    @NonNullByDefault
    public static <T extends Augmentable<T> & DataContainer> String jcTS1(final T augmentable, final String name,
            final @Nullable Object value) {
        return value == null ? jcTS0(augmentable) : jcTSB(augmentable).addProp(name, value).build();
    }

    /**
     * {@return the {@link JavaDataContainer#javaTS()} for container which can have one {@code type binary} property
     * and is {@link Augmentable}}}
     * @param augmentable the augmentable instance
     * @param name the property name
     * @param value the binary property value, or {@code null} if absent
     * @since 16.0.0
     */
    @NonNullByDefault
    public static <T extends Augmentable<T> & DataContainer> String jcTS1(final T augmentable,  final String name,
            final byte @Nullable [] value) {
        return value == null ? jcTS0(augmentable) : jcTSB(augmentable).addProp(name, value).build();
    }

    /**
     * {@return a {@link JavaTSBuilder} identifying specified class}
     * @param clazz the class to identify as
     * @since 16.0.0
     */
    @NonNullByDefault
    public static JavaTSBuilder jcTSB(final Class<?> clazz) {
        return new JavaTSBuilder(clazz, List.of());
    }

    /**
     * {@return a {@link JavaTSBuilder} identifying specified {@link Augmentable} {@link DataContainer} instance,
     * appending any augmentations present as {@code at the end}}
     * @param augmentable the {@link Augmentable} for which the string is being built.
     * @since 16.0.0
     */
    @NonNullByDefault
    public static <T extends Augmentable<T> & DataContainer> JavaTSBuilder jcTSB(final T augmentable) {
        final var clazz = augmentable.implementedInterface();
        final var augmentations = augmentable.augmentations();
        return augmentations.isEmpty() ? jcTSB(clazz) : jcTSB(clazz, augmentations);
    }

    private static <T extends Augmentable<T> & DataContainer> @NonNull JavaTSBuilder jcTSB(
            final @NonNull Class<?> clazz,
            final @NonNull Map<Class<? extends Augmentation<T>>, @NonNull Augmentation<T>> augmentations) {
        return new JavaTSBuilder(clazz, augmentations.values().stream()
            .sorted(AUGMENTATION_BY_CANONICAL_NAME)
            .collect(Collectors.toUnmodifiableList()));
    }

    //
    ////
    ////// BaseIdentity implementation methods
    ////
    //

    /**
     * Canonical implementation of {@link BaseIdentity#equals(Object)}.
     *
     * @param thisObj this object
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is equal to the reference object
     * @since 16.0.0
     */
    public static boolean biEQ(final @NonNull BaseIdentity thisObj, final @Nullable Object obj) {
        return thisObj == obj || biEQ(thisObj.implementedInterface(), obj);
    }

    private static boolean biEQ(final @NonNull Class<? extends BaseIdentity> iface, final Object obj) {
        return iface.isInstance(obj) && iface.equals(((BaseIdentity) obj).implementedInterface());
    }

    /**
     * {@return the {@link BaseIdentity#toString()} string}
     * @param clazz the identity class
     * @param qname identity's assigned QName
     * @since 16.0.0
     */
    @NonNullByDefault
    public static String biTS(final Class<? extends BaseIdentity> clazz, final QName qname) {
        return clazz.getSimpleName() + "{qname=" + qname.toString() + "}";
    }

    //
    ////
    ////// BitsTypeObject implementation methods
    ////
    //

    /**
     * Check whether a bit is not present.
     *
     * @param bit the bit name
     * @param present the bit value
     * @throws IllegalArgumentException if {@code present} is {@code true}
     * @since 16.0.0
     */
    @NonNullByDefault
    public static void checkBit(final String bit, final boolean present) {
        if (present) {
            throw new IllegalArgumentException("Invalid bit: " + bit);
        }
    }

    /**
     * Parse a {@link BitsTypeObject#stringValue()} string for the purposes of its generated {@code valueOf(String)}
     * method.
     *
     * @param str user-provided value
     * @param bits bit name strings
     * @return bit values corresponding to input {@code bits} array with the matching bit set
     * @throws IllegalArgumentException if {code defaultValue} is not a valid string
     * @throws NullPointerException if any argument is {@code null}
     */
    @NonNullByDefault
    public static boolean[] btoValues(final String str, final ImmutableSet<String> bits) {
        final var ret = new boolean[bits.size()];

        for (int begin = 0, length = str.length(); begin < length; ) {
            final int space = str.indexOf(' ', begin);
            if (space == -1) {
                ret[bitOffset(bits, str, begin, length)] = true;
                break;
            }
            if (space != begin) {
                ret[bitOffset(bits, str, begin, space)] = true;
            }
            begin = space + 1;
        }

        return ret;
    }

    @NonNullByDefault
    private static int bitOffset(final ImmutableSet<String> bits, final String str, final int begin, final int end) {
        int offset = 0;
        final var bitStr = str.substring(begin, end);
        if (Unqualified.tryLocalName(bitStr) == null) {
            throw new IllegalArgumentException('"' + bitStr + "\" is not a valid bit name");
        }
        for (var bit : bits) {
            if (bitStr.equals(bit)) {
                return offset;
            }
            offset++;
        }
        throw new IllegalArgumentException(bitStr + " is not one of " + bits);
    }

    /**
     * {@return an empty BitsCSBuilder}
     */
    public static BitsSVBuilder btoSVB() {
        return BitsSVBuilder.Empty.INSTANCE;
    }

    //
    ////
    ////// ScalarTypeObject implementation methods
    ////
    //

    /**
     * {@return the {@link ScalarTypeObject#toString()} string}
     * @param clazz type object class
     * @param value the value
     * @since 16.0.0
     */
    // TODO: Class<? extends ScalarTypeObject<?>> and non-null when binding-codegen knows it deals with a STO
    @NonNullByDefault
    public static String stoTS(final Class<?> clazz, final @Nullable Object value) {
        return jcTS1(clazz, "value", value);
    }

    /**
     * {@return the {@link ScalarTypeObject#toString()} string}
     * @param clazz type object class
     * @param value the value
     * @since 16.0.0
     */
    // TODO: Class<? extends ScalarTypeObject<?>> and non-null when binding-codegen knows it deals with a STO
    @NonNullByDefault
    public static String stoTS(final Class<?> clazz, final byte @Nullable [] value) {
        return jcTS1(clazz, "value", value);
    }
}
