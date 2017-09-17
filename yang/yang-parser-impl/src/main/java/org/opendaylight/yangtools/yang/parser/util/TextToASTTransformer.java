/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.util;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import java.io.IOException;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;
import org.opendaylight.yangtools.yang.model.repo.util.SchemaSourceTransformer;
import org.opendaylight.yangtools.yang.parser.rfc6020.repo.YangStatementStreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link SchemaSourceTransformer} which handles translation of models from
 * {@link YangTextSchemaSource} representation into {@link ASTSchemaSource}.
 */
@Beta
public final class TextToASTTransformer extends SchemaSourceTransformer<YangTextSchemaSource, ASTSchemaSource> {

    /**
     * A text-to-AST Transformation.
     *
     * @deprecated Use {@link TextToASTTransformer#transformText(YangTextSchemaSource)} instead.
     */
    @Deprecated
    public static final class TextToASTTransformation implements Transformation<YangTextSchemaSource, ASTSchemaSource> {
        @Override
        public CheckedFuture<ASTSchemaSource, SchemaSourceException> apply(@Nonnull final YangTextSchemaSource input)
                throws IOException, YangSyntaxErrorException {
            final YangStatementStreamSource src = YangStatementStreamSource.create(input);

            final ParserRuleContext ctx = src.getYangAST();
            LOG.debug("Model {} parsed successfully", input);

            //:TODO missing validation (YangModelBasicValidationListener should be re-implemented to new parser)

            final Optional<String> opt = input.getSymbolicName();
            final ASTSchemaSource result = opt.isPresent()
                    ? ASTSchemaSource.create(opt.get(), input.getIdentifier(), ctx)
                            : ASTSchemaSource.create(input.getIdentifier(), ctx);

            return Futures.immediateCheckedFuture(result);
        }
    }

    /**
     * Singleton instance of {@link TextToASTTransformation}.
     *
     * @deprecated Use {@link TextToASTTransformer#transformText(YangTextSchemaSource)} instead.
     */
    @Deprecated
    public static final TextToASTTransformation TRANSFORMATION = new TextToASTTransformation();

    private static final Logger LOG = LoggerFactory.getLogger(TextToASTTransformer.class);

    private TextToASTTransformer(final SchemaRepository provider, final SchemaSourceRegistry consumer) {
        super(provider, YangTextSchemaSource.class, consumer, ASTSchemaSource.class, TRANSFORMATION);
    }

    public static TextToASTTransformer create(final SchemaRepository provider, final SchemaSourceRegistry consumer) {
        return new TextToASTTransformer(provider, consumer);
    }

    public static ASTSchemaSource transformText(final YangTextSchemaSource text) throws SchemaSourceException,
            IOException, YangSyntaxErrorException {
        return TRANSFORMATION.apply(text).checkedGet();
    }
}
