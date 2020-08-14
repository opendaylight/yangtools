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

import com.google.common.base.VerifyException;
import java.util.Optional;
import org.antlr.v4.runtime.Token;
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
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter.ResumedStatement;

class StatementContextVisitor {
    private final QNameToStatementDefinition stmtDef;
    private final ArgumentContextUtils utils;
    private final StatementWriter writer;
    private final PrefixToModule prefixes;
    private final String sourceName;

    StatementContextVisitor(final String sourceName, final StatementWriter writer,
            final QNameToStatementDefinition stmtDef, final PrefixToModule prefixes, final YangVersion yangVersion) {
        this.writer = requireNonNull(writer);
        this.stmtDef = requireNonNull(stmtDef);
        this.utils = ArgumentContextUtils.forVersion(yangVersion);
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
     * @param keyword statement keyword text to parse from source
     * @param ref Source reference
     * @return valid QName for declared statement to be written, or null
     */
    QName getValidStatementDefinition(final KeywordContext keyword, final StatementSourceReference ref) {
        switch (keyword.getChildCount()) {
            case 1:
                final StatementDefinition def = stmtDef.get(QName.create(YangConstants.RFC6020_YIN_MODULE,
                    keyword.getChild(0).getText()));
                return def != null ? def.getStatementName() : null;
            case 3:
                if (prefixes == null) {
                    // No prefixes to look up from
                    return null;
                }

                final String prefix = keyword.getChild(0).getText();
                final QNameModule qNameModule = prefixes.get(prefix);
                if (qNameModule == null) {
                    // Failed to look the namespace
                    return null;
                }

                final String localName = keyword.getChild(2).getText();
                final StatementDefinition foundStmtDef = resolveStatement(qNameModule, localName);
                return foundStmtDef != null ? foundStmtDef.getStatementName() : null;
            default:
                throw new VerifyException("Unexpected shape of " + keyword);
        }
    }

    StatementDefinition resolveStatement(final QNameModule module, final String localName) {
        return stmtDef.get(QName.create(module, localName));
    }

    // Normal entry point, checks for potential resume
    private boolean processStatement(final int myOffset, final StatementContext ctx) {
        final Optional<? extends ResumedStatement> optResumed = writer.resumeStatement(myOffset);
        if (optResumed.isPresent()) {
            final ResumedStatement resumed = optResumed.get();
            return resumed.isFullyDefined() || doProcessStatement(ctx, resumed.getSourceReference());
        }
        return processNewStatement(myOffset, ctx);
    }

    // Slow-path allocation of a new statement
    private boolean processNewStatement(final int myOffset, final StatementContext ctx) {
        final Token start = ctx.getStart();
        final StatementSourceReference ref = DeclarationInTextSource.atPosition(sourceName, start.getLine(),
            start.getCharPositionInLine());
        final QName def = getValidStatementDefinition(verifyNotNull(ctx.getChild(KeywordContext.class, 0)), ref);
        if (def == null) {
            return false;
        }

        final ArgumentContext argumentCtx = ctx.getChild(ArgumentContext.class, 0);
        final String argument = argumentCtx == null ? null : utils.stringFromStringContext(argumentCtx, ref);
        writer.startStatement(myOffset, def, argument, ref);
        return doProcessStatement(ctx, ref);
    }

    // Actual processing
    private boolean doProcessStatement(final StatementContext ctx, final StatementSourceReference ref) {
        int childOffset = 0;
        boolean fullyDefined = true;
        if (ctx.children != null) {
            for (ParseTree s : ctx.children) {
                if (s instanceof StatementContext && !processStatement(childOffset++, (StatementContext) s)) {
                    fullyDefined = false;
                }
            }
        }

        writer.storeStatement(childOffset, fullyDefined);
        writer.endStatement(ref);
        return fullyDefined;
    }
}
