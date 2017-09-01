/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType;

public final class CrossSourceStatementReactor {
    private final Map<ModelProcessingPhase, StatementSupportBundle> supportedTerminology;
    private final Map<ValidationBundleType, Collection<?>> supportedValidation;

    CrossSourceStatementReactor(final Map<ModelProcessingPhase, StatementSupportBundle> supportedTerminology,
            final Map<ValidationBundleType, Collection<?>> supportedValidation) {
        this.supportedTerminology = ImmutableMap.copyOf(supportedTerminology);
        this.supportedValidation = ImmutableMap.copyOf(supportedValidation);
    }

    /**
     * Create a new {@link Builder}.
     *
     * @return A new builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Start a new reactor build using the default statement parser mode with all features and deviations enabled.
     *
     * @return A new {@link BuildAction}.
     */
    public BuildAction newBuild() {
        return newBuild(StatementParserMode.DEFAULT_MODE);
    }

    /**
     * Start a new reactor build using the default statement parser mode and enabling only the specified features
     * and all deviations.
     *
     * @param supportedFeatures The set of supported features in the final SchemaContext
     * @return A new {@link BuildAction}.
     *
     * @deprecated Use {@link #newBuild()} and then call setSupportedFeatures() on the created BuildAction instead.
     */
    @Deprecated
    public BuildAction newBuild(final Set<QName> supportedFeatures) {
        final BuildAction buildAction = newBuild();
        if (supportedFeatures != null) {
            buildAction.setSupportedFeatures(supportedFeatures);
        }

        return buildAction;
    }

    /**
     * Start a new reactor build using the default statement parser mode and enabling only the specified features
     * and all deviations.
     *
     * @param supportedFeatures The set of supported features in the final SchemaContext, if present.
     * @return A new {@link BuildAction}.
     *
     * @deprecated Use {@link #newBuild()} and then call setSupportedFeatures() on the created BuildAction instead.
     */
    @Deprecated
    public BuildAction newBuild(final Optional<Set<QName>> supportedFeatures) {
        final BuildAction buildAction = newBuild();
        if (supportedFeatures.isPresent()) {
            buildAction.setSupportedFeatures(supportedFeatures.get());
        }

        return buildAction;
    }

    /**
     * Start a new reactor build using the specified statement parser mode and enabling all features and deviations.
     *
     * @param statementParserMode Parser mode to use
     * @return A new {@link BuildAction}.
     * @throws NullPointerException if statementParserMode is null
     */
    public BuildAction newBuild(final StatementParserMode statementParserMode) {
        return new BuildAction(statementParserMode);
    }

    /**
     * Start a new reactor build using the specified statement parser mode and enabling only the specified features
     * and all deviations.
     *
     * @param statementParserMode Parser mode to use
     * @param supportedFeatures The set of supported features in the final SchemaContext
     * @return A new {@link BuildAction}.
     * @throws NullPointerException if statementParserMode is null
     *
     * @deprecated Use {@link #newBuild(StatementParserMode)} and then call setSupportedFeatures() on the created
     *             BuildAction instead.
     */
    @Deprecated
    public BuildAction newBuild(final StatementParserMode statementParserMode,
            final Set<QName> supportedFeatures) {
        final BuildAction buildAction = new BuildAction(statementParserMode);
        if (supportedFeatures != null) {
            buildAction.setSupportedFeatures(supportedFeatures);
        }

        return buildAction;
    }

    /**
     * Start a new reactor build using the specified statement parser mode and enabling only the specified features
     * and all deviations.
     *
     * @param statementParserMode Parser mode to use
     * @param supportedFeatures The set of supported features in the final SchemaContext, or absent if all features
     *                          encountered should be supported.
     * @return A new {@link BuildAction}.
     * @throws NullPointerException if statementParserMode is null
     *
     * @deprecated Use {@link #newBuild(StatementParserMode)} and then call setSupportedFeatures() on the created
     *             BuildAction instead.
     */
    @Deprecated
    public BuildAction newBuild(final StatementParserMode statementParserMode,
            final Optional<Set<QName>> supportedFeatures) {
        final BuildAction buildAction = new BuildAction(statementParserMode);
        if (supportedFeatures.isPresent()) {
            buildAction.setSupportedFeatures(supportedFeatures.get());
        }

        return buildAction;
    }

