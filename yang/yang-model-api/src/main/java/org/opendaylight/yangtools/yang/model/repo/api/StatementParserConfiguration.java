/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.repo.api;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

/**
 * Configuration of the YANG statement parser
 *
 * The configuration currently provides these options:
 * <ul>
 * <li>Setting the {@link StatementParserMode}</li>
 * <li>Setting the if-feature statement resolution mode by providing a Set of QNames:
 * <ul>
 * <li>Optional containing a Set of QNames - only the features specified in the Set are supported</li>
 * <li>Optional containing an empty Set - no feature in the processed model is supported</li>
 * <li>empty Optional - all features in the processed model are supported</li>
 * </ul>
 * </li>
 * <li>Setting the modules with activated deviation statements by providing a Set of QNameModules:
 * <ul>
 * <li>Optional containing a Set of QNameModules - only the modules specified in the Set support deviations</li>
 * <li>Optional containing an empty Set - no module in the processed model supports deviations</li>
 * <li>empty Optional - all modules in the processed model support deviations</li>
 * </ul>
 * </li>
 * </ul>
 *
 * The configuration can be extended with additional options if need be.
 */
@Beta
public final class StatementParserConfiguration implements Immutable {
    private static final StatementParserConfiguration DEFAULT = new Builder(StatementParserMode.DEFAULT_MODE).build();

    private final StatementParserMode statementParserMode;
    private final Optional<Set<QName>> supportedFeatures;
    private final Optional<Set<QNameModule>> modulesWithSupportedDeviations;

    private StatementParserConfiguration(final StatementParserMode statementParserMode,
            final Optional<Set<QName>> supportedFeatures,
            final Optional<Set<QNameModule>> modulesWithSupportedDeviations) {
        this.statementParserMode = statementParserMode;
        this.supportedFeatures = supportedFeatures;
        this.modulesWithSupportedDeviations = modulesWithSupportedDeviations;
    }

    public static StatementParserConfiguration getDefault() {
        return DEFAULT;
    }

    public StatementParserMode getStatementParserMode() {
        return statementParserMode;
    }

    public Optional<Set<QName>> getSupportedFeatures() {
        return supportedFeatures;
    }

    public Optional<Set<QNameModule>> getModulesWithSupportedDeviations() {
        return modulesWithSupportedDeviations;
    }

    public static class Builder implements org.opendaylight.yangtools.concepts.Builder<StatementParserConfiguration> {
        private final StatementParserMode statementParserMode;
        private Optional<Set<QName>> supportedFeatures;
        private Optional<Set<QNameModule>> modulesWithSupportedDeviations;

        public Builder(final StatementParserMode statementParserMode) {
            this.statementParserMode = Preconditions.checkNotNull(statementParserMode);
            supportedFeatures = Optional.empty();
            modulesWithSupportedDeviations = Optional.empty();
        }

        public Builder setSupportedFeatures(final Optional<Set<QName>> supportedFeatures) {
            this.supportedFeatures = supportedFeatures;
            return this;
        }

        public Builder setModulesWithSupportedDeviations(
                final Optional<Set<QNameModule>> modulesWithSupportedDeviations) {
            this.modulesWithSupportedDeviations = modulesWithSupportedDeviations;
            return this;
        }

        @Override
        public StatementParserConfiguration build() {
            return new StatementParserConfiguration(statementParserMode, supportedFeatures,
                    modulesWithSupportedDeviations);
        }
    }
}
