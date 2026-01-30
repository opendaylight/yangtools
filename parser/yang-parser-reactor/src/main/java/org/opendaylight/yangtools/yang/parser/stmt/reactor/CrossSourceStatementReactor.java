/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.SetMultimap;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.spi.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.model.spi.source.YinDomSource;
import org.opendaylight.yangtools.yang.parser.source.YangIRStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.source.YinDOMStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundles.ValidationBundleType;

public final class CrossSourceStatementReactor {
    private final ImmutableMap<ModelProcessingPhase, StatementSupportBundle> supportedTerminology;
    private final ImmutableMap<ValidationBundleType, Collection<?>> supportedValidation;

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
    public static @NonNull Builder builder() {
        return new Builder();
    }

    /**
     * Start a new reactor build using the default statement parser mode with all features and deviations enabled.
     *
     * @return A new {@link BuildAction}.
     */
    public @NonNull BuildAction newBuild() {
        return new BuildAction(supportedTerminology, supportedValidation);
    }

    public static class Builder implements Mutable {
        private final Map<ValidationBundleType, Collection<?>> validationBundles =
                new EnumMap<>(ValidationBundleType.class);
        private final Map<ModelProcessingPhase, StatementSupportBundle> bundles =
                new EnumMap<>(ModelProcessingPhase.class);

        public @NonNull Builder setBundle(final ModelProcessingPhase phase, final StatementSupportBundle bundle) {
            bundles.put(phase, bundle);
            return this;
        }

        public @NonNull Builder setValidationBundle(final ValidationBundleType type,
                final Collection<?> validationBundle) {
            validationBundles.put(type, validationBundle);
            return this;
        }

        /**
         * Return a {@link CrossSourceStatementReactor} configured with current state of this builder.
         *
         * @return A CrossSourceStatementReactor
         */
        public @NonNull CrossSourceStatementReactor build() {
            return new CrossSourceStatementReactor(bundles, validationBundles);
        }
    }

    public static final class BuildAction {
        private final BuildGlobalContext context;

        private boolean supportedFeaturesSet = false;
        private boolean modulesDeviatedByModulesSet = false;

        BuildAction(final ImmutableMap<ModelProcessingPhase, StatementSupportBundle> supportedTerminology,
                final ImmutableMap<ValidationBundleType, Collection<?>> supportedValidation) {
            context = new BuildGlobalContext(supportedTerminology, supportedValidation);
        }

        /**
         * Add a main source. All main sources are present in resulting {@link EffectiveSchemaContext}.
         *
         * @param source which should be added into main sources
         * @return This build action, for fluent use.
         * @deprecated Use {@link #addYangSource(YangIRSource)} instead.
         */
        @Deprecated(since = "15.0.0")
        @NonNullByDefault
        public BuildAction addSource(final YangIRSource source) {
            return addYangSource(source);
        }

        /**
         * Add a transformed main source. All main sources are present in resulting {@link EffectiveSchemaContext}.
         *
         * @param <S> source representation type
         * @param transformer the transformer to {@link YangIRSource}
         * @param source which should be transformed and added into main sources
         * @return This build action, for fluent use.
         * @throws SourceSyntaxException if the source is not syntactically valid
         * @deprecated Use {@link #addYangSource(SourceTransformer, SourceRepresentation)} instead.
         */
        @Deprecated(since = "15.0.0")
        @NonNullByDefault
        public <S extends SourceRepresentation> BuildAction addSource(
                final SourceTransformer<S, YangIRSource> transformer, final S source) throws SourceSyntaxException {
            return addYangSource(transformer, source);
        }

        /**
         * Add a main source. All main sources are present in resulting {@link EffectiveSchemaContext}.
         *
         * @param source which should be added into main sources
         * @return This build action, for fluent use.
         */
        @NonNullByDefault
        public BuildAction addYangSource(final YangIRSource source) {
            context.addSource(new YangIRStatementStreamSource(source));
            return this;
        }

