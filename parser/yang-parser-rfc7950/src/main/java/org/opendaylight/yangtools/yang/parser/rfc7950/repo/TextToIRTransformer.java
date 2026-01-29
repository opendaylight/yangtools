/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.Futures;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextToIRSourceTransformer;

@Beta
public final class TextToIRTransformer extends SchemaSourceTransformer<YangTextSource, YangIRSource> {
    @NonNullByDefault
    public TextToIRTransformer(final SchemaRepository provider, final SchemaSourceRegistry consumer,
            final YangTextToIRSourceTransformer textToIR) {
        super(provider, YangTextSource.class, consumer, YangIRSource.class, input -> {
            final var output = textToIR.transformSource(input);
            return Futures.immediateFuture(
                YangIRSource.of(output.extractSourceInfo().sourceId(), output.statement(), input.symbolicName()));
        });
    }

    @Deprecated(since = "15.0.0", forRemoval = true)
    public static @NonNull TextToIRTransformer create(final SchemaRepository provider,
            final SchemaSourceRegistry consumer) {
        return new TextToIRTransformer(provider, consumer, ServiceLoader.load(YangTextToIRSourceTransformer.class)
            .findFirst().orElseThrow(() -> new NoSuchElementException("No YangTextToIRSourceTransformer found")));
    }
}
