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

import com.google.common.base.VerifyException;
import java.util.List;
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
     * Require an argument being received. This is similar to {@link java.util.Objects#requireNonNull(Object)}, but
     * throws an IllegalArgumentException.
     *
     * <p>
     * Implementation note: we expect argName to be a string literal or a constant, so that it's non-nullness can be
     *                      quickly discovered for a call site (where we are going to be inlined).
     *
     * @param value Value itself
     * @param name Symbolic name
     * @return non-null value
     * @throws IllegalArgumentException if value is null
     * @throws NullPointerException if name is null
     */
    // FIXME: another advantage is that it is JDT-annotated, but we could live without that. At some point we should
    //        schedule a big ISE-to-NPE conversion and just use Objects.requireNonNull() instead.
    public static <T> @NonNull T nonNullValue(@Nullable final T value, final @NonNull String name) {
        requireNonNull(name);
        checkArgument(value != null, "%s must not be null", name);
        return value;
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
    public static @NonNull Pattern[] compilePatterns(final @NonNull List<String> patterns) {
        final int size = patterns.size();
        verify(size > 1, "Patterns has to have at least 2 elements");
        final @NonNull Pattern[] result = new Pattern[size];
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
            final String match = BindingMapping.isNegatedPattern(pattern) ? "matches forbidden"
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
}
