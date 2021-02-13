/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

/**
 * SchemaContextFactory configuration class.
 *
 * <p>
 * SchemaContextFactoryConfiguration supports currently the following options to be set:
 * <ul>
 * <li>schema source filter</li>
 * <li>statement parser mode</li>
 * <li>supported features</li>
 * <li>supported deviations</li>
 * </ul>
 */
@Beta
public final class SchemaContextFactoryConfiguration implements Immutable {
    private static final @NonNull SchemaContextFactoryConfiguration DEFAULT_CONFIGURATION = new Builder().build();

    private final @NonNull SchemaSourceFilter filter;
    private final @NonNull StatementParserMode statementParserMode;
    private final @Nullable ImmutableSet<QName> supportedFeatures;
    private final @Nullable ImmutableSetMultimap<QNameModule, QNameModule> modulesDeviatedByModules;

    private SchemaContextFactoryConfiguration(final @NonNull SchemaSourceFilter filter,
            final @NonNull StatementParserMode statementParserMode,
            final @Nullable ImmutableSet<QName> supportedFeatures,
            final @Nullable ImmutableSetMultimap<QNameModule, QNameModule> modulesDeviatedByModules) {
        this.filter = requireNonNull(filter);
        this.statementParserMode = requireNonNull(statementParserMode);
        this.supportedFeatures = supportedFeatures;
        this.modulesDeviatedByModules = modulesDeviatedByModules;
    }

    public @NonNull SchemaSourceFilter getSchemaSourceFilter() {
        return filter;
    }

    public @NonNull StatementParserMode getStatementParserMode() {
        return statementParserMode;
    }

    public Optional<Set<QName>> getSupportedFeatures() {
        return Optional.ofNullable(supportedFeatures);
    }

    public Optional<SetMultimap<QNameModule, QNameModule>> getModulesDeviatedByModules() {
        return Optional.ofNullable(modulesDeviatedByModules);
    }

    public static @NonNull SchemaContextFactoryConfiguration getDefault() {
        return DEFAULT_CONFIGURATION;
    }

    public static @NonNull Builder builder() {
        return new Builder();
    }

    @Override
    public int hashCode() {
        return Objects.hash(filter, statementParserMode, supportedFeatures, modulesDeviatedByModules);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SchemaContextFactoryConfiguration)) {
            return false;
        }
        final SchemaContextFactoryConfiguration other = (SchemaContextFactoryConfiguration) obj;
        return filter.equals(other.filter) && statementParserMode.equals(other.statementParserMode)
                && Objects.equals(supportedFeatures, other.supportedFeatures)
                && Objects.equals(modulesDeviatedByModules, other.modulesDeviatedByModules);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues().add("schemaSourceFilter", filter)
                .add("statementParserMode", statementParserMode).add("supportedFeatures", supportedFeatures)
                .add("modulesDeviatedByModules", modulesDeviatedByModules).toString();
    }

    public static class Builder
            implements org.opendaylight.yangtools.concepts.Builder<SchemaContextFactoryConfiguration> {
        private SchemaSourceFilter filter = SchemaSourceFilter.ALWAYS_ACCEPT;
        private StatementParserMode statementParserMode = StatementParserMode.DEFAULT_MODE;
        private ImmutableSetMultimap<QNameModule, QNameModule> modulesDeviatedByModules;
        private ImmutableSet<QName> supportedFeatures;

        /**
         * Set schema source filter which will filter available schema sources using the provided filter.
         *
         * @param filter schema source filter which acts as the gating function before a schema source is considered
         *               by the factory for inclusion in the SchemaContext it produces.
         * @return this builder
         */
        public @NonNull Builder setFilter(final @NonNull SchemaSourceFilter filter) {
            this.filter = requireNonNull(filter);
            return this;
        }

        /**
         * Set YANG statement parser mode.
         *
         * @param statementParserMode mode of yang statement parser
         * @return this builder
         */
        public @NonNull Builder setStatementParserMode(final @NonNull StatementParserMode statementParserMode) {
            this.statementParserMode = requireNonNull(statementParserMode);
            return this;
        }

        /**
         * Set supported features based on which all if-feature statements in the parsed YANG modules will be resolved.
         *
         * @param supportedFeatures Set of supported features in the final SchemaContext. If the set is empty, no
         *                          features encountered will be supported.
         * @return this builder
         */
        public @NonNull Builder setSupportedFeatures(final Set<QName> supportedFeatures) {
            this.supportedFeatures = supportedFeatures != null ? ImmutableSet.copyOf(supportedFeatures) : null;
            return this;
        }

        /**
         * Set YANG modules which can be deviated by specified modules during the parsing process. Map key (QNameModule)
         * denotes a module which can be deviated by the modules in the Map value.
         *
         * @param modulesDeviatedByModules Map of YANG modules (Map key) which can be deviated by specified modules
         *                                 (Map values) in the final SchemaContext. If the map is empty, no deviations
         *                                 encountered will be supported. If the map is null, all deviations will be
         *                                 applied.
         * @return this builder
         */
        public @NonNull Builder setModulesDeviatedByModules(
                final @Nullable SetMultimap<QNameModule, QNameModule> modulesDeviatedByModules) {
            this.modulesDeviatedByModules = modulesDeviatedByModules != null
                    ? ImmutableSetMultimap.copyOf(modulesDeviatedByModules) : null;
            return this;
        }

        @Override
        public @NonNull SchemaContextFactoryConfiguration build() {
            return new SchemaContextFactoryConfiguration(filter, statementParserMode, supportedFeatures,
                    modulesDeviatedByModules);
        }
    }
}
