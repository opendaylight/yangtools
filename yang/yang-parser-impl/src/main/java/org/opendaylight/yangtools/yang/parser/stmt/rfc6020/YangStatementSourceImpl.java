/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementLexer;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.impl.YangStatementParserListenerImpl;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
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

    private YangStatementParserListenerImpl yangStatementModelParser;
    private YangStatementParser.StatementContext statementContext;
    private ParseTreeWalker walker;
    private String sourceName;
    private static final Logger LOG = LoggerFactory.getLogger(YangStatementSourceImpl.class);

    public YangStatementSourceImpl(String fileName, boolean isAbsolute) {
        try {
            statementContext = parseYangSource(loadFile(fileName, isAbsolute));
            walker = new ParseTreeWalker();
            yangStatementModelParser = new YangStatementParserListenerImpl(fileName);
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
        }
    }

    public YangStatementSourceImpl(InputStream inputStream) {
        try {
            statementContext = parseYangSource(inputStream);
            walker = new ParseTreeWalker();
            yangStatementModelParser = new YangStatementParserListenerImpl(sourceName);
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
        }
    }

    @Override
    public void writeLinkage(StatementWriter writer, QNameToStatementDefinition stmtDef) throws SourceException {
        yangStatementModelParser.setAttributes(writer, stmtDef);
        walker.walk(yangStatementModelParser, statementContext);
    }

    @Override
    public void writeLinkageAndStatementDefinitions(StatementWriter writer, QNameToStatementDefinition stmtDef, PrefixToModule prefixes) throws SourceException {
        yangStatementModelParser.setAttributes(writer, stmtDef, prefixes);
        walker.walk(yangStatementModelParser, statementContext);
    }

    @Override
    public void writeFull(StatementWriter writer, QNameToStatementDefinition stmtDef, PrefixToModule prefixes) throws SourceException {
        yangStatementModelParser.setAttributes(writer, stmtDef, prefixes);
        walker.walk(yangStatementModelParser, statementContext);
    }

    private NamedFileInputStream loadFile(String fileName, boolean isAbsolute) throws URISyntaxException,
            FileNotFoundException {
        return isAbsolute ? new NamedFileInputStream(new File(fileName), fileName) : new NamedFileInputStream(new File
                (getClass().getResource(fileName).toURI()), fileName);
    }

    private YangStatementParser.StatementContext parseYangSource(final InputStream stream) throws IOException,
            YangSyntaxErrorException {
        final YangStatementLexer lexer = new YangStatementLexer(new ANTLRInputStream(stream));
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final YangStatementParser parser = new YangStatementParser(tokens);
        sourceName = parser.getSourceName();
//        if (sourceName == null && stream instanceof NamedFileInputStream) {
//            sourceName = ((NamedFileInputStream) stream).getFileDestination();
//        }
        if (sourceName == null) {
            sourceName = stream.toString();
        }
        System.out.println("stream: " + stream);
        System.out.println("sourceName: " + sourceName);
        return parser.statement();
    }
}
