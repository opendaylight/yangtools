/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser.ArgumentContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser.KeywordContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser.StatementContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParserBaseVisitor;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.source.DeclarationInTextSource;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;

final class YangStatementParserVisitorImpl extends YangStatementParserBaseVisitor<Void> {
    private static final class Counter {
        private int value = 0;

        int getAndIncrement() {
            return value++;
        }
    }

    private final List<String> toBeSkipped = new ArrayList<>();
    private final Deque<Counter> counters = new ArrayDeque<>();
    private final String sourceName;

    private QNameToStatementDefinition stmtDef;
    private PrefixToModule prefixes;
    private StatementWriter writer;
    private YangVersion yangVersion;

    YangStatementParserVisitorImpl(final String sourceName) {
        this.sourceName = sourceName;
    }

    @SuppressWarnings("checkstyle:hiddenField")
    void walk(final StatementWriter writer, final QNameToStatementDefinition stmtDef, final StatementContext context) {
        walk(writer, stmtDef, null, YangVersion.VERSION_1, context);
    }

    @SuppressWarnings("checkstyle:hiddenField")
    void walk(final StatementWriter writer, final QNameToStatementDefinition stmtDef,
            final PrefixToModule prefixes, final YangVersion yangVersion, final StatementContext context) {
        // Verify & initialize state
        verify(toBeSkipped.isEmpty());
        verify(counters.isEmpty());
        this.writer = requireNonNull(writer);
        this.stmtDef = requireNonNull(stmtDef);
        this.prefixes = prefixes;
        this.yangVersion = requireNonNull(yangVersion);
        counters.push(new Counter());

        try {
            visitStatement(context);
        } finally {
            // Reset out state
            this.writer = null;
            this.stmtDef = null;
            this.prefixes = null;
            this.yangVersion = null;
            toBeSkipped.clear();
            counters.clear();
        }
    }

    @Override
    public Void visitStatement(final StatementContext ctx) {
        final StatementSourceReference ref = DeclarationInTextSource.atPosition(sourceName, ctx.getStart().getLine(),
            ctx.getStart().getCharPositionInLine());
        final String keywordTxt = verifyNotNull(ctx.getChild(KeywordContext.class, 0)).getText();
        final QName validStatementDefinition = getValidStatementDefinition(prefixes, stmtDef, keywordTxt, ref);

        final int childId = counters.peek().getAndIncrement();
        counters.push(new Counter());
        if (stmtDef == null || validStatementDefinition == null || !toBeSkipped.isEmpty()) {
            SourceException.throwIf(writer.getPhase() == ModelProcessingPhase.FULL_DECLARATION, ref,
                    "%s is not a YANG statement or use of extension.", keywordTxt);
            toBeSkipped.add(keywordTxt);
            return null;
        }

        final ArgumentContext argumentCtx = ctx.getChild(ArgumentContext.class, 0);
        final String argument = argumentCtx == null ? null
                : ArgumentContextUtils.stringFromStringContext(argumentCtx, yangVersion, ref);
        writer.startStatement(childId, validStatementDefinition, argument, ref);

        // Takes care of any children
        // TODO: with some guidance from startStatement() we can skip visiting already parsed children
        // FIXME: we should in-line this which will eliminate counters and toBeSkipped
        super.visitStatement(ctx);

        if (toBeSkipped.isEmpty()) {
            writer.endStatement(ref);
        }

        // No-op if the statement is not on the list
        toBeSkipped.remove(keywordTxt);
        counters.pop();
        return null;
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
    private static QName getValidStatementDefinition(final PrefixToModule prefixes,
            final QNameToStatementDefinition stmtDef, final String keywordText, final StatementSourceReference ref) {
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
}
