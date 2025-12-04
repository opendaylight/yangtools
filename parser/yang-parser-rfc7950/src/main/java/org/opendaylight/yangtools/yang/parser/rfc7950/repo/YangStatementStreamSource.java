/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.AbstractSimpleIdentifiable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.ir.IRKeyword;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.spi.meta.StatementDeclarations;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.parser.antlr.YangTextParser;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
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
        return new YangStatementStreamSource(source.sourceId(), YangTextParser.parseToIR(source),
            source.symbolicName());
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
    public void writeRoot(StatementWriter writer, QNameToStatementDefinition stmtDef, YangVersion version) {
        final var ref = StatementDeclarations.inText(sourceName, rootStatement.startLine(),
            rootStatement.startColumn() + 1);
        verify(rootStatement.keyword() instanceof IRKeyword.Unqualified);

        final var def = stmtDef.get(QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE,
            rootStatement.keyword().identifier()));
        if (def == null) {
            throw new SourceException(ref, "%s is not a YANG Module or Submodule.",
                this.getIdentifier());
        }
        final QName defQname = def.getStatementName();

        final var argumentCtx = rootStatement.argument();
        final var argument = argumentCtx == null ? null :
            ArgumentContextUtils.forVersion(version).stringFromStringContext(argumentCtx, ref);
        writer.startStatement(0, defQname, argument, ref);
        writer.storeStatement(rootStatement.statements().size(), false);
    }

    @Override
    public void writeLinkageAndStatementDefinitions(final StatementWriter writer,
            final QNameToStatementDefinition stmtDef, final PrefixResolver prefixes, final YangVersion yangVersion) {
        new StatementContextVisitor(sourceName, writer, stmtDef, prefixes, yangVersion)
            .skipRootAndVisit(rootStatement);
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

    @Override
    public @NonNull SourceInfo getSourceInfo() {
        return YangIRSourceInfoExtractor.forIR(rootStatement, getIdentifier());
    }

    IRStatement rootStatement() {
        return rootStatement;
    }
}
