/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.concurrent.Immutable;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.source.DeclarationInTextSource;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Immutable
public class YinStatementParserImpl {

    private static final String DESCRIPTION = "description";
    private static final String CONTACT = "contact";
    private static final String REFERENCE = "reference";
    private static final String ERROR_MESSAGE = "error-message";
    private static final String ORGANIZATION = "organization";

    private static final String INPUT = "input";
    private static final String OUTPUT = "output";

    private static final Set<String> YIN_ELEMENTS = new HashSet<>();
    private static final Set<String> NO_ARGUMENT_ELEMENTS = new HashSet<>();

    static {
        YIN_ELEMENTS.add(DESCRIPTION);
        YIN_ELEMENTS.add(CONTACT);
        YIN_ELEMENTS.add(REFERENCE);
        YIN_ELEMENTS.add(ERROR_MESSAGE);
        YIN_ELEMENTS.add(ORGANIZATION);

        NO_ARGUMENT_ELEMENTS.add(INPUT);
        NO_ARGUMENT_ELEMENTS.add(OUTPUT);
    }


    private StatementWriter writer;
    private QNameToStatementDefinition stmtDef;
    private PrefixToModule prefixes;
    private List<String> toBeSkipped = new ArrayList<>();
    private String sourceName;
    private boolean isType = false;
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
        boolean action = true;
        QName identifier;
        StatementSourceReference ref;

        try {
            while (inputReader.hasNext()) {
                inputReader.next();
                if (inputReader.hasName() && inputReader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    ref = DeclarationInTextSource.atPosition(sourceName, inputReader.getLocation().getLineNumber(),
                            inputReader.getLocation().getColumnNumber());
                    identifier = new QName(YangConstants.RFC6020_YIN_NAMESPACE, getElementFullName(inputReader));
                    if (identifier.getLocalName().equals("text") && toBeSkipped.isEmpty()) {
                        argumentValue(inputReader, ref, true);
                    } else {
                        if (stmtDef != null && Utils.isValidStatementDefinition(prefixes, stmtDef, identifier) &&
                                toBeSkipped.isEmpty()) {
                            if (identifier.equals(Rfc6020Mapping.TYPE.getStatementName())) {
                                isType = true;
                            } else {
                                startStatement(identifier, ref);
                                if (isStatementWithYinElement(identifier)) {
                                    action = false;
                                }
                            }
                        } else {
                            if (writer.getPhase().equals(ModelProcessingPhase.FULL_DECLARATION)) {
                                throw new IllegalArgumentException(identifier.getLocalName() + " is not a YIN " +
                                        "statement or use of extension. Source: " + ref);
                            } else {
                                action = false;
                                toBeSkipped.add(inputReader.getLocalName());
                            }
                        }
                        if (isType) {
                            String argument = getAttributeValue(inputReader);
                            if (TypeUtils.isYangTypeBodyStmt(argument)) {
                                startStatement(new QName(YangConstants.RFC6020_YIN_NAMESPACE, argument), ref);
                            } else {
                                startStatement(new QName(YangConstants.RFC6020_YIN_NAMESPACE, Rfc6020Mapping
                                        .TYPE.getStatementName().getLocalName()), ref);
                            }
                            argumentValue(inputReader, ref, false);
                            isType = false;
                        } else if (action & isStatementWithArgument(identifier)) {
                            argumentValue(inputReader, ref, false);
                        } else {
                            action = true;
                        }
                    }
                }

                if (inputReader.hasName() && inputReader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                    String statementName = inputReader.getLocalName();
                    identifier = new QName(YangConstants.RFC6020_YIN_NAMESPACE, statementName);
                    ref = DeclarationInTextSource.atPosition(sourceName, inputReader.getLocation().getLineNumber(),
                            inputReader.getLocation().getColumnNumber());
                    if ((stmtDef != null && Utils.isValidStatementDefinition(prefixes, stmtDef, identifier) &&
                            toBeSkipped.isEmpty())) {
                        endStatement(ref);
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

    private void startStatement(QName identifier, StatementSourceReference ref) {
        writer.startStatement(identifier, ref);
    }

    private void argumentValue(XMLStreamReader inputReader, StatementSourceReference ref, boolean yinElement) {
        if (yinElement) {
            try {
                writer.argumentValue(inputReader.getElementText(), ref);
            } catch (XMLStreamException e) {
                LOG.warn(e.getMessage(), e);
            }
        } else {
            if (getAttributeValue(inputReader) != null)
                writer.argumentValue(getAttributeValue(inputReader), ref);
        }
    }

    private void endStatement(StatementSourceReference ref) {
        writer.endStatement(ref);
    }

    private static String getAttributeValue(XMLStreamReader inputReader) {
        String namespace = null;
        if (inputReader.getAttributeValue(namespace, "name") != null) {
            return inputReader.getAttributeValue(namespace, "name");
        } else if (inputReader.getAttributeValue(namespace, "value") != null) {
            return inputReader.getAttributeValue(namespace, "value");
        } else if (inputReader.getAttributeValue(namespace, "text") != null) {
            return inputReader.getAttributeValue(namespace, "text");
        } else if (inputReader.getAttributeValue(namespace, "uri") != null) {
            return inputReader.getAttributeValue(namespace, "uri");
        } else if (inputReader.getAttributeValue(namespace, "date") != null) {
            return inputReader.getAttributeValue(namespace, "date");
        } else if (inputReader.getAttributeValue(namespace, "target-node") != null) {
            return inputReader.getAttributeValue(namespace, "target-node");
        } else if (inputReader.getAttributeValue(namespace, "module") != null) {
            return inputReader.getAttributeValue(namespace, "module");
        } else if (inputReader.getAttributeValue(namespace, "condition") != null) {
            return inputReader.getAttributeValue(namespace, "condition");
        } else if (inputReader.getAttributeValue(namespace, "tag") != null) {
            return inputReader.getAttributeValue(namespace, "tag");
        } else {
            return null;
        }

    }

    private static String getElementFullName(XMLStreamReader inputReader) {
        if (!inputReader.getPrefix().isEmpty()) {
            return inputReader.getPrefix() + ":" + inputReader.getLocalName();
        } else {
            return inputReader.getLocalName();
        }
    }

    private static boolean isStatementWithArgument(QName identifier) {
        if (NO_ARGUMENT_ELEMENTS.contains(identifier.getLocalName())) {
            return false;
        }
        return true;
    }

    private static boolean isStatementWithYinElement(QName identifier) {
        if (YIN_ELEMENTS.contains(identifier.getLocalName())) {
            return true;
        }
        return false;
    }
}