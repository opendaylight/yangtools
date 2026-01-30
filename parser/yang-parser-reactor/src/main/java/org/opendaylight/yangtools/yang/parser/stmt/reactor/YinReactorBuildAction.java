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
import org.opendaylight.yangtools.yang.model.api.source.YinSourceRepresentation;
import org.opendaylight.yangtools.yang.model.spi.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.YinDomSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundles.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction.WithYin;

@NonNullByDefault
final class YinReactorBuildAction<S extends YinSourceRepresentation> extends ReactorBuildAction implements WithYin<S> {
    private final SourceTransformer<S, YinDomSource> transformer;

    YinReactorBuildAction(final ImmutableMap<ModelProcessingPhase, StatementSupportBundle> supportedTerminology,
            final ImmutableMap<ValidationBundleType, Collection<?>> supportedValidation,
            final SourceTransformer<S, YinDomSource> transformer) {
        super(supportedTerminology, supportedValidation);
        this.transformer = requireNonNull(transformer);
    }

    @Override
    public WithYin<S> addSource(final S source) throws SourceSyntaxException {
        addSource(transformer.transformSource(source));
        return this;
    }

    @Override
    public WithYin<S> addLibSource(final S libSource) throws SourceSyntaxException {
        addLibSource(transformer.transformSource(libSource));
        return this;
    }
}
