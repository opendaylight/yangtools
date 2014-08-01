/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.YangContext;
import org.opendaylight.yangtools.util.concurrent.ExceptionMapper;
import org.opendaylight.yangtools.util.concurrent.ReflectiveExceptionMapper;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceListener;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;
import org.opendaylight.yangtools.yang.parser.impl.YangModelBasicValidationListener;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link SchemaSourceTransformer} which handles translation of models from
 * {@link YangTextSchemaSource} representation into {@link ASTSchemaSource}.
 *
 * While this class is currently used explicitly, its long-term purpose is to
 * be registered with a {@link org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry}
 * and be invoked on demand when the processing pipeline requests the
 * ASTSchemaSource representation.
 */
public final class TextToASTTransformer implements SchemaSourceListener, SchemaSourceProvider<ASTSchemaSource> {
    private static final ExceptionMapper<SchemaSourceException> MAPPER = ReflectiveExceptionMapper.create("Source transformation", SchemaSourceException.class);
    private static final Logger LOG = LoggerFactory.getLogger(TextToASTTransformer.class);

    private final SchemaRepository provider;
    private final SchemaSourceRegistry consumer;

    private TextToASTTransformer(final SchemaRepository provider, final SchemaSourceRegistry consumer) {
        this.provider = Preconditions.checkNotNull(provider);
        this.consumer = Preconditions.checkNotNull(consumer);
    }

    public static final TextToASTTransformer create(final SchemaRepository provider, final SchemaSourceRegistry consumer) {
        return new TextToASTTransformer(provider, consumer);
    }

    @Override
    public CheckedFuture<ASTSchemaSource, SchemaSourceException> getSource(final SourceIdentifier sourceIdentifier) {
        final CheckedFuture<YangTextSchemaSource, SchemaSourceException> f = provider.getSchemaSource(sourceIdentifier, YangTextSchemaSource.class);

        return Futures.makeChecked(Futures.transform(f, new AsyncFunction<YangTextSchemaSource, ASTSchemaSource>() {
            @Override
            public ListenableFuture<ASTSchemaSource> apply(final YangTextSchemaSource input) throws IOException, YangSyntaxErrorException {
                try (InputStream is = input.openStream()) {
                    final YangContext ctx = YangParserImpl.parseYangSource(is);
                    LOG.debug("Model {} parsed successfully", input);

                    final ParseTreeWalker walker = new ParseTreeWalker();
                    final YangModelBasicValidationListener validator = new YangModelBasicValidationListener();
                    walker.walk(validator, ctx);
                    LOG.debug("Model {} validated successfully", input);

                    // Backwards compatibility
                    final String text = CharStreams.toString(
                            CharStreams.newReaderSupplier(new InputSupplier<InputStream>() {
                                @Override
                                public InputStream getInput() throws IOException {
                                    return input.openStream();
                                }
                            }, Charsets.UTF_8));

                    return Futures.immediateFuture(ASTSchemaSource.create(input.getIdentifier().getName(), ctx, text));
                }
            }
        }), MAPPER);
    }

}
