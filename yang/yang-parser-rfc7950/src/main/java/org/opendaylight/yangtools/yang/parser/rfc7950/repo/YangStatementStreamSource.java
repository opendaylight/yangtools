/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.io.InputStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.opendaylight.yangtools.concepts.AbstractIdentifiable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementLexer;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementParser;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementParser.FileContext;
import org.opendaylight.yangtools.yang.parser.rfc7950.antlr.CompactYangStatementLexer;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRKeyword;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRStatement;
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
public final class YangStatementStreamSource extends AbstractIdentifiable<SourceIdentifier>
        implements StatementStreamSource {
    private final IRStatement rootStatement;
    private final String sourceName;

    private YangStatementStreamSource(final SourceIdentifier identifier,  final IRStatement rootStatement,
            final String sourceName) {
        super(identifier);
        this.rootStatement = requireNonNull(rootStatement);
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
        final IRStatement rootStatement;
        try (InputStream stream = source.openStream()) {
            rootStatement = parseYangSource(source.getIdentifier(), stream);
        }

        return new YangStatementStreamSource(source.getIdentifier(), rootStatement,
            source.getSymbolicName().orElse(null));
    }

    /**
     * Create a {@link YangStatementStreamSource} for a {@link ASTSchemaSource}.
     *
     * @param source YangTextSchemaSource, must not be null
     * @return A new {@link YangStatementStreamSource}
     */
    public static YangStatementStreamSource create(final ASTSchemaSource source) {
        return create(source.getIdentifier(), source.getRootStatement(), source.getSymbolicName().orElse(null));
    }

    public static YangStatementStreamSource create(final SourceIdentifier identifier, final IRStatement rootStatement,
            final String symbolicName) {
        return new YangStatementStreamSource(identifier, rootStatement, symbolicName);
    }

    @Override
    public void writePreLinkage(final StatementWriter writer, final QNameToStatementDefinition stmtDef) {
        new StatementContextVisitor(sourceName, writer, stmtDef, null, YangVersion.VERSION_1).visit(rootStatement);
    }

    @Override
    public void writeLinkage(final StatementWriter writer, final QNameToStatementDefinition stmtDef,
            final PrefixToModule preLinkagePrefixes, final YangVersion yangVersion) {
        new StatementContextVisitor(sourceName, writer, stmtDef, preLinkagePrefixes, yangVersion) {
            @Override
            StatementDefinition resolveStatement(final QNameModule module, final String localName) {
                return stmtDef.getByNamespaceAndLocalName(module.getNamespace(), localName);
            }
        }.visit(rootStatement);
    }

    @Override
    public void writeLinkageAndStatementDefinitions(final StatementWriter writer,
            final QNameToStatementDefinition stmtDef, final PrefixToModule prefixes, final YangVersion yangVersion) {
        new StatementContextVisitor(sourceName, writer, stmtDef, prefixes, yangVersion).visit(rootStatement);
    }

    @Override
    public void writeFull(final StatementWriter writer, final QNameToStatementDefinition stmtDef,
            final PrefixToModule prefixes, final YangVersion yangVersion) {
        new StatementContextVisitor(sourceName, writer, stmtDef, prefixes, yangVersion) {
            @Override
            QName getValidStatementDefinition(final IRKeyword keyword, final StatementSourceReference ref) {
                final QName ret = super.getValidStatementDefinition(keyword, ref);
                if (ret == null) {
                    throw new SourceException(ref, "%s is not a YANG statement or use of extension.",
                        keyword.asStringDeclaration());
                }
                return ret;
            }
        }.visit(rootStatement);
    }

    @Deprecated(forRemoval = true)
    public ParserRuleContext getYangAST() {
        return new IRParserRuleContext(rootStatement);
    }

    IRStatement rootStatement() {
        return rootStatement;
    }

    private static IRStatement parseYangSource(final SourceIdentifier source, final InputStream stream)
            throws IOException, YangSyntaxErrorException {
        final YangStatementLexer lexer = new CompactYangStatementLexer(CharStreams.fromStream(stream));
        final YangStatementParser parser = new YangStatementParser(new CommonTokenStream(lexer));
        // disconnect from console error output
        lexer.removeErrorListeners();
        parser.removeErrorListeners();

        final YangErrorListener errorListener = new YangErrorListener(source);
        lexer.addErrorListener(errorListener);
        parser.addErrorListener(errorListener);

        final FileContext result = parser.file();
        errorListener.validate();
        return IRStatement.forContext(result.statement());
    }
}
