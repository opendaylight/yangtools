/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.SetMultimap;
import java.util.Collection;
import java.util.EnumMap;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.api.source.YangSourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.api.source.YinSourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.source.YinTextSource;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextToIRSourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.YinDOMSource;
import org.opendaylight.yangtools.yang.model.spi.source.YinTextToDOMSourceTransformer;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundles.ValidationBundleType;

public final class CrossSourceStatementReactor {
    private final ImmutableMap<ModelProcessingPhase, StatementSupportBundle> supportedTerminology;
    private final ImmutableMap<ValidationBundleType, Collection<?>> supportedValidation;

    CrossSourceStatementReactor(final EnumMap<ModelProcessingPhase, StatementSupportBundle> supportedTerminology,
            final EnumMap<ValidationBundleType, Collection<?>> supportedValidation) {
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
        return new ReactorBuildAction(supportedTerminology, supportedValidation);
    }

    /**
     * Start a new reactor build using the default statement parser mode with all features and deviations enabled.
     *
     * @return A new {@link BuildAction}.
     */
    @NonNullByDefault
    public BuildAction.WithYang<YangTextSource> newBuild(final YangTextToIRSourceTransformer textToIR) {
        return new YangReactorBuildAction<>(supportedTerminology, supportedValidation, textToIR);
    }

    /**
     * Start a new reactor build using the default statement parser mode with all features and deviations enabled.
     *
     * @return A new {@link BuildAction}.
     */
    @NonNullByDefault
    public BuildAction.WithYin<YinTextSource> newBuild(final YinTextToDOMSourceTransformer textToDOM) {
        return new YinReactorBuildAction<>(supportedTerminology, supportedValidation, textToDOM);
    }

    /**
     * Start a new reactor build using the default statement parser mode with all features and deviations enabled.
     *
     * @return A new {@link BuildAction}.
     */
    @NonNullByDefault
    public BuildAction.Full<YangTextSource, YinTextSource> newBuild(final YangTextToIRSourceTransformer textToIR,
            final YinTextToDOMSourceTransformer textToDOM) {
        return new FullReactorBuildAction<>(supportedTerminology, supportedValidation, textToIR, textToDOM);
    }

    public static class Builder implements Mutable {
        private final EnumMap<ValidationBundleType, Collection<?>> validationBundles =
                new EnumMap<>(ValidationBundleType.class);
        private final EnumMap<ModelProcessingPhase, StatementSupportBundle> bundles =
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

    /**
     * A single attempt at resolving a set of schema sources.
     */
    // FIXME: rename to SourceLinkageBuilder
    public sealed interface BuildAction permits ReactorBuildAction, BuildAction.WithYang, BuildAction.WithYin {
        /**
         * A {@link BuildAction} capable of accepting YANG source representations.
         *
         * @param <S> concrete {@link YangSourceRepresentation}
         * @since 15.0.0
         */
        sealed interface WithYang<S extends YangSourceRepresentation> extends BuildAction
                permits YangReactorBuildAction, Full {
            /**
             * Add a YANG source. All main sources are present in resulting {@link EffectiveSchemaContext}.
             *
             * @param source which should be transformed and added into main sources
             * @return This build action, for fluent use.
             * @throws SourceSyntaxException if the source is not syntactically valid
             */
            @NonNullByDefault
            WithYang<S> addSource(S source) throws ExtractorException, SourceSyntaxException;

            /**
             * Add a library YANG source. Only library sources required by main sources are present in resulting
             * {@link EffectiveSchemaContext}. Any other library sources are ignored and this also applies to error
             * reporting.
             *
             * <p>Library sources are not supported in semantic version mode currently.
             *
             * @param source which should be transformed and added into main sources
             * @return This build action, for fluent use.
             * @throws SourceSyntaxException if the source is not syntactically valid
             */
            @NonNullByDefault
            WithYang<S> addLibSource(S source) throws SourceSyntaxException;
        }

        /**
         * A {@link BuildAction} capable of accepting YIN source representations.
         *
         * @param <S> concrete {@link YinSourceRepresentation}
         * @since 15.0.0
         */
        sealed interface WithYin<S extends YinSourceRepresentation> extends BuildAction
                permits YinReactorBuildAction, Full {
            /**
             * Add a YIN source. All main sources are present in resulting {@link EffectiveSchemaContext}.
             *
             * @param source which should be transformed and added into main sources
             * @return This build action, for fluent use.
             * @throws SourceSyntaxException if the source is not syntactically valid
             */
            @NonNullByDefault
            WithYin<S> addSource(S source) throws ExtractorException, SourceSyntaxException;

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
            WithYin<S> addLibSource(S libSource) throws SourceSyntaxException;
        }

