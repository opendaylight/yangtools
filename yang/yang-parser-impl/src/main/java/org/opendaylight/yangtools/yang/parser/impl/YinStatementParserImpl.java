/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.impl;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.concurrent.Immutable;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.source.DeclarationInTextSource;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Immutable
public class YinStatementParserImpl {

    private StatementWriter writer;
    private QNameToStatementDefinition stmtDef;
    private PrefixToModule prefixes;
    private List<String> toBeSkipped = new ArrayList<>();
    private String sourceName;
    private static final Logger LOG = LoggerFactory.getLogger(YinStatementParserImpl.class);

    public YinStatementParserImpl(String sourceName) {
        this.sourceName = sourceName;
    }

    public void setAttributes(StatementWriter writer, QNameToStatementDefinition stmtDef) {
        this.writer = writer;
        this.stmtDef = stmtDef;
    }

    public void setAttributes(StatementWriter writer, QNameToStatementDefinition stmtDef, PrefixToModule prefixes) {
        this.writer = writer;
        this.stmtDef = stmtDef;
        this.prefixes = prefixes;
    }

    public void walk(XMLStreamReader inputReader) {
        //TODO: refactor me
        boolean action = true;
        QName identifier;
        StatementSourceReference ref;

        try {
            while (inputReader.hasNext()) {
                inputReader.next();
                if (inputReader.hasName() && inputReader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    ref = DeclarationInTextSource.atPosition(sourceName, inputReader.getLocation().getLineNumber(),
                            inputReader.getLocation().getColumnNumber());
                    identifier = new QName(YangConstants.RFC6020_YIN_NAMESPACE, inputReader.getLocalName());
                    if (stmtDef != null && Utils.isValidStatementDefinition(prefixes, stmtDef, identifier) &&
                            toBeSkipped.isEmpty()) {
                        writer.startStatement(identifier, ref);
                    } else {
                        if (writer.getPhase().equals(ModelProcessingPhase.FULL_DECLARATION)) {
                            throw new IllegalArgumentException(identifier.getLocalName() + " is not a YIN statement " +
                                    "or use of extension. Source: " + ref);
                        } else {
                            action = false;
                            toBeSkipped.add(inputReader.getLocalName());
                        }
                    }
                    if (action) {
                        argumentValue(inputReader, ref);
                    } else {
                        action = true;
                    }
                }
                if (inputReader.hasName() && inputReader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                    String statementName = inputReader.getLocalName();
                    identifier = new QName(YangConstants.RFC6020_YIN_NAMESPACE, statementName);
                    ref = DeclarationInTextSource.atPosition(sourceName, inputReader.getLocation().getLineNumber(),
                            inputReader.getLocation().getColumnNumber());
                    if (stmtDef != null && Utils.isValidStatementDefinition(prefixes, stmtDef, identifier) &&
                            toBeSkipped.isEmpty()) {
                        writer.endStatement(ref);
                    }

                    if (toBeSkipped.contains(statementName)) {
                        toBeSkipped.remove(statementName);
                    }
                }
            }
        } catch (XMLStreamException e) {
            LOG.warn(e.getMessage(), e);
        }
    }

    private void startStatement(XMLStreamReader inputReader, StatementSourceReference ref) {
        //TODO: move code here from walk()
    }

    private void argumentValue(XMLStreamReader inputReader, StatementSourceReference ref) {
        String namespace = null;
        if (inputReader.getAttributeValue(namespace, "name") != null) {
            writer.argumentValue(inputReader.getAttributeValue(namespace, "name"), ref);
        } else if (inputReader.getAttributeValue(namespace, "value") != null) {
            writer.argumentValue(inputReader.getAttributeValue(namespace, "value"), ref);
        } else if (inputReader.getAttributeValue(namespace, "text") != null) {
            writer.argumentValue(inputReader.getAttributeValue(namespace, "text"), ref);
        } else if (inputReader.getAttributeValue(namespace, "uri") != null) {
            writer.argumentValue(inputReader.getAttributeValue(namespace, "uri"), ref);
        } else if (inputReader.getAttributeValue(namespace, "date") != null) {
            writer.argumentValue(inputReader.getAttributeValue(namespace, "date"), ref);
        } else if (inputReader.getAttributeValue(namespace, "target-node") != null) {
            writer.argumentValue(inputReader.getAttributeValue(namespace, "target-node"), ref);
        } else if (inputReader.getAttributeValue(namespace, "module") != null) {
            writer.argumentValue(inputReader.getAttributeValue(namespace, "module"), ref);
        } else if (inputReader.getAttributeValue(namespace, "condition") != null) {
            writer.argumentValue(inputReader.getAttributeValue(namespace, "condition"), ref);
        } else if (inputReader.getAttributeValue(namespace, "tag") != null) {
            writer.argumentValue(inputReader.getAttributeValue(namespace, "tag"), ref);
        }
    }

    private void endStatement(StatementSourceReference ref) {
        //TODO: move code here from walk()
    }
}