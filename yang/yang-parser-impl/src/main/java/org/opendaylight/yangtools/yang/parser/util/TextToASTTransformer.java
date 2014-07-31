/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.YangContext;
import org.opendaylight.yangtools.util.concurrent.ExceptionMapper;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceTransformationException;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceTransformer;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

/**
 * A {@link SchemaSourceTransformer} which handles translation of models from
 * {@link YangTextSchemaSource} representation into {@link ASTSchemaSource}.
 *
 * While this class is currently used explicitly, its long-term purpose is to
 * be registered with a {@link org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry}
 * and be invoked on demand when the processing pipeline requests the
 * ASTSchemaSource representation.
 */
public final class TextToASTTransformer implements SchemaSourceTransformer<YangTextSchemaSource, ASTSchemaSource> {
    private static final Function<Exception, SchemaSourceTransformationException> MAPPER = new ExceptionMapper<SchemaSourceTransformationException>("Source transformation", SchemaSourceTransformationException.class) {
        @Override
        protected SchemaSourceTransformationException newWithCause(final String message, final Throwable cause) {
            return new SchemaSourceTransformationException(message, cause);
        }
    };
    private final ListeningExecutorService executor;

    TextToASTTransformer(final ListeningExecutorService executor) {
        this.executor = Preconditions.checkNotNull(executor);
    }

    @Override
    public Class<YangTextSchemaSource> getInputRepresentation() {
        return YangTextSchemaSource.class;
    }

    @Override
    public Class<ASTSchemaSource> getOutputRepresentation() {
        return ASTSchemaSource.class;
    }

    @Override
    public CheckedFuture<ASTSchemaSource, SchemaSourceTransformationException> transformSchemaSource(final YangTextSchemaSource source) {
        return Futures.makeChecked(executor.submit(new Callable<ASTSchemaSource>() {
            @Override
            public ASTSchemaSource call() throws IOException, YangSyntaxErrorException {
                try (InputStream is = source.openStream()) {
                    final YangContext ctx = YangParserImpl.parseYangSource(is);
                    return ASTSchemaSource.create(source.getIdentifier().getName(), ctx);
                }
            }
        }), MAPPER);
    }

    @Override
    public int getCost() {
        // We perform a direct translation, so the cost is 1.
        return 1;
    }
}
