/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import java.io.IOException;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;
import org.opendaylight.yangtools.yang.model.repo.util.SchemaSourceTransformer;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementParser.StatementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link SchemaSourceTransformer} which handles translation of models from
 * {@link YangTextSchemaSource} representation into {@link ASTSchemaSource}.
 *
 * @deprecated Use {@link TextToIRTransformer} instead.
 */
@Beta
@Deprecated(forRemoval = true)
public final class TextToASTTransformer extends SchemaSourceTransformer<YangTextSchemaSource, ASTSchemaSource> {
    private static final Logger LOG = LoggerFactory.getLogger(TextToASTTransformer.class);

    private static final ParseTreeListener MAKE_IMMUTABLE_LISTENER = new ParseTreeListener() {
        @Override
        public void enterEveryRule(final ParserRuleContext ctx) {
            // No-op
        }

        @Override
        public void exitEveryRule(final ParserRuleContext ctx) {
            ctx.children = ctx.children == null ? ImmutableList.of() : ImmutableList.copyOf(ctx.children);
        }

        @Override
        public void visitTerminal(final TerminalNode node) {
            // No-op
        }

        @Override
        public void visitErrorNode(final ErrorNode node) {
            // No-op
        }
    };

    private TextToASTTransformer(final SchemaRepository provider, final SchemaSourceRegistry consumer) {
        super(provider, YangTextSchemaSource.class, consumer, ASTSchemaSource.class,
            input -> Futures.immediateFuture(transformText(input)));
    }

    public static TextToASTTransformer create(final SchemaRepository provider, final SchemaSourceRegistry consumer) {
        return new TextToASTTransformer(provider, consumer);
    }

    public static ASTSchemaSource transformText(final YangTextSchemaSource text) throws SchemaSourceException,
            IOException, YangSyntaxErrorException {
        final StatementContext stmt = YangStatementStreamSource.parseYangSource(text);
        LOG.debug("Model {} parsed successfully", text);

        // Walk the resulting tree and replace each children with an immutable list, lowering memory requirements
        // and making sure the resulting tree will not get accidentally modified. An alternative would be to use
        // org.antlr.v4.runtime.Parser.TrimToSizeListener, but that does not make the tree immutable.
        ParseTreeWalker.DEFAULT.walk(MAKE_IMMUTABLE_LISTENER, stmt);

        // TODO: missing validation (YangModelBasicValidationListener should be re-implemented to new parser)

        return ASTSchemaSource.create(text.getIdentifier(), text.getSymbolicName().orElse(null), stmt);
    }
}
