/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.base.Preconditions;
import java.io.File;
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

    private static final Logger LOG = LoggerFactory.getLogger(YinStatementSourceImpl.class);
    private static XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();

    private YinStatementParserImpl yinStatementModelParser;
    private XMLStreamReader streamReader;
    private InputStream inputStream;
    private String fileName;
    private boolean isAbsolute;

    public YinStatementSourceImpl(final InputStream inputStream) {
        yinStatementModelParser = new YinStatementParserImpl(inputStream.toString());
        this.inputStream = inputStream;
    }

    public YinStatementSourceImpl(final String fileName, final boolean isAbsolute) {
        yinStatementModelParser = new YinStatementParserImpl(fileName);
        this.fileName = Preconditions.checkNotNull(fileName);
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

        } catch (XMLStreamException e) {
            LOG.warn("Error while creating XMLStreamReader from input stream", e);
        } catch (URISyntaxException e) {
            LOG.warn("File name {} cannot be parsed as URI reference", fileName, e);
        } catch (IOException e) {
            LOG.warn("File {} cannot be found or read into string ", fileName, e);
        }
    }

    private void closeReader() {
        try {
            inputStream.close();
            streamReader.close();
        } catch (XMLStreamException e) {
            LOG.warn("Error occured while freeing associated resources", e);
        } catch (IOException e) {
            LOG.warn("I/O error occured during closing the input stream", e);
        }
    }

    private InputStream loadFile(final String fileName, boolean isAbsolute) throws URISyntaxException, IOException {
        final File file;
        if (isAbsolute) {
            file = new File(fileName);
        } else {
            file = new File(getClass().getResource(fileName).toURI());
        }

        return new NamedFileInputStream(file, fileName);
    }
}