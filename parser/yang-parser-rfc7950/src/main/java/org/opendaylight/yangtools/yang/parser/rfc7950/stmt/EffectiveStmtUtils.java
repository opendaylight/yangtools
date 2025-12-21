/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.CopyableNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class EffectiveStmtUtils {
    // FIXME: this should reside somewhere in max_elements
    private static final String UNBOUNDED_STR = "unbounded";

    private EffectiveStmtUtils() {
        // Hidden on purpose
    }

    public static SourceException createNameCollisionSourceException(final EffectiveStmtCtx.Current<?, ?> stmt,
            final EffectiveStatement<?, ?> effectiveStatement) {
        return new SourceException(stmt,
            "Error in module '%s': cannot add '%s'. Node name collision: '%s' already declared.",
            stmt.moduleName().getLocalName(), effectiveStatement.argument(), effectiveStatement.argument());
    }

    @NonNullByDefault
    public static @Nullable ElementCountConstraint createElementCountConstraint(final CommonStmtCtx ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return createElementCountConstraint(ctx,
            AbstractStatementSupport.findFirstStatement(substatements, MinElementsEffectiveStatement.class),
            AbstractStatementSupport.findFirstStatement(substatements, MaxElementsEffectiveStatement.class));
    }

    private static @Nullable ElementCountConstraint createElementCountConstraint(final @NonNull CommonStmtCtx ctx,
            final @Nullable MinElementsEffectiveStatement minStmt,
            final @Nullable MaxElementsEffectiveStatement maxStmt) {
        final Integer minElements;
        if (minStmt != null) {
            final var arg = minStmt.argument();
            minElements = arg > 0 ? arg : null;
        } else {
            minElements = null;
        }

        final Integer maxElements;
        if (maxStmt != null) {
            final var max = maxStmt.argument().asSaturatedInt();
            maxElements = max < Integer.MAX_VALUE ? max : null;
        } else {
            maxElements = null;
        }

        if (minElements == null) {
            return maxElements == null ? null : ElementCountConstraint.atMost(maxElements);
        }
        if (maxElements == null) {
            return ElementCountConstraint.atLeast(minElements);
        }
        if (minElements <= maxElements) {
            return ElementCountConstraint.inRange(minElements, maxElements);
        }
        throw new SourceException(ctx, "Conflicting 'min-elements %s' and 'max-elements %s'",
            minStmt.argument(), maxStmt.argument());
    }

    /**
     * Checks whether supplied type has any of specified default values marked
     * with an if-feature. This method creates mutable copy of supplied set of
     * default values.
     *
     * @param yangVersion YANG version
     * @param typeStmt type statement which should be checked
     * @param defaultValues set of default values which should be checked. The method creates mutable copy of this set
     *
     * @return true if any of specified default values is marked with an if-feature, otherwise false
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
        final var defaultValues = new HashSet<String>();
        defaultValues.add(defaultValue);
        return !Strings.isNullOrEmpty(defaultValue) && yangVersion == YangVersion.VERSION_1_1
            && isRelevantForIfFeatureCheck(typeStmt) && isAnyDefaultValueMarkedWithIfFeature(typeStmt, defaultValues);
    }

    private static boolean isRelevantForIfFeatureCheck(final TypeEffectiveStatement<?> typeStmt) {
        final var typeDefinition = typeStmt.getTypeDefinition();
        return typeDefinition instanceof EnumTypeDefinition || typeDefinition instanceof BitsTypeDefinition
                || typeDefinition instanceof UnionTypeDefinition;
    }

    private static boolean isAnyDefaultValueMarkedWithIfFeature(final TypeEffectiveStatement<?> typeStmt,
            final Set<String> defaultValues) {
        final var iter = typeStmt.effectiveSubstatements().iterator();
        while (iter.hasNext() && !defaultValues.isEmpty()) {
            switch (iter.next()) {
                case BitEffectiveStatement bes -> {
                    if (defaultValues.remove(bes.argument()) && containsIfFeature(bes)) {
                        return true;
                    }
                }
                case EnumEffectiveStatement ees -> {
                    if (defaultValues.remove(ees.argument()) && containsIfFeature(ees)) {
                        return true;
                    }
                }
                case TypeEffectiveStatement<?> tes -> {
                    if (isAnyDefaultValueMarkedWithIfFeature(tes, defaultValues)) {
                        return true;
                    }
                }
                default -> {
                    // No-op
                }
            }
        }
        return false;
    }

    private static boolean containsIfFeature(final EffectiveStatement<?, ?> effectiveStatement) {
        for (var substatement : effectiveStatement.effectiveSubstatements()) {
            if (substatement instanceof IfFeatureEffectiveStatement) {
                return true;
            }
        }
        return false;
    }

    public static void checkUniqueGroupings(final EffectiveStmtCtx.Current<?, ?> stmt,
            final Collection<? extends EffectiveStatement<?, ?>> statements) {
        checkUniqueNodes(stmt, statements, GroupingDefinition.class);
    }

    public static void checkUniqueTypedefs(final EffectiveStmtCtx.Current<?, ?> stmt,
            final Collection<? extends EffectiveStatement<?, ?>> statements) {
        final var typedefs = new HashSet<TypeDefinition<?>>();
        for (var statement : statements) {
            if (statement instanceof TypedefEffectiveStatement tes && !typedefs.add(tes.getTypeDefinition())) {
                throw EffectiveStmtUtils.createNameCollisionSourceException(stmt, statement);
            }
        }
    }

    public static void checkUniqueUses(final EffectiveStmtCtx.Current<?, ?> stmt,
            final Collection<? extends EffectiveStatement<?, ?>> statements) {
        checkUniqueNodes(stmt, statements, UsesNode.class);
    }

    private static void checkUniqueNodes(final EffectiveStmtCtx.Current<?, ?> stmt,
            final Collection<? extends EffectiveStatement<?, ?>> statements, final Class<?> type) {
        final var nodes = new HashSet<EffectiveStatement<?, ?>>();
        for (var statement : statements) {
            if (type.isInstance(statement) && !nodes.add(statement)) {
                throw EffectiveStmtUtils.createNameCollisionSourceException(stmt, statement);
            }
        }
    }

    public static int historyAndStatusFlags(final CopyableNode history,
            final Collection<? extends EffectiveStatement<?, ?>> substatements) {
        return new FlagsBuilder()
            .setHistory(history)
            .setStatus(substatements.stream()
                .filter(StatusEffectiveStatement.class::isInstance)
                .findAny()
                .map(stmt -> ((StatusEffectiveStatement) stmt).argument())
                .orElse(Status.CURRENT))
            .toFlags();
    }

    public static <T extends CopyableNode & DocumentedNode.WithStatus> int historyAndStatusFlags(final T stmt) {
        return new FlagsBuilder().setHistory(stmt).setStatus(stmt.getStatus()).toFlags();
    }
}
