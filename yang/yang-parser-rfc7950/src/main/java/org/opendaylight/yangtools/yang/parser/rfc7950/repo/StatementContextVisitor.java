/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import org.antlr.v4.runtime.tree.ParseTree;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser.ArgumentContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser.KeywordContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser.StatementContext;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.DeclarationInTextSource;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;

abstract class StatementContextVisitor {
    static final class Loose extends StatementContextVisitor {
        Loose(final String sourceName, final StatementWriter writer,
            final QNameToStatementDefinition stmtDef, final PrefixToModule prefixes, final YangVersion yangVersion) {
            super(sourceName, writer, stmtDef, prefixes, yangVersion);
        }

        Loose(final String sourceName, final StatementWriter writer, final QNameToStatementDefinition stmtDef) {
            this(sourceName, writer, stmtDef, null, YangVersion.VERSION_1);
        }
    }

    static final class Strict extends StatementContextVisitor {
        Strict(final String sourceName, final StatementWriter writer,
            final QNameToStatementDefinition stmtDef, final PrefixToModule prefixes, final YangVersion yangVersion) {
            super(sourceName, writer, stmtDef, prefixes, yangVersion);
        }

        @Override
        QName getValidStatementDefinition(final String keywordText, final StatementSourceReference ref) {
            return SourceException.throwIfNull(super.getValidStatementDefinition(keywordText, ref), ref,
                "%s is not a YANG statement or use of extension.", keywordText);
        }
    }

    private final QNameToStatementDefinition stmtDef;
    private final StatementWriter writer;
    private final YangVersion yangVersion;
    private final PrefixToModule prefixes;
    private final String sourceName;

    private StatementContextVisitor(final String sourceName, final StatementWriter writer,
            final QNameToStatementDefinition stmtDef, final PrefixToModule prefixes, final YangVersion yangVersion) {
        this.writer = requireNonNull(writer);
        this.stmtDef = requireNonNull(stmtDef);
        this.yangVersion = requireNonNull(yangVersion);
        this.sourceName = sourceName;
        this.prefixes = prefixes;
    }

    void visit(final StatementContext context) {
        processStatement(0, context);
    }

    /**
     * Based on identifier read from source and collections of relevant prefixes and statement definitions mappings
     * provided for actual phase, method resolves and returns valid QName for declared statement to be written.
     * This applies to any declared statement, including unknown statements.
     *
     * @param prefixes collection of all relevant prefix mappings supplied for actual parsing phase
     * @param stmtDef collection of all relevant statement definition mappings provided for actual parsing phase
     * @param keywordText statement keyword text to parse from source
     * @param ref Source reference
     * @return valid QName for declared statement to be written, or null
     */
    QName getValidStatementDefinition(final String keywordText, final StatementSourceReference ref) {
        final int firstColon = keywordText.indexOf(':');
        if (firstColon == -1) {
            final StatementDefinition statementDefinition = stmtDef.get(
                QName.create(YangConstants.RFC6020_YIN_NAMESPACE, keywordText));
            return statementDefinition != null ? statementDefinition.getStatementName() : null;
        }

        SourceException.throwIf(firstColon == keywordText.length() - 1
                || keywordText.indexOf(':', firstColon + 1) != -1, ref, "Malformed statement '%s'", keywordText);

        if (prefixes == null) {
            // No prefixes to look up from
            return null;
        }

        final String prefix = keywordText.substring(0, firstColon);
        final QNameModule qNameModule = prefixes.get(prefix);
        if (qNameModule == null) {
            // Failed to look the namespace
            return null;
        }

        final String localName = keywordText.substring(firstColon + 1);
        final StatementDefinition foundStmtDef;
        if (prefixes.isPreLinkageMap()) {
            foundStmtDef = stmtDef.getByNamespaceAndLocalName(qNameModule.getNamespace(), localName);
        } else {
            foundStmtDef = stmtDef.get(QName.create(qNameModule, localName));
        }

        return foundStmtDef != null ? foundStmtDef.getStatementName() : null;
    }

    private void processStatement(final int myOffset, final StatementContext ctx) {
        final String keywordTxt = verifyNotNull(ctx.getChild(KeywordContext.class, 0)).getText();
        final StatementSourceReference ref = DeclarationInTextSource.atPosition(sourceName, ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine());
        final QName def = getValidStatementDefinition(keywordTxt, ref);
        if (def == null) {
            return;
        }

        final ArgumentContext argumentCtx = ctx.getChild(ArgumentContext.class, 0);
        final String argument = argumentCtx == null ? null
                : ArgumentContextUtils.stringFromStringContext(argumentCtx, yangVersion, ref);
        writer.startStatement(myOffset, def, argument, ref);

        if (ctx.children != null) {
            int childOffset = 0;
            for (ParseTree s : ctx.children) {
                if (s instanceof StatementContext) {
                    processStatement(childOffset++, (StatementContext) s);
                }
            }
        }

        writer.endStatement(ref);
    }
}
