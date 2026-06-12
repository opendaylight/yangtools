/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.TypeDefinitionCompat;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRanges;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeRestrictedTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;

// FIXME: MDSAL-85: specialize for supported types
@Beta
public final class Restrictions {
    private static final @NonNull Restrictions EMPTY = new Restrictions(null, null, ImmutableList.of());

    private final LengthConstraint lengthConstraint;
    // FIXME: MDSAL-85: this is applicable only to int/uint/decimal types and it needs to be captured in the type
    //                  itself
    private final RangeConstraint<?> rangeConstraint;
    private final ImmutableList<PatternConstraint> patternConstraints;

    private Restrictions(final LengthConstraint lengthConstraint, final RangeConstraint<?> rangeConstraint,
            final List<PatternConstraint> patternConstraints) {
        this.lengthConstraint = lengthConstraint;
        this.rangeConstraint = rangeConstraint;
        this.patternConstraints = ImmutableList.copyOf(patternConstraints);
    }

    public static @NonNull Restrictions empty() {
        return EMPTY;
    }

    public static @NonNull Restrictions of(final @Nullable LengthConstraint lengthConstraint) {
        return lengthConstraint == null ? EMPTY : new Restrictions(lengthConstraint, null, ImmutableList.of());
    }

    public static @NonNull Restrictions of(final @Nullable RangeConstraint<?> rangeConstraint) {
        return rangeConstraint == null ? EMPTY : new Restrictions(null, rangeConstraint, ImmutableList.of());
    }

    public static @NonNull Restrictions of(final List<PatternConstraint> patternConstraints,
            final @Nullable LengthConstraint lengthConstraint) {
        return patternConstraints.isEmpty() && lengthConstraint == null ? EMPTY
            : new Restrictions(lengthConstraint, null, patternConstraints);
    }

    @NonNullByDefault
    public static Restrictions of(final TypeDefinition<?> type) {
        // Old parser generated types which actually contained based restrictions, but our code deals with that when
        // binding to core Java types. Hence we'll emit empty restrictions for base types.
        if (type.getBaseType() == null) {
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

            return EMPTY;
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
        return switch (type) {
            case BinaryTypeDefinition binary -> {
                final var base = binary.getBaseType();
                final var length = base != null && base.getBaseType() != null
                    ? currentOrEmpty(binary.getLengthConstraint(), base.getLengthConstraint())
                    : binary.getLengthConstraint();
                yield Restrictions.of(length.orElse(null));
            }
            case DecimalTypeDefinition decimal -> {
                final var base = decimal.getBaseType();
                final var range = base != null && base.getBaseType() != null
                    ? currentOrEmpty(decimal.getRangeConstraint(), base.getRangeConstraint())
                    : decimal.getRangeConstraint();
                yield Restrictions.of(range.orElse(null));
            }
            case RangeRestrictedTypeDefinition<?, ?> range ->
                // Integer-like types
                Restrictions.of(extractRangeConstraint(range).orElse(null));
            case StringTypeDefinition string -> {
                final var base = string.getBaseType();
                final var length = base != null && base.getBaseType() != null
                    ? currentOrEmpty(string.getLengthConstraint(), base.getLengthConstraint())
                    : string.getLengthConstraint();
                yield Restrictions.of(uniquePatterns(string), length.orElse(null));
            }
            default -> EMPTY;
        };
    }

    @Beta
    public static @Nullable Restrictions compute(final @NonNull TypeDefinitionCompat<?, ?> definingStatement,
            final @NonNull TypeEffectiveStatement type) {
        final var length = type.findFirstEffectiveSubstatementArgument(LengthEffectiveStatement.class)
            .map(ValueRanges::asList)
            .orElse(List.of());
        final var range = type.findFirstEffectiveSubstatementArgument(RangeEffectiveStatement.class)
            .map(ValueRanges::asList)
            .orElse(List.of());
        final var patterns = type.streamEffectiveSubstatements(PatternEffectiveStatement.class)
            .map(PatternEffectiveStatement::argument)
            .collect(Collectors.toUnmodifiableList());

        return length.isEmpty() && range.isEmpty() && patterns.isEmpty() ? null
            : of(definingStatement.typeDefinition());
    }

    public Optional<LengthConstraint> getLengthConstraint() {
        return Optional.ofNullable(lengthConstraint);
    }

    public List<PatternConstraint> getPatternConstraints() {
        return patternConstraints;
    }

    public Optional<RangeConstraint<?>> getRangeConstraint() {
        return Optional.ofNullable(rangeConstraint);
    }

    public boolean isEmpty() {
        return lengthConstraint == null && rangeConstraint == null && patternConstraints.isEmpty();
    }

    private static <T> Optional<T> currentOrEmpty(final Optional<T> current, final Optional<?> base) {
        return current.equals(base) ? Optional.empty() : current;
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
            extractRangeConstraint(final @NonNull T def) {
        @SuppressWarnings("unchecked")
        final var base = (T) def.getBaseType();
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

    private static List<PatternConstraint> uniquePatterns(final StringTypeDefinition type) {
        final var constraints = type.getPatternConstraints();
        if (constraints.isEmpty()) {
            return constraints;
        }

        final var builder = ImmutableList.<PatternConstraint>builder();
        boolean filtered = false;
        for (var constraint : constraints) {
            if (containsConstraint(type.getBaseType(), constraint)) {
                filtered = true;
            } else {
                builder.add(constraint);
            }
        }

        return filtered ? builder.build() : constraints;
    }

    private static boolean containsConstraint(final StringTypeDefinition type, final PatternConstraint constraint) {
        for (var wlk = type; wlk != null; wlk = wlk.getBaseType()) {
            if (wlk.getPatternConstraints().contains(constraint)) {
                return true;
            }
        }
        return false;
    }
}
