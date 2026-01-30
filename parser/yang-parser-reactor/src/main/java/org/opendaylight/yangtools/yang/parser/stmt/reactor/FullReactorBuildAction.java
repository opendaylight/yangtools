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
import java.io.IOException;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.api.source.YangSourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.source.YinSourceRepresentation;
import org.opendaylight.yangtools.yang.model.spi.source.SourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.model.spi.source.YinDOMSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundles.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction.Full;

@NonNullByDefault
final class FullReactorBuildAction<H extends YangSourceRepresentation, M extends YinSourceRepresentation>
        extends YangReactorBuildAction<H> implements Full<H, M> {
    private final SourceTransformer<M, YinDOMSource> yinTransformer;

    FullReactorBuildAction(final ImmutableMap<ModelProcessingPhase, StatementSupportBundle> supportedTerminology,
            final ImmutableMap<ValidationBundleType, Collection<?>> supportedValidation,
            final SourceTransformer<H, YangIRSource> yangTransformer,
            final SourceTransformer<M, YinDOMSource> yinTransformer) {
        super(supportedTerminology, supportedValidation, yangTransformer);
        this.yinTransformer = requireNonNull(yinTransformer);
    }

    @Override
    public Full<H, M> addSource(final H source) throws IOException, SourceSyntaxException {
        super.addSource(source);
        return this;
    }

    @Override
    public Full<H, M> addSource(final M source) throws IOException, SourceSyntaxException {
        addSource(yinTransformer.transformSource(source));
        return this;
    }

    @Override
    public Full<H, M> addLibSource(final H source) {
        super.addLibSource(source);
        return this;
    }

    @Override
    public Full<H, M> addLibSource(final M libSource) {
        addLibYinSource(yinTransformer, libSource);
        return this;
    }
}
