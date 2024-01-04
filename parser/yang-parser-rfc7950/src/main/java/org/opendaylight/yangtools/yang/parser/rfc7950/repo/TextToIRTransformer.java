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
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.ir.YangIRSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.rfc7950.antlr.IRSupport;

@Beta
public final class TextToIRTransformer extends SchemaSourceTransformer<YangTextSource, YangIRSchemaSource> {
    private TextToIRTransformer(final SchemaRepository provider, final SchemaSourceRegistry consumer) {
        super(provider, YangTextSource.class, consumer, YangIRSchemaSource.class,
            input -> Futures.immediateFuture(transformText(input)));
    }

    public static @NonNull TextToIRTransformer create(final SchemaRepository provider,
            final SchemaSourceRegistry consumer) {
        return new TextToIRTransformer(provider, consumer);
    }

    public static @NonNull YangIRSchemaSource transformText(final YangTextSource text)
            throws YangSyntaxErrorException, IOException {
        final var rootStatement = IRSupport.createStatement(YangStatementStreamSource.parseYangSource(text));
        final var info = YangIRSourceInfoExtractor.forIR(rootStatement, text.sourceId());
        return new YangIRSchemaSource(info.sourceId(), rootStatement, text.symbolicName());
    }
}
