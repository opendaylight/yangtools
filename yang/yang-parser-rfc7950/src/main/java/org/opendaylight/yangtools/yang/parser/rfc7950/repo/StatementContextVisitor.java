/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRArgument;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRKeyword;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRKeyword.Qualified;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRStatement;
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

    void visit(final IRStatement stmt) {
        processStatement(0, stmt);
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
    QName getValidStatementDefinition(final IRKeyword keyword, final StatementSourceReference ref) {
        if (keyword instanceof Qualified) {
            return getValidStatementDefinition((Qualified) keyword, ref);
        }
        final StatementDefinition def = stmtDef.get(QName.create(YangConstants.RFC6020_YIN_MODULE,
            keyword.identifier()));
        return def != null ? def.getStatementName() : null;
    }

    private QName getValidStatementDefinition(final Qualified keyword, final StatementSourceReference ref) {
        if (prefixes == null) {
            // No prefixes to look up from
            return null;
        }

        final QNameModule qNameModule = prefixes.get(keyword.prefix());
        if (qNameModule == null) {
            // Failed to look the namespace
            return null;
        }

        final StatementDefinition foundStmtDef = resolveStatement(qNameModule, keyword.identifier());
        return foundStmtDef != null ? foundStmtDef.getStatementName() : null;
    }

    StatementDefinition resolveStatement(final QNameModule module, final String localName) {
        return stmtDef.get(QName.create(module, localName));
    }

    // Normal entry point, checks for potential resume
    private boolean processStatement(final int myOffset, final IRStatement stmt) {
        final Optional<? extends ResumedStatement> optResumed = writer.resumeStatement(myOffset);
        if (optResumed.isPresent()) {
            final ResumedStatement resumed = optResumed.get();
            return resumed.isFullyDefined() || doProcessStatement(stmt, resumed.getSourceReference());
        }
        return processNewStatement(myOffset, stmt);
    }

    // Slow-path allocation of a new statement
    private boolean processNewStatement(final int myOffset, final IRStatement stmt) {
        final StatementSourceReference ref = DeclarationInTextSource.atPosition(sourceName, stmt.startLine(),
            stmt.startColumn());
        final QName def = getValidStatementDefinition(stmt.keyword(), ref);
        if (def == null) {
            return false;
        }

        final IRArgument argumentCtx = stmt.argument();
        final String argument = argumentCtx == null ? null : utils.stringFromStringContext(argumentCtx, ref);
        writer.startStatement(myOffset, def, argument, ref);
        return doProcessStatement(stmt, ref);
    }

    // Actual processing
    private boolean doProcessStatement(final IRStatement stmt, final StatementSourceReference ref) {
        int childOffset = 0;
        boolean fullyDefined = true;
        for (IRStatement substatement : stmt.statements()) {
            if (!processStatement(childOffset++, substatement)) {
                fullyDefined = false;
            }
        }

        writer.storeStatement(childOffset, fullyDefined);
        writer.endStatement(ref);
        return fullyDefined;
    }
}