        /**
         * Add a transformed main source. All main sources are present in resulting {@link EffectiveSchemaContext}.
         *
         * @param <S> source representation type
         * @param transformer the transformer to {@link YangIRSource}
         * @param source which should be transformed and added into main sources
         * @return This build action, for fluent use.
         * @throws SourceSyntaxException if the source is not syntactically valid
         */
        @NonNullByDefault
        public <S extends SourceRepresentation> BuildAction addYangSource(
                final SourceTransformer<S, YangIRSource> transformer, final S source) throws SourceSyntaxException {
            return addSource(transformer.transformSource(source));
        }

        /**
         * Add a main source. All main sources are present in resulting {@link EffectiveSchemaContext}.
         *
         * @param source which should be added into main sources
         * @return This build action, for fluent use.
         */
        @NonNullByDefault
        public BuildAction addYinSource(final YinDomSource source) {
            context.addSource(new YinDOMStatementStreamSource(source));
            return this;
        }

        /**
         * Add a transformed main source. All main sources are present in resulting {@link EffectiveSchemaContext}.
         *
         * @param <S> source representation type
         * @param transformer the transformer to {@link YangIRSource}
         * @param source which should be transformed and added into main sources
         * @return This build action, for fluent use.
         * @throws SourceSyntaxException if the source is not syntactically valid
         */
        @NonNullByDefault
        public <S extends SourceRepresentation> BuildAction addYinSource(
                final SourceTransformer<S, YinDomSource> transformer, final S source) throws SourceSyntaxException {
            return addYinSource(transformer.transformSource(source));
        }

        /**
         * Add a library source. Only library sources required by main sources are present in resulting
         * {@link EffectiveSchemaContext}. Any other library sources are ignored and this also applies to error
         * reporting.
         *
         * <p>Library sources are not supported in semantic version mode currently.
         *
         * @param libSource source which should be added into library sources
         * @return This build action, for fluent use.
         * @deprecated Use {@link #addLibYangSource(YangIRSource)} instead.
         */
        @Deprecated(since = "15.0.0")
        @NonNullByDefault
        public BuildAction addLibSource(final YangIRSource libSource) {
            context.addLibSource(new YangIRStatementStreamSource(libSource));
            return this;
        }

        /**
         * Add a transformed library source. Only library sources required by main sources are present in resulting
         * {@link EffectiveSchemaContext}. Any other library sources are ignored and this also applies to error
         * reporting.
         *
         * <p>Library sources are not supported in semantic version mode currently.
         *
         * @param <S> source representation type
         * @param transformer the transformer to {@link YangIRSource}
         * @param source which should be transformed and added into main sources
         * @return This build action, for fluent use.
         * @throws SourceSyntaxException if the source is not syntactically valid
         * @deprecated Use {@link #addLibYangSource(SourceTransformer, SourceRepresentation)} instead.
         */
        @Deprecated(since = "15.0.0")
        @NonNullByDefault
        public <S extends SourceRepresentation> BuildAction addLibSource(
                final SourceTransformer<S, YangIRSource> transformer, final S source) throws SourceSyntaxException {
            return addLibSource(transformer.transformSource(source));
        }

        /**
         * Add a library YANG source. Only library sources required by main sources are present in resulting
         * {@link EffectiveSchemaContext}. Any other library sources are ignored and this also applies to error
         * reporting.
         *
         * <p>Library sources are not supported in semantic version mode currently.
         *
         * @param libSource source which should be added into library sources
         * @return This build action, for fluent use.
         */
        @NonNullByDefault
        public BuildAction addLibYangSource(final YangIRSource libSource) {
            context.addLibSource(new YangIRStatementStreamSource(libSource));
            return this;
        }

