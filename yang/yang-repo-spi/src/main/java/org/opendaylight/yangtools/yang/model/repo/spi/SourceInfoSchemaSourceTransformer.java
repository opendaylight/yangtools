/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.spi;

import com.google.common.util.concurrent.Futures;
import java.util.function.BiFunction;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.model.spi.source.YinDomSource;

/**
 * A {@code SchemaSourceTransformer} built on top of a {@link SourceTransformer} producing {@link SourceRepresentation}s
 * which are also {@link SourceInfo.Extractor}s. It uses the extracted {@link SourceInfo} to provide accurate
 * {@link SourceRepresentation#sourceId()} information.
 *
 * @param <I> the input {@link SourceRepresentation}
 * @param <O> the output {@link SourceRepresentation}
 */
@NonNullByDefault
public final class SourceInfoSchemaSourceTransformer<
        I extends SourceRepresentation,
        O extends SourceRepresentation & SourceInfo.Extractor> extends SchemaSourceTransformer<I, O> {
    public SourceInfoSchemaSourceTransformer(final SchemaRepository provider, final SchemaSourceRegistry consumer,
            final SourceTransformer<I, O> transformer, final BiFunction<O, SourceIdentifier, O> rebind) {
        super(provider, transformer.inputRepresentation(), consumer, transformer.outputRepresentation(), input -> {
            final O output = transformer.transformSource(input);
            final var sourceId = output.extractSourceInfo().sourceId();
            return Futures.immediateFuture(sourceId.equals(output.sourceId()) ? output
                : rebind.apply(output, sourceId));
        });
    }

    /**
     * {@return a {@link SourceInfoSchemaSourceTransformer} resulting in a {@link YangIRSource}}
     * @param <I> the input {@link SourceRepresentation}
     * @param provider the provider {@link SchemaRepository}
     * @param consumer the consumer {@link SchemaSourceRegistry}
     * @param transformer the {@link SourceTransformer} producing the output
     */
    public static <I extends SourceRepresentation> SourceInfoSchemaSourceTransformer<I, YangIRSource> ofYang(
            final SchemaRepository provider, final SchemaSourceRegistry consumer,
            final SourceTransformer<I, YangIRSource> transformer) {
        return new SourceInfoSchemaSourceTransformer<>(provider, consumer, transformer,
            (source, sourceId) -> YangIRSource.of(sourceId, source.statement(), source.symbolicName()));
    }

    /**
     * {@return a {@link SourceInfoSchemaSourceTransformer} resulting in a {@link YinDomSource}}
     * @param <I> the input {@link SourceRepresentation}
     * @param provider the provider {@link SchemaRepository}
     * @param consumer the consumer {@link SchemaSourceRegistry}
     * @param transformer the {@link SourceTransformer} to use
     */
    public static <I extends SourceRepresentation> SourceInfoSchemaSourceTransformer<I, YinDomSource> ofYin(
            final SchemaRepository provider, final SchemaSourceRegistry consumer,
            final SourceTransformer<I, YinDomSource> transformer) {
        return new SourceInfoSchemaSourceTransformer<>(provider, consumer, transformer,
            (source, sourceId) -> YinDomSource.of(sourceId, source.domSource(), source.refProvider(),
                source.symbolicName()));
    }
}
