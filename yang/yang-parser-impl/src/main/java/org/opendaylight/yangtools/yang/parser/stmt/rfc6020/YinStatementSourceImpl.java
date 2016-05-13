/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
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
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;
import org.opendaylight.yangtools.yang.parser.util.NamedFileInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents implementation of StatementStreamSource
 * in order to emit YIN statements using supplied StatementWriter.
 */
public class YinStatementSourceImpl implements StatementStreamSource {

    private static final Logger LOG = LoggerFactory.getLogger(YinStatementSourceImpl.class);
    private static XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();

    private final YinStatementParserImpl yinStatementModelParser;
    private XMLStreamReader streamReader;
    private InputStream inputStream;
    private String fileName;
    private boolean isAbsolute;

    // FIXME IO exception: input stream closed when called from StmtTestUtils parserseYinSources method
    public YinStatementSourceImpl(final InputStream inputStream) {
        yinStatementModelParser = new YinStatementParserImpl(inputStream.toString());
        this.inputStream = new BufferedInputStream(inputStream);
        this.inputStream.mark(Integer.MAX_VALUE);
    }

    public YinStatementSourceImpl(final String fileName, final boolean isAbsolute) {
        yinStatementModelParser = new YinStatementParserImpl(fileName);
        this.fileName = Preconditions.checkNotNull(fileName);
        this.isAbsolute = isAbsolute;
    }

    @Override
    public void writePreLinkage(StatementWriter writer, QNameToStatementDefinition stmtDef) {
        initializeReader();
        yinStatementModelParser.setAttributes(writer, stmtDef);
        yinStatementModelParser.walk(streamReader);
    }

    @Override
    public void writeLinkage(StatementWriter writer, QNameToStatementDefinition stmtDef, final PrefixToModule preLinkagePrefixes) {
        initializeReader();
        yinStatementModelParser.setAttributes(writer, stmtDef, preLinkagePrefixes);
        yinStatementModelParser.walk(streamReader);
    }

    @Override
    public void writeLinkageAndStatementDefinitions(StatementWriter writer, QNameToStatementDefinition stmtDef,
            PrefixToModule prefixes) {
        initializeReader();
        yinStatementModelParser.setAttributes(writer, stmtDef, prefixes);
        yinStatementModelParser.walk(streamReader);
    }

    @Override
    public void writeFull(StatementWriter writer, QNameToStatementDefinition stmtDef, PrefixToModule prefixes) {
        initializeReader();
        yinStatementModelParser.setAttributes(writer, stmtDef, prefixes);
        yinStatementModelParser.walk(streamReader);
        closeReader();
    }

    private void initializeReader() {
        try {
            if (fileName != null) {
                inputStream = loadFile(fileName, isAbsolute);
                streamReader = xmlInputFactory.createXMLStreamReader(inputStream);
            } else {
                inputStream.reset();
                ByteArrayInputStream bais = new ByteArrayInputStream(ByteStreams.toByteArray(inputStream));
                streamReader = xmlInputFactory.createXMLStreamReader(bais);
            }
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