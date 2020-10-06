/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.annotations.Beta;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class EffectiveStmtUtils {
    // FIXME: this should reside somewhere in max_elements
    private static final String UNBOUNDED_STR = "unbounded";

    private EffectiveStmtUtils() {
        // Hidden on purpose
    }

    public static SourceException createNameCollisionSourceException(final StmtContext<?, ?, ?> ctx,
            final EffectiveStatement<?, ?> effectiveStatement) {
        return new SourceException(ctx.getStatementSourceReference(),
            "Error in module '%s': cannot add '%s'. Node name collision: '%s' already declared.",
            ctx.getRoot().rawStatementArgument(), effectiveStatement.argument(), effectiveStatement.argument());
    }

    public static Optional<ElementCountConstraint> createElementCountConstraint(final EffectiveStatement<?, ?> stmt) {
        return createElementCountConstraint(
            stmt.findFirstEffectiveSubstatementArgument(MinElementsEffectiveStatement.class).orElse(null),
            stmt.findFirstEffectiveSubstatementArgument(MaxElementsEffectiveStatement.class).orElse(null));
    }

    public static Optional<ElementCountConstraint> createElementCountConstraint(
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return createElementCountConstraint(
            BaseQNameStatementSupport.findFirstArgument(substatements, MinElementsEffectiveStatement.class, null),
            BaseQNameStatementSupport.findFirstArgument(substatements, MaxElementsEffectiveStatement.class, null));
    }

    private static Optional<ElementCountConstraint> createElementCountConstraint(
            final @Nullable Integer min, final @Nullable String max) {
        final Integer minElements;
        if (min != null) {
            minElements = min > 0 ? min : null;
        } else {
            minElements = null;
        }

        final Integer maxElements;
        if (max != null && !UNBOUNDED_STR.equals(max)) {
            final Integer m = Integer.valueOf(max);
            maxElements = m < Integer.MAX_VALUE ? m : null;
        } else {
            maxElements = null;
        }

        return ElementCountConstraint.forNullable(minElements, maxElements);
    }

    /**
     * Checks whether supplied type has any of specified default values marked
     * with an if-feature. This method creates mutable copy of supplied set of
     * default values.
     *
     * @param yangVersion
     *            yang version
     * @param typeStmt
     *            type statement which should be checked
     * @param defaultValues
     *            set of default values which should be checked. The method
     *            creates mutable copy of this set
     *
     * @return true if any of specified default values is marked with an
     *         if-feature, otherwise false
     */
    public static boolean hasDefaultValueMarkedWithIfFeature(final YangVersion yangVersion,
            final TypeEffectiveStatement<?> typeStmt, final Set<String> defaultValues) {
        return !defaultValues.isEmpty() && yangVersion == YangVersion.VERSION_1_1
                && isRelevantForIfFeatureCheck(typeStmt)
                && isAnyDefaultValueMarkedWithIfFeature(typeStmt, new HashSet<>(defaultValues));
    }

    /**
     * Checks whether supplied type has specified default value marked with an
     * if-feature. This method creates mutable set of supplied default value.
     *
     * @param yangVersion
     *            yang version
     * @param typeStmt
     *            type statement which should be checked
     * @param defaultValue
     *            default value to be checked
     *
     * @return true if specified default value is marked with an if-feature,
     *         otherwise false
     */
    public static boolean hasDefaultValueMarkedWithIfFeature(final YangVersion yangVersion,
            final TypeEffectiveStatement<?> typeStmt, final String defaultValue) {
        final HashSet<String> defaultValues = new HashSet<>();
        defaultValues.add(defaultValue);
        return !Strings.isNullOrEmpty(defaultValue) && yangVersion == YangVersion.VERSION_1_1
                && isRelevantForIfFeatureCheck(typeStmt)
                && isAnyDefaultValueMarkedWithIfFeature(typeStmt, defaultValues);
    }

    private static boolean isRelevantForIfFeatureCheck(final TypeEffectiveStatement<?> typeStmt) {
        final TypeDefinition<?> typeDefinition = typeStmt.getTypeDefinition();
        return typeDefinition instanceof EnumTypeDefinition || typeDefinition instanceof BitsTypeDefinition
                || typeDefinition instanceof UnionTypeDefinition;
    }

    private static boolean isAnyDefaultValueMarkedWithIfFeature(final TypeEffectiveStatement<?> typeStmt,
            final Set<String> defaultValues) {
        final Iterator<? extends EffectiveStatement<?, ?>> iter = typeStmt.effectiveSubstatements().iterator();
        while (iter.hasNext() && !defaultValues.isEmpty()) {
            final EffectiveStatement<?, ?> effectiveSubstatement = iter.next();
            if (YangStmtMapping.BIT.equals(effectiveSubstatement.statementDefinition())) {
                final String bitName = (String) effectiveSubstatement.argument();
                if (defaultValues.remove(bitName) && containsIfFeature(effectiveSubstatement)) {
                    return true;
                }
            } else if (YangStmtMapping.ENUM.equals(effectiveSubstatement.statementDefinition())
                    && defaultValues.remove(effectiveSubstatement.argument())
                    && containsIfFeature(effectiveSubstatement)) {
                return true;
            } else if (effectiveSubstatement instanceof TypeEffectiveStatement && isAnyDefaultValueMarkedWithIfFeature(
                    (TypeEffectiveStatement<?>) effectiveSubstatement, defaultValues)) {
                return true;
            }
        }

        return false;
    }

    private static boolean containsIfFeature(final EffectiveStatement<?, ?> effectiveStatement) {
        for (final EffectiveStatement<?, ?> effectiveSubstatement : effectiveStatement.effectiveSubstatements()) {
            if (YangStmtMapping.IF_FEATURE.equals(effectiveSubstatement.statementDefinition())) {
                return true;
            }
        }
        return false;
    }

    public static void checkUniqueGroupings(final StmtContext<?, ?, ?> ctx,
            final Collection<? extends EffectiveStatement<?, ?>> statements) {
        checkUniqueNodes(ctx, statements, GroupingDefinition.class);
    }

    public static void checkUniqueTypedefs(final StmtContext<?, ?, ?> ctx,
            final Collection<? extends EffectiveStatement<?, ?>> statements) {
        final Set<Object> typedefs = new HashSet<>();
        for (EffectiveStatement<?, ?> stmt : statements) {
            if (stmt instanceof TypedefEffectiveStatement
                    && !typedefs.add(((TypedefEffectiveStatement) stmt).getTypeDefinition())) {
                throw EffectiveStmtUtils.createNameCollisionSourceException(ctx, stmt);
            }
        }
    }

    public static void checkUniqueUses(final StmtContext<?, ?, ?> ctx,
            final Collection<? extends EffectiveStatement<?, ?>> statements) {
        checkUniqueNodes(ctx, statements, UsesNode.class);
    }

    private static void checkUniqueNodes(final StmtContext<?, ?, ?> ctx,
            final Collection<? extends EffectiveStatement<?, ?>> statements, final Class<?> type) {
        final Set<Object> nodes = new HashSet<>();
        for (EffectiveStatement<?, ?> stmt : statements) {
            if (type.isInstance(stmt) && !nodes.add(stmt)) {
                throw EffectiveStmtUtils.createNameCollisionSourceException(ctx, stmt);
            }
        }
    }
}
