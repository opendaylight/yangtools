/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc6020.repo;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.InputStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementLexer;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser.StatementContext;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.impl.YangStatementParserListenerImpl;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;

/**
 * This class represents implementation of StatementStreamSource in order to emit YANG statements using supplied
 * StatementWriter.
 *
 * @author Robert Varga
 */
@Beta
public final class YangStatementStreamSource implements Identifiable<SourceIdentifier>, StatementStreamSource {
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

    private final YangStatementParserListenerImpl yangStatementModelParser;
    private final SourceIdentifier identifier;
    private final StatementContext context;

    private YangStatementStreamSource(final SourceIdentifier identifier, final YangStatementParserListenerImpl parser,
            final StatementContext context) {
        this.identifier = Preconditions.checkNotNull(identifier);
        this.yangStatementModelParser = Preconditions.checkNotNull(parser);
        this.context = Preconditions.checkNotNull(context);
    }

    public static YangStatementStreamSource create(final YangTextSchemaSource source) throws IOException,
            YangSyntaxErrorException {
        final StatementContext context;
        try (final InputStream stream = source.openStream()) {
            context = parseYangSource(stream);
        }

        final String sourceName = source.getSymbolicName().orElse(null);
        final YangStatementParserListenerImpl parser = new YangStatementParserListenerImpl(sourceName);
        return new YangStatementStreamSource(source.getIdentifier(), parser, context);
    }

    public static YangStatementStreamSource create(final SourceIdentifier identifier, final StatementContext context,
        final String symbolicName) {
        return new YangStatementStreamSource(identifier, new YangStatementParserListenerImpl(symbolicName), context);
    }

    @Override
    public void writePreLinkage(final StatementWriter writer, final QNameToStatementDefinition stmtDef) {
        yangStatementModelParser.setAttributes(writer, stmtDef);
        ParseTreeWalker.DEFAULT.walk(yangStatementModelParser, context);
    }

    @Override
    public void writeLinkage(final StatementWriter writer, final QNameToStatementDefinition stmtDef,
            final PrefixToModule preLinkagePrefixes) {
        writeLinkage(writer, stmtDef, preLinkagePrefixes, YangVersion.VERSION_1);
    }

    @Override
    public void writeLinkage(final StatementWriter writer, final QNameToStatementDefinition stmtDef,
            final PrefixToModule preLinkagePrefixes, final YangVersion yangVersion) {
        yangStatementModelParser.setAttributes(writer, stmtDef, preLinkagePrefixes, yangVersion);
        ParseTreeWalker.DEFAULT.walk(yangStatementModelParser, context);
    }

    @Override
    public void writeLinkageAndStatementDefinitions(final StatementWriter writer,
            final QNameToStatementDefinition stmtDef, final PrefixToModule prefixes) {
        writeLinkageAndStatementDefinitions(writer, stmtDef, prefixes, YangVersion.VERSION_1);
    }

    @Override
    public void writeLinkageAndStatementDefinitions(final StatementWriter writer,
            final QNameToStatementDefinition stmtDef, final PrefixToModule prefixes, final YangVersion yangVersion) {
        yangStatementModelParser.setAttributes(writer, stmtDef, prefixes, yangVersion);
        ParseTreeWalker.DEFAULT.walk(yangStatementModelParser, context);
    }

    @Override
    public void writeFull(final StatementWriter writer, final QNameToStatementDefinition stmtDef,
            final PrefixToModule prefixes) {
        writeFull(writer, stmtDef, prefixes, YangVersion.VERSION_1);
    }

    @Override
    public void writeFull(final StatementWriter writer, final QNameToStatementDefinition stmtDef,
            final PrefixToModule prefixes, final YangVersion yangVersion) {
        yangStatementModelParser.setAttributes(writer, stmtDef, prefixes, yangVersion);
        ParseTreeWalker.DEFAULT.walk(yangStatementModelParser, context);
    }

    @Override
    public SourceIdentifier getIdentifier() {
        return identifier;
    }

    public ParserRuleContext getYangAST() {
        return context;
    }

    /**
     * @deprecated Provided for migration purposes only. Do not use.
     */
    @Deprecated
    public static StatementContext parseYangSource(final InputStream stream) throws IOException,
            YangSyntaxErrorException {
        final YangStatementLexer lexer = new YangStatementLexer(CharStreams.fromStream(stream));
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final YangStatementParser parser = new YangStatementParser(tokens);
        //disconnect from console error output
        parser.removeErrorListeners();

        final YangErrorListener errorListener = new YangErrorListener();
        parser.addErrorListener(errorListener);

        final StatementContext result = parser.statement();
        errorListener.validate();

        // Walk the resulting tree and replace each children with an immutable list, lowering memory requirements
        // and making sure the resulting tree will not get accidentally modified. An alternative would be to use
        // org.antlr.v4.runtime.Parser.TrimToSizeListener, but that does not make the tree immutable.
        ParseTreeWalker.DEFAULT.walk(MAKE_IMMUTABLE_LISTENER, result);

        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("identifier", getIdentifier()).toString();
    }
}
