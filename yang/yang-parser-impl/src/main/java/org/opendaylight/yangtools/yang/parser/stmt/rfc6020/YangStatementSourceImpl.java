/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.base.Throwables;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser.StatementContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.impl.YangStatementParserListenerImpl;
import org.opendaylight.yangtools.yang.parser.rfc6020.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;
import org.opendaylight.yangtools.yang.parser.util.NamedFileInputStream;
import org.opendaylight.yangtools.yang.parser.util.NamedInputStream;

/**
 * This class represents implementation of StatementStreamSource in order to emit YANG statements using supplied
 * StatementWriter.
 *
 * @deprecated Use {@link YangStatementStreamSource} instead.
 */
@Deprecated
public final class YangStatementSourceImpl implements StatementStreamSource {
    private final YangStatementParserListenerImpl yangStatementModelParser;
    private final StatementContext statementContext;
    private final String sourceName;

    public YangStatementSourceImpl(final String fileName, final boolean isAbsolute) {
        try {
            final NamedFileInputStream is = loadFile(fileName, isAbsolute);
            sourceName = is.toString();
            statementContext = YangStatementStreamSource.parseYangSource(is);
            yangStatementModelParser = new YangStatementParserListenerImpl(sourceName);
        } catch (IOException | URISyntaxException | YangSyntaxErrorException e) {
            throw Throwables.propagate(e);
        }
    }

    public YangStatementSourceImpl(final InputStream inputStream) {
        try {
            sourceName = inputStream instanceof NamedInputStream ? inputStream.toString() : null;
            statementContext = YangStatementStreamSource.parseYangSource(inputStream);
            yangStatementModelParser = new YangStatementParserListenerImpl(sourceName);
        } catch (IOException | YangSyntaxErrorException e) {
            throw Throwables.propagate(e);
        }
    }

    public YangStatementSourceImpl(final SourceIdentifier identifier, final StatementContext statementContext) {
        this.statementContext = statementContext;
        this.sourceName = identifier.getName();
        yangStatementModelParser = new YangStatementParserListenerImpl(sourceName);
    }

    @Override
    public void writePreLinkage(final StatementWriter writer, final QNameToStatementDefinition stmtDef) {
        yangStatementModelParser.setAttributes(writer, stmtDef);
        ParseTreeWalker.DEFAULT.walk(yangStatementModelParser, statementContext);
    }

    @Override
    public void writeLinkage(final StatementWriter writer, final QNameToStatementDefinition stmtDef,
            final PrefixToModule preLinkagePrefixes) {
        yangStatementModelParser.setAttributes(writer, stmtDef, preLinkagePrefixes);
        ParseTreeWalker.DEFAULT.walk(yangStatementModelParser, statementContext);
    }

    @Override
    public void writeLinkageAndStatementDefinitions(final StatementWriter writer,
            final QNameToStatementDefinition stmtDef, final PrefixToModule prefixes) {
        yangStatementModelParser.setAttributes(writer, stmtDef, prefixes);
        ParseTreeWalker.DEFAULT.walk(yangStatementModelParser, statementContext);
    }

    @Override
    public void writeFull(final StatementWriter writer, final QNameToStatementDefinition stmtDef,
            final PrefixToModule prefixes) {
        yangStatementModelParser.setAttributes(writer, stmtDef, prefixes);
        ParseTreeWalker.DEFAULT.walk(yangStatementModelParser, statementContext);
    }

    private NamedFileInputStream loadFile(final String fileName, final boolean isAbsolute)
            throws URISyntaxException, IOException {
        //TODO: we need absolute path first!
        return isAbsolute ? new NamedFileInputStream(new File(fileName), fileName)
                : new NamedFileInputStream(new File(getClass().getResource(fileName).toURI()), fileName);
    }

    public StatementContext getYangAST() {
        return statementContext;
    }

    @Override
    public String toString() {
        return sourceName;
    }
}
