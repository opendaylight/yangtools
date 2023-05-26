/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator;

import com.google.common.annotations.Beta;
import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeRestrictedTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;

/**
 * Contains the methods for converting strings to valid JAVA language strings
 * (package names, class names, attribute names) and to valid javadoc comments.
 */
@Beta
public final class BindingGeneratorUtil {
    /**
     * Pre-compiled replacement pattern.
     */
    private static final CharMatcher GT_MATCHER = CharMatcher.is('>');
    private static final CharMatcher LT_MATCHER = CharMatcher.is('<');
    private static final Pattern UNICODE_CHAR_PATTERN = Pattern.compile("\\\\+u");

    private BindingGeneratorUtil() {
        // Hidden on purpose
    }

    public static Restrictions getRestrictions(final TypeDefinition<?> type) {
        // Old parser generated types which actually contained based restrictions, but our code deals with that when
        // binding to core Java types. Hence we'll emit empty restrictions for base types.
        if (type == null || type.getBaseType() == null) {
            // Handling of decimal64 has changed in the new parser. It contains range restrictions applied to the type
            // directly, without an extended type. We need to capture such constraints. In order to retain behavior we
            // need to analyze the new semantics and see if the constraints have been overridden. To do that we
            // instantiate a temporary unconstrained type and compare them.
            //
            // FIXME: looking at the generated code it looks as though we need to pass the restrictions without
            //        comparison
            if (type instanceof DecimalTypeDefinition decimal) {
                final var tmp = BaseTypes.decimalTypeBuilder(decimal.getQName())
                    .setFractionDigits(decimal.getFractionDigits())
                    .build();

                if (!tmp.getRangeConstraint().equals(decimal.getRangeConstraint())) {
                    return Restrictions.of(decimal.getRangeConstraint().orElse(null));
                }
            }

            return Restrictions.empty();
        }


        /*
         * Take care of extended types.
         *
         * Other types which support constraints are check afterwards. There is a slight twist with them, as returned
         * constraints are the effective view, e.g. they are inherited from base type. Since the constraint is already
         * enforced by the base type, we want to skip them and not perform duplicate checks.
         *
         * We end up emitting ConcreteType instances for YANG base types, which leads to their constraints not being
         * enforced (most notably decimal64). Therefore we need to make sure we do not strip the next-to-last
         * restrictions.
         *
         * FIXME: this probably not the best solution and needs further analysis.
         */
        if (type instanceof BinaryTypeDefinition binary) {
            final var base = binary.getBaseType();
            final Optional<LengthConstraint> length;
            if (base != null && base.getBaseType() != null) {
                length = currentOrEmpty(binary.getLengthConstraint(), base.getLengthConstraint());
            } else {
                length = binary.getLengthConstraint();
            }
            return Restrictions.of(length.orElse(null));
        } else if (type instanceof DecimalTypeDefinition decimal) {
            final var base = decimal.getBaseType();
            final Optional<? extends RangeConstraint<?>> range;
            if (base != null && base.getBaseType() != null) {
                range = currentOrEmpty(decimal.getRangeConstraint(), base.getRangeConstraint());
            } else {
                range = decimal.getRangeConstraint();
            }
            return Restrictions.of(range.orElse(null));
        } else if (type instanceof RangeRestrictedTypeDefinition) {
            // Integer-like types
            return Restrictions.of(extractRangeConstraint((RangeRestrictedTypeDefinition<?, ?>) type).orElse(null));
        } else if (type instanceof StringTypeDefinition string) {
            final var base = string.getBaseType();
            final Optional<LengthConstraint> length;
            if (base != null && base.getBaseType() != null) {
                length = currentOrEmpty(string.getLengthConstraint(), base.getLengthConstraint());
            } else {
                length = string.getLengthConstraint();
            }
            return Restrictions.of(uniquePatterns(string), length.orElse(null));
        } else {
            return Restrictions.empty();
        }
    }

    /*
     * We don't want to include redundant range constraints in Restrictions we emit.
     *
     * Range constraints are inherited from base type and range statement is not mandatory or can contain same
     * range constraints as base type. In these cases range constraints are same as in base type, and we don't want
     * to perform duplicate checks.
     *
     * If range constraints are the same as in base type we emit empty Restrictions. We can do it like this since
     * range constraints can only be same or stricter than in base type. And in case range constraints are the same,
     * we already enforce them in base type.
    */
    private static <T extends RangeRestrictedTypeDefinition<?, ?>> Optional<? extends RangeConstraint<?>>
            extractRangeConstraint(final T def) {
        final T base = (T) def.getBaseType();
        if (base != null) {
            final var defConstrains = def.getRangeConstraint().orElse(null);
            final var baseConstrains = base.getRangeConstraint().orElse(null);
            if (defConstrains != null && baseConstrains != null) {
                return defConstrains.getAllowedRanges().equals(baseConstrains.getAllowedRanges())
                        ? Optional.empty() : def.getRangeConstraint();
            }
        }
        return def.getRangeConstraint();
    }

    private static <T> Optional<T> currentOrEmpty(final Optional<T> current, final Optional<?> base) {
        return current.equals(base) ? Optional.empty() : current;
    }

    private static boolean containsConstraint(final StringTypeDefinition type, final PatternConstraint constraint) {
        for (var wlk = type; wlk != null; wlk = wlk.getBaseType()) {
            if (wlk.getPatternConstraints().contains(constraint)) {
                return true;
            }
        }

        return false;
    }

    private static List<PatternConstraint> uniquePatterns(final StringTypeDefinition type) {
        final var constraints = type.getPatternConstraints();
        if (constraints.isEmpty()) {
            return constraints;
        }

        final var builder = ImmutableList.<PatternConstraint>builder();
        boolean filtered = false;
        for (final PatternConstraint c : constraints) {
            if (containsConstraint(type.getBaseType(), c)) {
                filtered = true;
            } else {
                builder.add(c);
            }
        }

        return filtered ? builder.build() : constraints;
    }

    /**
     * Encodes angle brackets in yang statement description.
     *
     * @param description description of a yang statement which is used to generate javadoc comments
     * @return string with encoded angle brackets
     */
    public static String encodeAngleBrackets(String description) {
        if (description != null) {
            description = LT_MATCHER.replaceFrom(description, "&lt;");
            description = GT_MATCHER.replaceFrom(description, "&gt;");
        }
        return description;
    }

    /**
     * Escape potential unicode references so that the resulting string is safe to put into a {@code .java} file. This
     * processing is required to ensure this text we want to append does not end up with eligible backslashes. See
     * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.3">Java Language Specification</a>
     * for more information.
     *
     * @param str Input string
     * @return A string with all backslashes made ineligible
     */
    public static String replaceAllIllegalChars(final String str) {
        final int backslash = str.indexOf('\\');
        return backslash == -1 ? str : defangUnicodeEscapes(str);
    }

    private static String defangUnicodeEscapes(final String str) {
        // TODO: we should be able to receive the first offset from the non-deprecated method and perform a manual
        //       check for eligibility and escape -- that would be faster I think.
        final var ret = UNICODE_CHAR_PATTERN.matcher(str).replaceAll("\\\\\\\\u");
        return ret.isEmpty() ? "" : ret;
    }
}
