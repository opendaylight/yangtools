/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Charsets;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import java.io.IOException;
import java.io.InputStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.YangContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;
import org.opendaylight.yangtools.yang.model.repo.util.SchemaSourceTransformer;
import org.opendaylight.yangtools.yang.parser.impl.YangModelBasicValidationListener;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link SchemaSourceTransformer} which handles translation of models from
 * {@link YangTextSchemaSource} representation into {@link ASTSchemaSource}.
 */
@Beta
public final class TextToASTTransformer extends SchemaSourceTransformer<YangTextSchemaSource, ASTSchemaSource> {
    public static final class TextToASTTransformation implements Transformation<YangTextSchemaSource, ASTSchemaSource> {
        @Override
        public CheckedFuture<ASTSchemaSource, SchemaSourceException> apply(final YangTextSchemaSource input) throws IOException, YangSyntaxErrorException {
            try (InputStream is = input.openStream()) {
                final YangContext ctx = YangParserImpl.parseYangSource(is);
                LOG.debug("Model {} parsed successfully", input);

                final ParseTreeWalker walker = new ParseTreeWalker();
                final YangModelBasicValidationListener validator = new YangModelBasicValidationListener();
                walker.walk(validator, ctx);
                LOG.debug("Model {} validated successfully", input);

                // Backwards compatibility
                final String text = input.asCharSource(Charsets.UTF_8).read();

                return Futures.immediateCheckedFuture(ASTSchemaSource.create(input.getIdentifier().getName(), ctx, text));
            }
        }
    }

    public static final TextToASTTransformation TRANSFORMATION = new TextToASTTransformation();
    private static final Logger LOG = LoggerFactory.getLogger(TextToASTTransformer.class);

    private TextToASTTransformer(final SchemaRepository provider, final SchemaSourceRegistry consumer) {
        super(provider, YangTextSchemaSource.class, consumer, ASTSchemaSource.class, TRANSFORMATION);
    }

    public static TextToASTTransformer create(final SchemaRepository provider, final SchemaSourceRegistry consumer) {
        return new TextToASTTransformer(provider, consumer);
    }
}
