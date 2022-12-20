/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;

/**
 * Utility class for formatting {@link DeclaredStatement}s.
 *
 * @author Robert Varga
 */
@NonNullByDefault
public final class DeclaredStatementFormatter implements Immutable {
    private static final DeclaredStatementFormatter DEFAULT = new DeclaredStatementFormatter(ImmutableSet.of(), true);

    private final Set<StatementDefinition> ignoredStatements;
    private final boolean omitDefaultStatements;

    private int defaultOrganizationTreeDepth = 8;

    DeclaredStatementFormatter(final Set<StatementDefinition> ignoredStatements, final boolean omitDefaultStatements) {
        this.ignoredStatements = requireNonNull(ignoredStatements);
        this.omitDefaultStatements = omitDefaultStatements;
    }

    /**
     * Format specified statement into a {@link YangTextSnippet}.
     *
     * @param module parent module
     * @param statement statement to format
     * @return A {@link YangTextSnippet}
     * @throws NullPointerException if any of the arguments is null
     */
    public YangTextSnippet toYangTextSnippet(final ModuleEffectiveStatement module,
            final DeclaredStatement<?> statement) {
        return new YangTextSnippet(statement, StatementPrefixResolver.forModule(module), ignoredStatements,
            omitDefaultStatements);
    }

    public YangTextSnippet toYangTextSnippet(final SubmoduleEffectiveStatement submodule,
            final DeclaredStatement<?> statement) {
        return new YangTextSnippet(statement, StatementPrefixResolver.forSubmodule(submodule), ignoredStatements,
            omitDefaultStatements);
    }

    public YangOrganizationTree toYangOrganizationTree(final ModuleEffectiveStatement module,
            final DeclaredStatement<?> statement) {
        return toYangOrganizationTree(module, statement, defaultOrganizationTreeDepth);
    }

    public YangOrganizationTree toYangOrganizationTree(final SubmoduleEffectiveStatement submodule,
            final DeclaredStatement<?> statement) {
        return toYangOrganizationTree(submodule, statement, defaultOrganizationTreeDepth);
    }

    public YangOrganizationTree toYangOrganizationTree(final ModuleEffectiveStatement module,
            final DeclaredStatement<?> statement, final int depth) {
        return new YangOrganizationTree(statement, StatementPrefixResolver.forModule(module), ignoredStatements,
                depth);
    }

    public YangOrganizationTree toYangOrganizationTree(final SubmoduleEffectiveStatement submodule,
            final DeclaredStatement<?> statement, final int depth) {
        return new YangOrganizationTree(statement, StatementPrefixResolver.forSubmodule(submodule), ignoredStatements,
                depth);
    }

    /**
     * Return the default DeclaredStatementFormatter instance. The instance suppresses statements with default values
     * and does not ignore any statements.
     *
     * @return Default-configured instance.
     */
    public static DeclaredStatementFormatter defaultInstance() {
        return DEFAULT;
    }

    /**
     * Create a new {@link Builder}, which can be used to create customized DeclaredStatementFormatter instances.
     *
     * @return A new Builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for instantiation of a customized {@link DeclaredStatementFormatter}.
     */
    public static final class Builder implements Mutable {
        private final Set<StatementDefinition> ignoredStatements = new HashSet<>(4);
        private boolean retainDefaultStatements;

        private Builder() {
            // Hidden on purpose
        }

        /**
         * Add a statement which should be skipped along with any of its children.
         *
         * @param statementDef Statement to be ignored
         * @return This builder
         */
        public Builder addIgnoredStatement(final StatementDefinition statementDef) {
            ignoredStatements.add(requireNonNull(statementDef));
            return this;
        }

        /**
         * Retain common known statements whose argument matches semantics of not being present. By default these
         * statements are omitted from output.
         *
         * @return This builder
         */
        public Builder retainDefaultStatements() {
            retainDefaultStatements = true;
            return this;
        }

        /**
         * Return a {@link DeclaredStatementFormatter} based on this builder's current state.
         *
         * @return A DeclaredStatementFormatter
         */
        public DeclaredStatementFormatter build() {
            return new DeclaredStatementFormatter(ImmutableSet.copyOf(ignoredStatements), !retainDefaultStatements);
        }
    }
}
