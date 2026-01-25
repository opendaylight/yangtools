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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.parser.antlr.DefaultYangTextToIRSourceTransformer;

@Beta
public final class TextToIRTransformer extends SchemaSourceTransformer<YangTextSource, YangIRSource> {
    private TextToIRTransformer(final SchemaRepository provider, final SchemaSourceRegistry consumer,
            final DefaultYangTextToIRSourceTransformer textToIR) {
        super(provider, YangTextSource.class, consumer, YangIRSource.class,
            input -> Futures.immediateFuture(textToIR.transformSource(input)));
    }

    public static @NonNull TextToIRTransformer create(final SchemaRepository provider,
            final SchemaSourceRegistry consumer) {
        return new TextToIRTransformer(provider, consumer, new DefaultYangTextToIRSourceTransformer());
    }
}
