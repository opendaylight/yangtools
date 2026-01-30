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
import org.opendaylight.yangtools.yang.model.api.source.YinSourceRepresentation;
import org.opendaylight.yangtools.yang.model.spi.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.model.spi.source.YinDomSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundles.ValidationBundleType;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction.Full;

@NonNullByDefault
final class FullReactorBuildAction<H extends YangSourceRepresentation, M extends YinSourceRepresentation>
        extends YangReactorBuildAction<H> implements Full<H, M> {
    private final SourceTransformer<M, YinDomSource> yinTransformer;

    FullReactorBuildAction(final ImmutableMap<ModelProcessingPhase, StatementSupportBundle> supportedTerminology,
            final ImmutableMap<ValidationBundleType, Collection<?>> supportedValidation,
            final SourceTransformer<H, YangIRSource> yangTransformer,
            final SourceTransformer<M, YinDomSource> yinTransformer) {
        super(supportedTerminology, supportedValidation, yangTransformer);
        this.yinTransformer = requireNonNull(yinTransformer);
    }

    @Override
    public Full<H, M> addYangSource(final H source) throws SourceSyntaxException {
        super.addYangSource(source);
        return this;
    }

    @Override
    public Full<H, M> addYinSource(final M source) throws SourceSyntaxException {
        addYinSource(yinTransformer.transformSource(source));
        return this;
    }

    @Override
    public Full<H, M> addLibYangSource(final H source) throws SourceSyntaxException {
        super.addLibYangSource(source);
        return this;
    }

    @Override
    public Full<H, M> addLibYinSource(final M libSource) throws SourceSyntaxException {
        addLibYinSource(yinTransformer.transformSource(libSource));
        return this;
    }
}