        /**
         * Add a transformed YANG library source. Only library sources required by main sources are present in resulting
         * {@link EffectiveSchemaContext}. Any other library sources are ignored and this also applies to error
         * reporting.
         *
         * <p>Library sources are not supported in semantic version mode currently.
         *
         * @param <S> source representation type
         * @param transformer the transformer to {@link YangIRSource}
         * @param source which should be transformed and added into main sources
         * @return This build action, for fluent use.
         * @throws SourceSyntaxException if the source is not syntactically valid
         */
        @NonNullByDefault
        public <S extends SourceRepresentation> BuildAction addLibYangSource(
                final SourceTransformer<S, YangIRSource> transformer, final S source) throws SourceSyntaxException {
            return addLibSource(transformer.transformSource(source));
        }

        /**
         * Add a library YIN source. Only library sources required by main sources are present in resulting
         * {@link EffectiveSchemaContext}. Any other library sources are ignored and this also applies to error
         * reporting.
         *
         * <p>Library sources are not supported in semantic version mode currently.
         *
         * @param libSource source which should be added into library sources
         * @return This build action, for fluent use.
         */
        @NonNullByDefault
        public BuildAction addLibYinSource(final YinDomSource libSource) {
            context.addLibSource(new YinDOMStatementStreamSource(libSource));
            return this;
        }

        /**
         * Add a transformed YIN library source. Only library sources required by main sources are present in resulting
         * {@link EffectiveSchemaContext}. Any other library sources are ignored and this also applies to error
         * reporting.
         *
         * <p>Library sources are not supported in semantic version mode currently.
         *
         * @param <S> source representation type
         * @param transformer the transformer to {@link YangIRSource}
         * @param source which should be transformed and added into main sources
         * @return This build action, for fluent use.
         * @throws SourceSyntaxException if the source is not syntactically valid
         */
        @NonNullByDefault
        public <S extends SourceRepresentation> BuildAction addLibYinSource(
                final SourceTransformer<S, YinDomSource> transformer, final S source) throws SourceSyntaxException {
            return addLibYinSource(transformer.transformSource(source));
        }

        /**
         * Set supported features based on which all if-feature statements in the
         * parsed YANG modules will be resolved.
         *
         * @param supportedFeatures
         *            Set of supported features in the final SchemaContext.
         *            If the set is empty, no features encountered will be supported.
         * @return This build action, for fluent use.
         */
        public @NonNull BuildAction setSupportedFeatures(final @NonNull FeatureSet supportedFeatures) {
            checkState(!supportedFeaturesSet, "Supported features should be set only once.");
            context.setSupportedFeatures(requireNonNull(supportedFeatures));
            supportedFeaturesSet = true;
            return this;
        }

        /**
         * Set YANG modules which can be deviated by specified modules during the parsing process.
         * Map key (QNameModule) denotes a module which can be deviated by the modules in the Map value.
         *
         * @param modulesDeviatedByModules
         *            Map of YANG modules (Map key) which can be deviated by specified modules (Map value) in the final
         *            SchemaContext. If the map is empty, no deviations encountered will be supported.
         * @return This build action, for fluent use.
         */
        public @NonNull BuildAction setModulesWithSupportedDeviations(
                final @NonNull SetMultimap<QNameModule, QNameModule> modulesDeviatedByModules) {
            checkState(!modulesDeviatedByModulesSet, "Modules with supported deviations should be set only once.");
            context.setModulesDeviatedByModules(requireNonNull(modulesDeviatedByModules));
            modulesDeviatedByModulesSet = true;
            return this;
        }

        /**
         * Build the {@link ReactorDeclaredModel} view of this action.
         *
         * @return A declared view of selected models.
         * @throws ReactorException if the declared model cannot be built
         */
        public @NonNull ReactorDeclaredModel build() throws ReactorException {
            return context.build();
        }

        /**
         * Build the {@link EffectiveSchemaContext} view of this action.
         *
         * @return An effective view of selected models.
         * @throws ReactorException if the effective model cannot be built
         */
        public @NonNull EffectiveSchemaContext buildEffective() throws ReactorException {
            return context.buildEffective();
        }
    }
}
