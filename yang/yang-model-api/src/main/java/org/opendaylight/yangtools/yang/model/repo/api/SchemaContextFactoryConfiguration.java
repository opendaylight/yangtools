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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

/**
 * SchemaContextFactory configuration class.
 *
 * <p>
 * SchemaContextFactoryConfiguration supports currently the following options to
 * be set:
 * <ul>
 * <li>StatementParserMode</li>
 * <li>supported features</li>
 * <li>supported deviations</li>
 * </ul>
 */
@Beta
public class SchemaContextFactoryConfiguration implements Immutable {
    public static final SchemaContextFactoryConfiguration DEFAULT_CONFIGURATION = new Builder().build();

    private final StatementParserMode statementParserMode;
    private final Set<QName> supportedFeatures;
    private final Map<QNameModule, Set<QNameModule>> modulesDeviatedByModules;

    private SchemaContextFactoryConfiguration(final StatementParserMode statementParserMode,
            final Set<QName> supportedFeatures, final Map<QNameModule, Set<QNameModule>> modulesDeviatedByModules) {
        // :FIXME immutable copy of ?
        this.statementParserMode = requireNonNull(statementParserMode);
        this.supportedFeatures = supportedFeatures;
        this.modulesDeviatedByModules = modulesDeviatedByModules;
    }

    protected StatementParserMode getStatementParserMode() {
        return statementParserMode;
    }

    protected Set<QName> getSupportedFeatures() {
        return supportedFeatures;
    }

    protected Map<QNameModule, Set<QNameModule>> getModulesDeviatedByModules() {
        return modulesDeviatedByModules;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((modulesDeviatedByModules == null) ? 0 : modulesDeviatedByModules.hashCode());
        result = prime * result + ((statementParserMode == null) ? 0 : statementParserMode.hashCode());
        result = prime * result + ((supportedFeatures == null) ? 0 : supportedFeatures.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SchemaContextFactoryConfiguration other = (SchemaContextFactoryConfiguration) obj;
        if (modulesDeviatedByModules == null) {
            if (other.modulesDeviatedByModules != null)
                return false;
        } else if (!modulesDeviatedByModules.equals(other.modulesDeviatedByModules))
            return false;
        if (statementParserMode != other.statementParserMode)
            return false;
        if (supportedFeatures == null) {
            if (other.supportedFeatures != null)
                return false;
        } else if (!supportedFeatures.equals(other.supportedFeatures))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("statementParserMode", statementParserMode)
                .add("supportedFeatures", supportedFeatures).add("modulesDeviatedByModules", modulesDeviatedByModules)
                .toString();
    }

    public static SchemaContextFactoryConfiguration getDefault() {
        return DEFAULT_CONFIGURATION;
    }

    public static class Builder
            implements org.opendaylight.yangtools.concepts.Builder<SchemaContextFactoryConfiguration> {
        private StatementParserMode statementParserMode = StatementParserMode.DEFAULT_MODE;
        private Set<QName> supportedFeatures;
        private Map<QNameModule, Set<QNameModule>> modulesDeviatedByModules;

        /**
         * Set statement parser mode.
         *
         * @param statementParserMode
         *            mode of statement parser
         * @return this builder
         */
        public Builder setStatementParserMode(final StatementParserMode statementParserMode) {
            this.statementParserMode = statementParserMode;
            return this;
        }

        /**
         * Set supported features based on which all if-feature statements in
         * the parsed YANG modules will be resolved.
         *
         * @param supportedFeatures
         *            Set of supported features in the final SchemaContext. If
         *            the set is empty, no features encountered will be
         *            supported.
         * @return this builder
         */
        public Builder setSupportedFeatures(final Set<QName> supportedFeatures) {
            this.supportedFeatures = ImmutableSet.copyOf(supportedFeatures);
            return this;
        }

        /**
         * Set YANG modules which can be deviated by specified modules during
         * the parsing process. Map key (QNameModule) denotes a module which can
         * be deviated by the modules in the Map value.
         *
         * @param modulesDeviatedByModules
         *            Map of YANG modules (Map key) which can be deviated by
         *            specified modules (Map value) in the final SchemaContext.
         *            If the map is empty, no deviations encountered will be
         *            supported.
         * @return this builder
         */
        public Builder setModulesDeviatedByModules(final Map<QNameModule, Set<QNameModule>> modulesDeviatedByModules) {
            // :FIXME make a immutable copy of this map. Maybe consider conversion of Map to
            // Multimap... ??
            this.modulesDeviatedByModules = ImmutableMap.copyOf(modulesDeviatedByModules);
            return this;
        }

        @Override
        public SchemaContextFactoryConfiguration build() {
            return new SchemaContextFactoryConfiguration(statementParserMode, supportedFeatures,
                    modulesDeviatedByModules);
        }
    }
}