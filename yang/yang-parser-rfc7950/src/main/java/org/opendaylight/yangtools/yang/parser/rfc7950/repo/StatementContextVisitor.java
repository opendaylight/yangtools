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

import java.util.Optional;
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
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter.ResumedStatement;

class StatementContextVisitor {
    private final QNameToStatementDefinition stmtDef;
    private final StatementWriter writer;
    private final YangVersion yangVersion;
    private final PrefixToModule prefixes;
    private final String sourceName;

    StatementContextVisitor(final String sourceName, final StatementWriter writer,
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
            final StatementDefinition def = resolveStatement(YangConstants.RFC6020_YIN_MODULE, keywordText);
            return def != null ? def.getStatementName() : null;
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
        final StatementDefinition foundStmtDef = resolveStatement(qNameModule, localName);
        return foundStmtDef != null ? foundStmtDef.getStatementName() : null;
    }

    StatementDefinition resolveStatement(final QNameModule module, final String localName) {
        return stmtDef.get(QName.create(module, localName));
    }

    private boolean processStatement(final int myOffset, final StatementContext ctx) {
        final Optional<? extends ResumedStatement> optResumed = writer.resumeStatement(myOffset);
        final StatementSourceReference ref;
        if (optResumed.isPresent()) {
            final ResumedStatement resumed = optResumed.get();
            if (resumed.isFullyDefined()) {
                return true;
            }

            ref = resumed.getSourceReference();
        } else {
            final String keywordTxt = verifyNotNull(ctx.getChild(KeywordContext.class, 0)).getText();
            ref = DeclarationInTextSource.atPosition(sourceName, ctx.getStart().getLine(),
                ctx.getStart().getCharPositionInLine());
            final QName def = getValidStatementDefinition(keywordTxt, ref);
            if (def == null) {
                return false;
            }

            final ArgumentContext argumentCtx = ctx.getChild(ArgumentContext.class, 0);
            final String argument = argumentCtx == null ? null
                    : ArgumentContextUtils.stringFromStringContext(argumentCtx, yangVersion, ref);
            writer.startStatement(myOffset, def, argument, ref);
        }

        int childOffset = 0;
        boolean fullyDefined = true;
        if (ctx.children != null) {
            for (ParseTree s : ctx.children) {
                if (s instanceof StatementContext) {
                    if (!processStatement(childOffset++, (StatementContext) s)) {
                        fullyDefined = false;
                    }
                }
            }
        }

        writer.storeStatement(childOffset, fullyDefined);
        writer.endStatement(ref);
        return fullyDefined;
    }
}
