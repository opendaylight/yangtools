/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.io.Reader;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.opendaylight.yangtools.concepts.AbstractSimpleIdentifiable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.ir.IRKeyword;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementLexer;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementParser;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementParser.FileContext;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementParser.StatementContext;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.rfc7950.antlr.CompactYangStatementLexer;
import org.opendaylight.yangtools.yang.parser.rfc7950.antlr.IRSupport;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixResolver;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;

/**
 * This class represents implementation of StatementStreamSource in order to emit YANG statements using supplied
 * StatementWriter.
 */
@Beta
public final class YangStatementStreamSource extends AbstractSimpleIdentifiable<SourceIdentifier>
        implements StatementStreamSource {
    private final IRStatement rootStatement;
    private final String sourceName;

    private YangStatementStreamSource(final SourceIdentifier sourceId, final IRStatement rootStatement,
            final String sourceName) {
        super(sourceId);
        this.rootStatement = requireNonNull(rootStatement);
        this.sourceName = sourceName;
    }

    /**
     * Create a {@link YangStatementStreamSource} for a {@link YangTextSource}.
     *
     * @param source YangTextSchemaSource, must not be null
     * @return A new {@link YangStatementStreamSource}
     * @throws IOException When we fail to read the source
     * @throws YangSyntaxErrorException If the source fails basic parsing
     */
    public static YangStatementStreamSource create(final YangTextSource source)
            throws IOException, YangSyntaxErrorException {
        return new YangStatementStreamSource(source.sourceId(),
            IRSupport.createStatement(parseYangSource(source)), source.symbolicName());
    }

    /**
     * Create a {@link YangStatementStreamSource} for a {@link YangIRSource}.
     *
     * @param source YangTextSchemaSource, must not be null
     * @return A new {@link YangStatementStreamSource}
     * @throws NullPointerException if {@code source} is null
     */
    public static YangStatementStreamSource create(final YangIRSource source) {
        return create(source.sourceId(), source.statement(), source.symbolicName());
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
            final PrefixResolver preLinkagePrefixes, final YangVersion yangVersion) {
        new StatementContextVisitor(sourceName, writer, stmtDef, preLinkagePrefixes, yangVersion) {
            @Override
            StatementDefinition resolveStatement(final QNameModule module, final String localName) {
                return stmtDef.getByNamespaceAndLocalName(module.namespace(), localName);
            }
        }.visit(rootStatement);
    }

    @Override
    public void writeLinkageAndStatementDefinitions(final StatementWriter writer,
            final QNameToStatementDefinition stmtDef, final PrefixResolver prefixes, final YangVersion yangVersion) {
        new StatementContextVisitor(sourceName, writer, stmtDef, prefixes, yangVersion).visit(rootStatement);
    }

    @Override
    public void writeFull(final StatementWriter writer, final QNameToStatementDefinition stmtDef,
            final PrefixResolver prefixes, final YangVersion yangVersion) {
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

    IRStatement rootStatement() {
        return rootStatement;
    }

    static StatementContext parseYangSource(final YangTextSource source)
            throws IOException, YangSyntaxErrorException {
        try (var reader = source.openStream()) {
            return parseYangSource(source.sourceId(), reader);
        }
    }

    private static StatementContext parseYangSource(final SourceIdentifier sourceId, final Reader stream)
            throws IOException, YangSyntaxErrorException {
        final YangStatementLexer lexer = new CompactYangStatementLexer(CharStreams.fromReader(stream));
        final YangStatementParser parser = new YangStatementParser(new CommonTokenStream(lexer));
        // disconnect from console error output
        lexer.removeErrorListeners();
        parser.removeErrorListeners();

        final YangErrorListener errorListener = new YangErrorListener(sourceId);
        lexer.addErrorListener(errorListener);
        parser.addErrorListener(errorListener);

        final FileContext result = parser.file();
        errorListener.validate();
        return verifyNotNull(result.statement());
    }
}
