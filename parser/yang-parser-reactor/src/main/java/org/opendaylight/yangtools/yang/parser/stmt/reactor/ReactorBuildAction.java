/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorException;
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
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;

sealed class ReactorBuildAction implements CrossSourceStatementReactor.BuildAction
        permits YangReactorBuildAction, YinReactorBuildAction {
    private final @NonNull BuildGlobalContext context;

    private boolean supportedFeaturesSet = false;
    private boolean modulesDeviatedByModulesSet = false;

    ReactorBuildAction(final ImmutableMap<ModelProcessingPhase, StatementSupportBundle> supportedTerminology,
            final ImmutableMap<ValidationBundleType, Collection<?>> supportedValidation) {
        context = new BuildGlobalContext(supportedTerminology, supportedValidation);
    }

    @Override
    public BuildAction addSource(final YangIRSource source) throws ExtractorException {
        context.addSource(source, YangIRStatementStreamSource::new);
        return this;
    }

    @Override
    public BuildAction addSource(final YinDomSource source) throws ExtractorException {
        context.addSource(source, YinDOMStatementStreamSource::new);
        return this;
    }

    @Override
    public final BuildAction addLibSource(final YangIRSource libSource) {
        context.addLibSource(libSource, YangIRStatementStreamSource::new);
        return this;
    }

    @Override
    public final BuildAction addLibSource(final YinDomSource libSource) {
        context.addLibSource(libSource, YinDOMStatementStreamSource::new);
        return this;
    }

    @Override
    public final BuildAction setSupportedFeatures(final FeatureSet supportedFeatures) {
        checkState(!supportedFeaturesSet, "Supported features should be set only once.");
        context.setSupportedFeatures(requireNonNull(supportedFeatures));
        supportedFeaturesSet = true;
        return this;
    }

    @Override
    public final BuildAction setModulesWithSupportedDeviations(
            final SetMultimap<QNameModule, QNameModule> modulesDeviatedByModules) {
        checkState(!modulesDeviatedByModulesSet, "Modules with supported deviations should be set only once.");
        context.setModulesDeviatedByModules(requireNonNull(modulesDeviatedByModules));
        modulesDeviatedByModulesSet = true;
        return this;
    }

    @Override
    public final ReactorDeclaredModel buildDeclared()
            throws ExtractorException, ReactorException, SourceSyntaxException {
        return context.build();
    }

    @Override
    public final EffectiveSchemaContext buildEffective()
            throws ExtractorException, ReactorException, SourceSyntaxException {
        return context.buildEffective();
    }

    @Override
    @NonNullByDefault
    public final <S extends SourceRepresentation> BuildAction addLibYangSource(
            final SourceTransformer<S, YangIRSource> transformer, final S source) {
        context.addLibSource(transformer, source, YangIRStatementStreamSource::new);
        return this;
    }

    @Override
    @NonNullByDefault
    public final <S extends SourceRepresentation> BuildAction addLibYinSource(
            final SourceTransformer<S, YinDomSource> transformer, final S source) {
        context.addLibSource(transformer, source, YinDOMStatementStreamSource::new);
        return this;
    }
}
