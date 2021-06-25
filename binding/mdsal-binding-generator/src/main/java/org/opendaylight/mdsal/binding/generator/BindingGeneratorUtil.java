/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator;

import com.google.common.annotations.Beta;
import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeRestrictedTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.ri.type.DecimalTypeBuilder;

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

    private static final Restrictions EMPTY_RESTRICTIONS = new Restrictions() {
        @Override
        public Optional<LengthConstraint> getLengthConstraint() {
            return Optional.empty();
        }

        @Override
        public List<PatternConstraint> getPatternConstraints() {
            return Collections.emptyList();
        }

        @Override
        public Optional<RangeConstraint<?>> getRangeConstraint() {
            return Optional.empty();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    };

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
            if (type instanceof DecimalTypeDefinition) {
                final DecimalTypeDefinition decimal = (DecimalTypeDefinition) type;
                final DecimalTypeBuilder tmpBuilder = BaseTypes.decimalTypeBuilder(decimal.getQName());
                tmpBuilder.setFractionDigits(decimal.getFractionDigits());
                final DecimalTypeDefinition tmp = tmpBuilder.build();

                if (!tmp.getRangeConstraint().equals(decimal.getRangeConstraint())) {
                    return new Restrictions() {
                        @Override
                        public boolean isEmpty() {
                            return false;
                        }

                        @Override
                        public Optional<? extends RangeConstraint<?>> getRangeConstraint() {
                            return decimal.getRangeConstraint();
                        }

                        @Override
                        public List<PatternConstraint> getPatternConstraints() {
                            return ImmutableList.of();
                        }

                        @Override
                        public Optional<LengthConstraint> getLengthConstraint() {
                            return Optional.empty();
                        }
                    };
                }
            }

            return EMPTY_RESTRICTIONS;
        }

        final Optional<LengthConstraint> length;
        final List<PatternConstraint> pattern;
        final Optional<? extends RangeConstraint<?>> range;

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
        if (type instanceof BinaryTypeDefinition) {
            final BinaryTypeDefinition binary = (BinaryTypeDefinition)type;
            final BinaryTypeDefinition base = binary.getBaseType();
            if (base != null && base.getBaseType() != null) {
                length = currentOrEmpty(binary.getLengthConstraint(), base.getLengthConstraint());
            } else {
                length = binary.getLengthConstraint();
            }

            pattern = ImmutableList.of();
            range = Optional.empty();
        } else if (type instanceof DecimalTypeDefinition) {
            length = Optional.empty();
            pattern = ImmutableList.of();

            final DecimalTypeDefinition decimal = (DecimalTypeDefinition)type;
            final DecimalTypeDefinition base = decimal.getBaseType();
            if (base != null && base.getBaseType() != null) {
                range = currentOrEmpty(decimal.getRangeConstraint(), base.getRangeConstraint());
            } else {
                range = decimal.getRangeConstraint();
            }
        } else if (type instanceof RangeRestrictedTypeDefinition) {
            // Integer-like types
            length = Optional.empty();
            pattern = ImmutableList.of();
            range = extractRangeConstraint((RangeRestrictedTypeDefinition<?, ?>)type);
        } else if (type instanceof StringTypeDefinition) {
            final StringTypeDefinition string = (StringTypeDefinition)type;
            final StringTypeDefinition base = string.getBaseType();
            if (base != null && base.getBaseType() != null) {
                length = currentOrEmpty(string.getLengthConstraint(), base.getLengthConstraint());
            } else {
                length = string.getLengthConstraint();
            }

            pattern = uniquePatterns(string);
            range = Optional.empty();
        } else {
            length = Optional.empty();
            pattern = ImmutableList.of();
            range = Optional.empty();
        }

        // Now, this may have ended up being empty, too...
        if (!length.isPresent() && pattern.isEmpty() && !range.isPresent()) {
            return EMPTY_RESTRICTIONS;
        }

        // Nope, not empty allocate a holder
        return new Restrictions() {
            @Override
            public Optional<? extends RangeConstraint<?>> getRangeConstraint() {
                return range;
            }

            @Override
            public List<PatternConstraint> getPatternConstraints() {
                return pattern;
            }

            @Override
            public Optional<LengthConstraint> getLengthConstraint() {
                return length;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }
        };
    }

    private static <T extends RangeRestrictedTypeDefinition<?, ?>> Optional<? extends RangeConstraint<?>>
            extractRangeConstraint(final T def) {
        final T base = (T) def.getBaseType();
        if (base != null && base.getBaseType() != null) {
            return currentOrEmpty(def.getRangeConstraint(), base.getRangeConstraint());
        }

        return def.getRangeConstraint();
    }

    private static <T extends Optional<?>> T currentOrEmpty(final T current, final T base) {
        return current.equals(base) ? (T)Optional.empty() : current;
    }

    private static boolean containsConstraint(final StringTypeDefinition type, final PatternConstraint constraint) {
        for (StringTypeDefinition wlk = type; wlk != null; wlk = wlk.getBaseType()) {
            if (wlk.getPatternConstraints().contains(constraint)) {
                return true;
            }
        }

        return false;
    }

    private static List<PatternConstraint> uniquePatterns(final StringTypeDefinition type) {
        final List<PatternConstraint> constraints = type.getPatternConstraints();
        if (constraints.isEmpty()) {
            return constraints;
        }

        final Builder<PatternConstraint> builder = ImmutableList.builder();
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
        final String ret = UNICODE_CHAR_PATTERN.matcher(str).replaceAll("\\\\\\\\u");
        return ret.isEmpty() ? "" : ret;
    }
}
