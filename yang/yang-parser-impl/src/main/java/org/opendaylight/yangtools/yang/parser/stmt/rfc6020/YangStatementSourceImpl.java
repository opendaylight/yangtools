/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementLexer;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.impl.YangStatementParserListenerImpl;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

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
    private static final Logger LOG = LoggerFactory.getLogger(YangStatementSourceImpl.class);

    private static final StatementSourceReference REF = new StatementSourceReference() {

        @Override
        public StatementSource getStatementSource() {
            return StatementSource.DECLARATION;
        }
    };

    public YangStatementSourceImpl(String fileName) {
        try {
            statementContext = parseYangSource(loadFile(fileName));
            walker = new ParseTreeWalker();
            yangStatementModelParser = new YangStatementParserListenerImpl(REF);
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

    private FileInputStream loadFile(String fileName) throws URISyntaxException, FileNotFoundException {
        return new FileInputStream(new File(getClass().getResource(fileName).toURI()));
    }

    private YangStatementParser.StatementContext parseYangSource(final InputStream stream) throws IOException, YangSyntaxErrorException {
        final YangStatementLexer lexer = new YangStatementLexer(new ANTLRInputStream(stream));
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final YangStatementParser parser = new YangStatementParser(tokens);
        return parser.statement();
    }
}