        /**
         * A {@link BuildAction} capable of accepting YIN source representations.
         *
         * @param <H> concrete {@link YangSourceRepresentation}
         * @param <M> concrete {@link YinSourceRepresentation}
         * @since 15.0.0
         */
        sealed interface Full<H extends YangSourceRepresentation, M extends YinSourceRepresentation>
                extends WithYang<H>, WithYin<M> permits FullReactorBuildAction {
            @Override
            Full<H, M> addSource(H source) throws ExtractorException, SourceSyntaxException;

            @Override
            Full<H, M> addSource(M source) throws ExtractorException, SourceSyntaxException;

            @Override
            Full<H, M> addLibSource(H source) throws SourceSyntaxException;

            @Override
            Full<H, M> addLibSource(M source) throws SourceSyntaxException;
        }

        /**
         * Add a main source. All main sources are present in resulting {@link EffectiveSchemaContext}.
         *
         * @param source which should be added into main sources
         * @return This build action, for fluent use.
         */
        @NonNullByDefault
        BuildAction addSource(YangIRSource source) throws ExtractorException;

        /**
         * Add a main source. All main sources are present in resulting {@link EffectiveSchemaContext}.
         *
         * @param source which should be added into main sources
         * @return This build action, for fluent use.
         */
        @NonNullByDefault
        BuildAction addSource(YinDOMSource source) throws ExtractorException;

        /**
         * Add a transformed main source. All main sources are present in resulting {@link EffectiveSchemaContext}.
         *
         * @param <S> source representation type
         * @param transformer the transformer to {@link YangIRSource}
         * @param source which should be transformed and added into main sources
         * @return This build action, for fluent use.
         * @throws ExtractorException if the source cannot be analyzed
         * @throws SourceSyntaxException if the source is not syntactically valid
         */
        @NonNullByDefault
        default <S extends SourceRepresentation> BuildAction addYangSource(
                final SourceTransformer<S, YangIRSource> transformer, final S source)
                    throws ExtractorException, SourceSyntaxException {
            return addSource(transformer.transformSource(source));
        }

        /**
         * Add a transformed main source. All main sources are present in resulting {@link EffectiveSchemaContext}.
         *
         * @param <S> source representation type
         * @param transformer the transformer to {@link YangIRSource}
         * @param source which should be transformed and added into main sources
         * @return This build action, for fluent use.
         * @throws ExtractorException if the source cannot be analyzed
         * @throws SourceSyntaxException if the source is not syntactically valid
         */
        @NonNullByDefault
        default <S extends SourceRepresentation> BuildAction addYinSource(
                final SourceTransformer<S, YinDOMSource> transformer, final S source)
                    throws ExtractorException, SourceSyntaxException {
            return addSource(transformer.transformSource(source));
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
         */
        @NonNullByDefault
        BuildAction addLibSource(YangIRSource libSource);

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
        BuildAction addLibSource(YinDOMSource libSource);

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
        default <S extends SourceRepresentation> BuildAction addLibYangSource(
                final SourceTransformer<S, YangIRSource> transformer, final S source) throws SourceSyntaxException {
            return addLibSource(transformer.transformSource(source));
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
        default <S extends SourceRepresentation> BuildAction addLibYinSource(
                final SourceTransformer<S, YinDOMSource> transformer, final S source) throws SourceSyntaxException {
            return addLibSource(transformer.transformSource(source));
        }

        // FIXME: add a SourceLinkage interface which will contain the below methods and add
        //
        //          SourceLinkage build();
        //
        //        as a partocilar set of consistent sources can be built with any number of features.

        /**
         * Set supported features based on which all if-feature statements in the
         * parsed YANG modules will be resolved.
         *
         * @param supportedFeatures
         *            Set of supported features in the final SchemaContext.
         *            If the set is empty, no features encountered will be supported.
         * @return This build action, for fluent use.
         */
        @NonNullByDefault
        BuildAction setSupportedFeatures(FeatureSet supportedFeatures);

        /**
         * Set YANG modules which can be deviated by specified modules during the parsing process.
         * Map key (QNameModule) denotes a module which can be deviated by the modules in the Map value.
         *
         * @param modulesDeviatedByModules
         *            Map of YANG modules (Map key) which can be deviated by specified modules (Map value) in the final
         *            SchemaContext. If the map is empty, no deviations encountered will be supported.
         * @return This build action, for fluent use.
         */
        @NonNull BuildAction setModulesWithSupportedDeviations(
                @NonNull SetMultimap<QNameModule, QNameModule> modulesDeviatedByModules);

        /**
         * Build the {@link ReactorDeclaredModel} view of this action.
         *
         * @return A declared view of selected models.
         * @throws ExtractorException if a source cannot be analyzed
         * @throws ReactorException if the declared model cannot be built
         */
        @NonNull ReactorDeclaredModel buildDeclared() throws ExtractorException, ReactorException;

        /**
         * Build the {@link EffectiveSchemaContext} view of this action.
         *
         * @return An effective view of selected models.
         * @throws ExtractorException if a source cannot be analyzed
         * @throws ReactorException if the effective model cannot be built
         */
        @NonNull EffectiveSchemaContext buildEffective() throws ExtractorException, ReactorException;
    }
}
