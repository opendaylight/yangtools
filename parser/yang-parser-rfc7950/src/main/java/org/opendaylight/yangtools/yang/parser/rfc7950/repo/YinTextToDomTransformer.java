/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.Futures;
import java.io.IOException;
import org.opendaylight.yangtools.yang.model.api.source.YinTextSource;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.YinDomSource;
import org.xml.sax.SAXException;

/**
 * A {@link SchemaSourceTransformer} which handles translation of models from {@link YinTextSource} representation into
 * {@link YinDomSource}.
 */
@Beta
public final class YinTextToDomTransformer extends SchemaSourceTransformer<YinTextSource, YinDomSource> {
    private YinTextToDomTransformer(final SchemaRepository provider, final SchemaSourceRegistry consumer) {
        super(provider, YinTextSource.class, consumer, YinDomSource.class,
            input -> Futures.immediateFuture(YinDomSource.of(input)));
    }

    public static YinTextToDomTransformer create(final SchemaRepository provider, final SchemaSourceRegistry consumer) {
        return new YinTextToDomTransformer(provider, consumer);
    }

    @Deprecated(since = "14.0.22", forRemoval = true)
    public static YinDomSource transformSource(final YinTextSource source) throws SAXException, IOException {
        return YinDomSource.of(source);
    }
}
