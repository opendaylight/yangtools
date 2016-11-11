/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import com.google.common.base.Verify;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import javax.annotation.concurrent.Immutable;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser.ArgumentContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser.KeywordContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser.StatementContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParserBaseListener;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.source.DeclarationInTextSource;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

@Immutable
public class YangStatementParserListenerImpl extends YangStatementParserBaseListener {
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

    public YangStatementParserListenerImpl(final String sourceName) {
        this.sourceName = sourceName;
    }

    public void setAttributes(final StatementWriter writer, final QNameToStatementDefinition stmtDef) {
        this.writer = writer;
        this.stmtDef = stmtDef;
        initCounters();
    }

    public void setAttributes(final StatementWriter writer, final QNameToStatementDefinition stmtDef,
            final PrefixToModule prefixes) {
        this.writer = writer;
        this.stmtDef = stmtDef;
        this.prefixes = prefixes;
        initCounters();
    }

    private void initCounters() {
        counters.clear();
        counters.push(new Counter());
    }

    @Override
    public void enterStatement(final StatementContext ctx) {
        final StatementSourceReference ref = DeclarationInTextSource.atPosition(sourceName, ctx
            .getStart().getLine(), ctx.getStart().getCharPositionInLine());
        final String keywordTxt = Verify.verifyNotNull(ctx.getChild(KeywordContext.class, 0)).getText();
        final QName identifier = QName.create(YangConstants.RFC6020_YIN_MODULE, keywordTxt);
        final QName validStatementDefinition = Utils.getValidStatementDefinition(prefixes, stmtDef, identifier);

        final int childId = counters.peek().getAndIncrement();
        counters.push(new Counter());
        if (stmtDef == null || validStatementDefinition == null || !toBeSkipped.isEmpty()) {
            SourceException.throwIf(writer.getPhase() == ModelProcessingPhase.FULL_DECLARATION, ref,
                    "%s is not a YANG statement or use of extension.", keywordTxt);
            toBeSkipped.add(keywordTxt);
            return;
        }

        final ArgumentContext argumentCtx = ctx.getChild(ArgumentContext.class, 0);
        final String argument = argumentCtx != null ? Utils.stringFromStringContext(argumentCtx) : null;
        writer.startStatement(childId, validStatementDefinition, argument, ref);
    }

    @Override
    public void exitStatement(final StatementContext ctx) {
        final StatementSourceReference ref = DeclarationInTextSource.atPosition(
            sourceName, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());

        KeywordContext keyword = ctx.getChild(KeywordContext.class, 0);
        String statementName = keyword.getText();
        QName identifier = QName.create(YangConstants.RFC6020_YIN_MODULE, statementName);
        if (stmtDef != null && Utils.getValidStatementDefinition(prefixes, stmtDef, identifier) != null
                && toBeSkipped.isEmpty()) {
            writer.endStatement(ref);
        }

        // No-op if the statement is not on the list
        toBeSkipped.remove(statementName);
        counters.pop();
    }
}