    public static class Builder implements org.opendaylight.yangtools.concepts.Builder<CrossSourceStatementReactor> {
        private final Map<ValidationBundleType, Collection<?>> validationBundles =
                new EnumMap<>(ValidationBundleType.class);
        private final Map<ModelProcessingPhase, StatementSupportBundle> bundles =
                new EnumMap<>(ModelProcessingPhase.class);

        public Builder setBundle(final ModelProcessingPhase phase, final StatementSupportBundle bundle) {
            bundles.put(phase, bundle);
            return this;
        }

        public Builder setValidationBundle(final ValidationBundleType type, final Collection<?> validationBundle) {
            validationBundles.put(type, validationBundle);
            return this;
        }

        @Override
        public CrossSourceStatementReactor build() {
            return new CrossSourceStatementReactor(bundles, validationBundles);
        }
    }

    public class BuildAction {
        private final BuildGlobalContext context;
        private boolean supportedFeaturesSet = false;
        private boolean modulesDeviatedByModulesSet = false;

        BuildAction(@Nonnull final StatementParserMode statementParserMode) {
            this.context = new BuildGlobalContext(supportedTerminology,supportedValidation,
                    Preconditions.checkNotNull(statementParserMode));
        }

        /**
         * Add main source. All main sources are present in resulting SchemaContext.
         *
         * @param source
         *            which should be added into main sources
         */
        public void addSource(final StatementStreamSource source) {
            context.addSource(source);
        }

        /**
         * Add main sources. All main sources are present in resulting SchemaContext.
         *
         * @param sources
         *            which should be added into main sources
         */
        public void addSources(final StatementStreamSource... sources) {
            addSources(Arrays.asList(sources));
        }

        public void addSources(final Collection<? extends StatementStreamSource> sources) {
            for (final StatementStreamSource source : sources) {
                context.addSource(source);
            }
        }

        /**
         * Add library sources. Only library sources required by main sources are present in resulting SchemaContext.
         * Any other library sources are ignored and this also applies to error reporting.
         *
         * <p>
         * Library sources are not supported in semantic version mode currently.
         *
         * @param libSources
         *            yang sources which should be added into library sources
         */
        public void addLibSources(final StatementStreamSource... libSources) {
            addLibSources(Arrays.asList(libSources));
        }

        public void addLibSources(final Collection<StatementStreamSource> libSources) {
            for (final StatementStreamSource libSource : libSources) {
                context.addLibSource(libSource);
            }
        }

        /**
         * Set supported features based on which all if-feature statements in the
         * parsed YANG modules will be resolved.
         *
         * @param supportedFeatures
         *            Set of supported features in the final SchemaContext.
         *            If the set is empty, no features encountered will be supported.
         */
        public void setSupportedFeatures(@Nonnull final Set<QName> supportedFeatures) {
            Preconditions.checkState(!supportedFeaturesSet, "Supported features should be set only once.");
            context.setSupportedFeatures(Preconditions.checkNotNull(supportedFeatures));
            supportedFeaturesSet = true;
        }

        /**
         * Set YANG modules which can be deviated by specified modules during the parsing process.
         * Map key (QNameModule) denotes a module which can be deviated by the modules in the Map value.
         *
         * @param modulesDeviatedByModules
         *            Map of YANG modules (Map key) which can be deviated by specified modules (Map value) in the final
         *            SchemaContext. If the map is empty, no deviations encountered will be supported.
         */
        public void setModulesWithSupportedDeviations(
                @Nonnull final Map<QNameModule, Set<QNameModule>> modulesDeviatedByModules) {
            Preconditions.checkState(!modulesDeviatedByModulesSet,
                    "Modules with supported deviations should be set only once.");
            context.setModulesDeviatedByModules(Preconditions.checkNotNull(modulesDeviatedByModules));
            modulesDeviatedByModulesSet = true;
        }

        /**
         * Build the effective model context.
         */
        public EffectiveModelContext build() throws ReactorException {
            return context.build();
        }

        public EffectiveSchemaContext buildEffective() throws ReactorException {
            return context.buildEffective();
        }
    }
}
