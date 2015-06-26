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
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.opendaylight.yangtools.yang.parser.impl.YinStatementParserImpl;
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
 * in order to emit YIN statements using supplied StatementWriter
 *
 */
public class YinStatementSourceImpl implements StatementStreamSource {

    private YinStatementParserImpl yinStatementModelParser;
    private InputStream inputStream;
    private String fileName;
    private boolean isAbsolute;
    private XMLStreamReader streamReader;
    private XMLInputFactory xmlInputFactory;
    private static final Logger LOG = LoggerFactory.getLogger(YinStatementSourceImpl.class);

    public YinStatementSourceImpl(final InputStream inputStream) {
        yinStatementModelParser = new YinStatementParserImpl(inputStream.toString());
        xmlInputFactory = XMLInputFactory.newInstance();
        this.inputStream = inputStream;
    }

    public YinStatementSourceImpl(final String fileName, final boolean isAbsolute) {
        yinStatementModelParser = new YinStatementParserImpl(fileName);
        xmlInputFactory = XMLInputFactory.newInstance();
        this.fileName = fileName;
        this.isAbsolute = isAbsolute;
    }

    @Override
    public void writeLinkage(StatementWriter writer, QNameToStatementDefinition stmtDef) throws SourceException {
        initializeReader();
        yinStatementModelParser.setAttributes(writer, stmtDef);
        yinStatementModelParser.walk(streamReader);
    }

    @Override
    public void writeLinkageAndStatementDefinitions(StatementWriter writer, QNameToStatementDefinition stmtDef,
                                                    PrefixToModule prefixes) throws SourceException {
        initializeReader();
        yinStatementModelParser.setAttributes(writer, stmtDef, prefixes);
        yinStatementModelParser.walk(streamReader);
    }

    @Override
    public void writeFull(StatementWriter writer, QNameToStatementDefinition stmtDef, PrefixToModule prefixes) throws
            SourceException {
        initializeReader();
        yinStatementModelParser.setAttributes(writer, stmtDef, prefixes);
        yinStatementModelParser.walk(streamReader);
        closeReader();
    }

    private void initializeReader() {
        try {
            if (fileName != null) {
                this.inputStream = loadFile(fileName, isAbsolute);
            }
            streamReader = xmlInputFactory.createXMLStreamReader(inputStream);

        } catch (XMLStreamException|FileNotFoundException|URISyntaxException e) {
            LOG.warn(e.getMessage(), e);
        }
    }

    private void closeReader() {
        try {
            inputStream.close();
            streamReader.close();
        } catch (XMLStreamException|IOException e) {
            LOG.warn(e.getMessage(), e);
        }
    }

    private InputStream loadFile(final String fileName, boolean isAbsolute) throws URISyntaxException,
            FileNotFoundException {
        return isAbsolute ? new NamedFileInputStream(new File(fileName), fileName) : new NamedFileInputStream(new File
                (getClass().getResource(fileName).toURI()), fileName);
    }
}