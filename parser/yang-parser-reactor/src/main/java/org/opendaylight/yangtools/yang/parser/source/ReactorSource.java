/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;

/**
 * A single registered source. Allows instantiating {@link SourceInfo} and {@link StatementStreamSource} to further
 * process the source.
 *
 * @param extractor the {@link SourceInfoExtractor}
 * @param streamSupplier the {@link StatementStreamSource} supplier
 */
@NonNullByDefault
public record ReactorSource<S extends SourceRepresentation & SourceInfo.Extractor>(
        S source,
        Function<S, StatementStreamSource> streamFactory) implements Supplier<StatementStreamSource> {
    public ReactorSource {
        requireNonNull(source);
        requireNonNull(streamFactory);
    }

    @Override
    public StatementStreamSource get() {
        return streamFactory.apply(source);
    }
}