/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static java.util.Objects.requireNonNull;

import java.text.ParseException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.ir.IRKeyword;
import org.opendaylight.yangtools.yang.ir.IRKeyword.Qualified;
import org.opendaylight.yangtools.yang.ir.IRKeyword.Unqualified;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.ir.StringEscaping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.spi.meta.StatementDeclarations;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixResolver;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;

class StatementContextVisitor {
    private final @NonNull QNameToStatementDefinition stmtDef;
    private final @NonNull StringEscaping escaping;
    private final PrefixResolver prefixes;
    private final @NonNull StatementWriter writer;
    private final String sourceName;

    StatementContextVisitor(final String sourceName, final StatementWriter writer,
            final QNameToStatementDefinition stmtDef, final PrefixResolver prefixes, final YangVersion yangVersion) {
        this.writer = requireNonNull(writer);
        this.stmtDef = requireNonNull(stmtDef);
        this.sourceName = sourceName;
        this.prefixes = prefixes;
        escaping = switch (yangVersion) {
            case VERSION_1 -> StringEscaping.RFC6020;
            case VERSION_1_1 -> StringEscaping.RFC7950;
        };
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
    @Nullable QName getValidStatementDefinition(final @NonNull IRKeyword keyword,
            final @NonNull StatementSourceReference ref) {
        return switch (keyword) {
            case Qualified qualified -> getValidStatementDefinition(qualified, ref);
            case Unqualified unqualified -> {
                final var def = stmtDef.get(QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, unqualified.identifier()));
                yield def != null ? def.getStatementName() : null;
            }
        };
    }

    private @Nullable QName getValidStatementDefinition(final @NonNull Qualified keyword,
            final @NonNull StatementSourceReference ref) {
        if (prefixes == null) {
            // No prefixes to look up from
            return null;
        }

        final var module = prefixes.resolvePrefix(keyword.prefix());
        if (module == null) {
            // Failed to look the namespace
            return null;
        }

        final var foundStmtDef = resolveStatement(module, keyword.identifier());
        return foundStmtDef != null ? foundStmtDef.getStatementName() : null;
    }

    StatementDefinition resolveStatement(final @NonNull QNameModule module, final @NonNull String localName) {
        return stmtDef.get(QName.unsafeOf(module, localName));
    }

    // Normal entry point, checks for potential resume
    private boolean processStatement(final int myOffset, final IRStatement stmt) {
        final var optResumed = writer.resumeStatement(myOffset);
        if (optResumed.isPresent()) {
            final var resumed = optResumed.orElseThrow();
            return resumed.isFullyDefined() || doProcessStatement(stmt, resumed.getSourceReference());
        }
        return processNewStatement(myOffset, stmt);
    }

    // Slow-path allocation of a new statement
    private boolean processNewStatement(final int myOffset, final IRStatement stmt) {
        final var ref = StatementDeclarations.inText(sourceName, stmt.startLine(), stmt.startColumn() + 1);
        final var def = getValidStatementDefinition(stmt.keyword(), ref);
        if (def == null) {
            return false;
        }

        final var argumentCtx = stmt.argument();
        final String argument;
        if (argumentCtx != null) {
            try {
                argument = argumentCtx.asString(escaping);
            } catch (ParseException e) {
                throw new SourceException(e.getMessage(), ref, e);
            }
        } else {
            argument = null;
        }

        writer.startStatement(myOffset, def, argument, ref);
        return doProcessStatement(stmt, ref);
    }

    // Actual processing
    @NonNullByDefault
    private boolean doProcessStatement(final IRStatement stmt, final StatementSourceReference ref) {
        int childOffset = 0;
        boolean fullyDefined = true;
        for (var substatement : stmt.statements()) {
            if (!processStatement(childOffset++, substatement)) {
                fullyDefined = false;
            }
        }

        writer.storeStatement(childOffset, fullyDefined);
        writer.endStatement(ref);
        return fullyDefined;
    }
}
