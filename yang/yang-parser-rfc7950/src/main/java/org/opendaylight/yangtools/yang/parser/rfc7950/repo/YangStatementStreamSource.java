/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;

/**
 * This class represents implementation of StatementStreamSource in order to emit YANG statements using supplied
 * StatementWriter.
 *
 * @author Robert Varga
 */
@Beta
public final class YangStatementStreamSource implements StatementStreamSource {
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

    private final SourceIdentifier identifier;
    private final StatementContext context;
    private final String sourceName;

    private YangStatementStreamSource(final SourceIdentifier identifier,  final StatementContext context,
            final String sourceName) {
        this.identifier = requireNonNull(identifier);
        this.context = requireNonNull(context);
        this.sourceName = sourceName;
    }

    /**
     * Create a {@link YangStatementStreamSource} for a {@link YangTextSchemaSource}.
     *
     * @param source YangTextSchemaSource, must not be null
     * @return A new {@link YangStatementStreamSource}
     * @throws IOException When we fail to read the source
     * @throws YangSyntaxErrorException If the source fails basic parsing
     */
    public static YangStatementStreamSource create(final YangTextSchemaSource source) throws IOException,
            YangSyntaxErrorException {
        final StatementContext context;
        try (InputStream stream = source.openStream()) {
            context = parseYangSource(source.getIdentifier(), stream);
        }

        return new YangStatementStreamSource(source.getIdentifier(), context, source.getSymbolicName().orElse(null));
    }

    /**
     * Create a {@link YangStatementStreamSource} for a {@link ASTSchemaSource}.
     *
     * @param source YangTextSchemaSource, must not be null
     * @return A new {@link YangStatementStreamSource}
     */
    public static YangStatementStreamSource create(final ASTSchemaSource source) {
        final ParserRuleContext ast = source.getAST();
        checkArgument(ast instanceof StatementContext,
                "Unsupported context class %s for source %s", ast.getClass(), source.getIdentifier());
        return create(source.getIdentifier(), (StatementContext) ast, source.getSymbolicName().orElse(null));
    }

    public static YangStatementStreamSource create(final SourceIdentifier identifier, final StatementContext context,
        final String symbolicName) {
        return new YangStatementStreamSource(identifier, context, symbolicName);
    }

    @Override
    public void writePreLinkage(final StatementWriter writer, final QNameToStatementDefinition stmtDef) {
        new StatementContextVisitor(sourceName, writer, stmtDef, null, YangVersion.VERSION_1).visit(context);
    }

    @Override
    public void writeLinkage(final StatementWriter writer, final QNameToStatementDefinition stmtDef,
            final PrefixToModule preLinkagePrefixes, final YangVersion yangVersion) {
        new StatementContextVisitor(sourceName, writer, stmtDef, preLinkagePrefixes, yangVersion) {
            @Override
            StatementDefinition resolveStatement(final QNameModule module, final String localName) {
                return stmtDef.getByNamespaceAndLocalName(module.getNamespace(), localName);
            }
        }.visit(context);
    }

    @Override
    public void writeLinkageAndStatementDefinitions(final StatementWriter writer,
            final QNameToStatementDefinition stmtDef, final PrefixToModule prefixes, final YangVersion yangVersion) {
        new StatementContextVisitor(sourceName, writer, stmtDef, prefixes, yangVersion).visit(context);
    }

    @Override
    public void writeFull(final StatementWriter writer, final QNameToStatementDefinition stmtDef,
            final PrefixToModule prefixes, final YangVersion yangVersion) {
        new StatementContextVisitor(sourceName, writer, stmtDef, prefixes, yangVersion) {
            @Override
            QName getValidStatementDefinition(final String keywordText, final StatementSourceReference ref) {
                return SourceException.throwIfNull(super.getValidStatementDefinition(keywordText, ref), ref,
                    "%s is not a YANG statement or use of extension.", keywordText);
            }
        }.visit(context);
    }

    @Override
    public SourceIdentifier getIdentifier() {
        return identifier;
    }

    public ParserRuleContext getYangAST() {
        return context;
    }

    private static StatementContext parseYangSource(final SourceIdentifier source, final InputStream stream)
            throws IOException, YangSyntaxErrorException {
        final YangStatementLexer lexer = new YangStatementLexer(CharStreams.fromStream(stream));
        final YangStatementParser parser = new YangStatementParser(new CommonTokenStream(lexer));
        // disconnect from console error output
        lexer.removeErrorListeners();
        parser.removeErrorListeners();

        final YangErrorListener errorListener = new YangErrorListener(source);
        lexer.addErrorListener(errorListener);
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
