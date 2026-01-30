/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.source.YangSourceRepresentation;
import org.opendaylight.yangtools.yang.model.spi.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundles.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction.WithYang;

@NonNullByDefault
sealed class YangReactorBuildAction<S extends YangSourceRepresentation> extends ReactorBuildAction
        implements WithYang<S> permits FullReactorBuildAction {
    private final SourceTransformer<S, YangIRSource> transformer;

    YangReactorBuildAction(final ImmutableMap<ModelProcessingPhase, StatementSupportBundle> supportedTerminology,
            final ImmutableMap<ValidationBundleType, Collection<?>> supportedValidation,
            final SourceTransformer<S, YangIRSource> transformer) {
        super(supportedTerminology, supportedValidation);
        this.transformer = requireNonNull(transformer);
    }

    @Override
    public WithYang<S> addYangSource(final S source) throws SourceSyntaxException {
        addYangSource(transformer.transformSource(source));
        return this;
    }

    @Override
    public WithYang<S> addLibYangSource(final S source) throws SourceSyntaxException {
        addLibYangSource(transformer.transformSource(source));
        return this;
    }
}
