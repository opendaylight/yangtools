/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementLexer;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser.StatementContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.impl.YangErrorListener;
import org.opendaylight.yangtools.yang.parser.impl.YangStatementParserListenerImpl;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;
import org.opendaylight.yangtools.yang.parser.util.NamedFileInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * This class represents implementation of StatementStreamSource
 * in order to emit YANG statements using supplied StatementWriter
 *
 */
public final class YangStatementSourceImpl implements StatementStreamSource {
    private static final Logger LOG = LoggerFactory.getLogger(YangStatementSourceImpl.class);

    private YangStatementParserListenerImpl yangStatementModelParser;
    private YangStatementParser.StatementContext statementContext;
    private String sourceName;

    public YangStatementSourceImpl(final String fileName, final boolean isAbsolute) {
        try {
            statementContext = parseYangSource(loadFile(fileName, isAbsolute));
            yangStatementModelParser = new YangStatementParserListenerImpl(sourceName);
        } catch (Exception e) {
            logError(e);
        }
    }

    public YangStatementSourceImpl(final InputStream inputStream) {
        try {
            statementContext = parseYangSource(inputStream);
            yangStatementModelParser = new YangStatementParserListenerImpl(sourceName);
        } catch (Exception e) {
            logError(e);
        }
    }

    public YangStatementSourceImpl(final SourceIdentifier identifier, final YangStatementParser.StatementContext statementContext) {
        try {
            this.statementContext = statementContext;
            this.sourceName = identifier.getName();
            yangStatementModelParser = new YangStatementParserListenerImpl(sourceName);
        } catch (Exception e) {
            logError(e);
        }
    }

    @Override
    public void writePreLinkage(final StatementWriter writer, final QNameToStatementDefinition stmtDef) {
        yangStatementModelParser.setAttributes(writer, stmtDef);
        ParseTreeWalker.DEFAULT.walk(yangStatementModelParser, statementContext);
    }

    @Override
    public void writeLinkage(final StatementWriter writer, final QNameToStatementDefinition stmtDef, final PrefixToModule preLinkagePrefixes) {
        yangStatementModelParser.setAttributes(writer, stmtDef, preLinkagePrefixes);
        ParseTreeWalker.DEFAULT.walk(yangStatementModelParser, statementContext);
    }

    @Override
    public void writeLinkageAndStatementDefinitions(final StatementWriter writer, final QNameToStatementDefinition stmtDef, final PrefixToModule prefixes) {
        yangStatementModelParser.setAttributes(writer, stmtDef, prefixes);
        ParseTreeWalker.DEFAULT.walk(yangStatementModelParser, statementContext);
    }

    @Override
    public void writeFull(final StatementWriter writer, final QNameToStatementDefinition stmtDef, final PrefixToModule prefixes) {
        yangStatementModelParser.setAttributes(writer, stmtDef, prefixes);
        ParseTreeWalker.DEFAULT.walk(yangStatementModelParser, statementContext);
    }

    private NamedFileInputStream loadFile(final String fileName, final boolean isAbsolute) throws URISyntaxException,
            IOException {
        //TODO: we need absolute path first!
        return isAbsolute ? new NamedFileInputStream(new File(fileName), fileName) : new NamedFileInputStream(new File
                (getClass().getResource(fileName).toURI()), fileName);
    }

    private YangStatementParser.StatementContext parseYangSource(final InputStream stream) throws IOException,
            YangSyntaxErrorException {
        final YangStatementLexer lexer = new YangStatementLexer(new ANTLRInputStream(stream));
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final YangStatementParser parser = new YangStatementParser(tokens);
        //disconnect from console error output
        parser.removeErrorListeners();

        final YangErrorListener errorListener = new YangErrorListener();
        parser.addErrorListener(errorListener);

        if (stream instanceof NamedFileInputStream) {
            sourceName = stream.toString();
        } else {
            sourceName = null;
        }

        final StatementContext result = parser.statement();
        errorListener.validate();

        return result;
    }

    public YangStatementParser.StatementContext getYangAST() {
        return statementContext;
    }

    @Override
    public String toString() {
        return sourceName;
    }

    private static void logError(final Exception e) {
        if (e instanceof YangSyntaxErrorException) {
            LOG.error(((YangSyntaxErrorException) e).getFormattedMessage(), e);
        } else {
            LOG.error(e.getMessage(), e);
        }
    }
}
