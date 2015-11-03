/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.impl;

import com.google.common.base.Preconditions;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.source.DeclarationInTextSource;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YinStatementParserImpl {

    private static final Logger LOG = LoggerFactory.getLogger(YinStatementParserImpl.class);

    private StatementWriter writer;
    private QNameToStatementDefinition stmtDef;
    private PrefixToModule prefixes;
    private final String sourceName;
    private String uriStr;
    private boolean isType = false;
    private boolean action = true;
    private boolean yinElement = false;
    private List<String> toBeSkipped = new ArrayList<>();

    public YinStatementParserImpl(String sourceName) {
        this.sourceName = Preconditions.checkNotNull(sourceName);
    }

    /**
     *
     * This method is supposed to be called in linkage phase, when YinStatementParserImpl instance has already been
     * created.
     * When done, start walking through YIN source
     *
     *
     * @param writer - instance of StatementWriter to emit declared statements
     * @param stmtDef - map of valid statement definitions for linkage phase
     *
     */
    public void setAttributes(StatementWriter writer, QNameToStatementDefinition stmtDef) {
        this.writer = writer;
        this.stmtDef = stmtDef;
    }

    /**
     *
     * This method is supposed to be called in any phase but linkage, when YinStatementParserImpl instance has already
     * been created.
     * When done, start walking through YIN source
     *
     * @param writer - instance of StatementWriter to emit declared statements
     * @param stmtDef - map of valid statement definitions for any phase but linkage
     * @param prefixes - map of valid prefixes for any phase but linkage
     *
     */
    public void setAttributes(StatementWriter writer, QNameToStatementDefinition stmtDef, PrefixToModule prefixes) {
        this.writer = writer;
        this.stmtDef = stmtDef;
        this.prefixes = prefixes;
    }

    /**
     *
     * This method executes parsing YIN source and emitting declared statements via attached StatementWriter
     *
     * @param inputReader - instance of XMlStreamReader, allows forward, read-only access to XML.
     *
     */
    public void walk(XMLStreamReader inputReader) {
        try {
            while (inputReader.hasNext()) {
                inputReader.next();
                if (inputReader.hasName() && inputReader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    enterStatement(inputReader);
                }

                if (inputReader.hasName() && inputReader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                    exitStatement(inputReader);
                }
            }
        } catch (XMLStreamException e) {
            LOG.warn("Fatal error detecting the next state of XMLStreamReader", e);
        } catch (URISyntaxException e) {
            LOG.warn("Given string {} violates RFC2396", uriStr, e);
        }
    }

    private void startStatement(QName identifier, StatementSourceReference ref) {
        writer.startStatement(identifier, ref);
    }

    private void argumentValue(XMLStreamReader inputReader, StatementSourceReference ref, QName identifier, boolean
            yinElement) {
        if (yinElement) {
            writeTextOnlyElement(inputReader, ref);
        } else {
            writeNormalizedAttributeValue(inputReader, identifier, ref);
        }
    }

    private void endStatement(StatementSourceReference ref) {
        writer.endStatement(ref);
    }

    private void enterStatement(XMLStreamReader inputReader) throws URISyntaxException {
        final StatementSourceReference ref = DeclarationInTextSource.atPosition(sourceName, inputReader
                .getLocation().getLineNumber(), inputReader.getLocation().getColumnNumber());
        uriStr = inputReader.getNamespaceURI();
        final QName identifier = new QName(new URI(uriStr), getElementFullName(inputReader));
        if (yinElement && toBeSkipped.isEmpty()) {
            //at yin element, it has be read as argument
            argumentValue(inputReader, ref, identifier, true);
        } else {
            if (isStatementWithYinElement(identifier, stmtDef)) {
                //at statement with yin element, so next statement will be read as argument
                yinElement = true;
            }

            final QName validStatementDefinition = Utils.getValidStatementDefinition(prefixes, stmtDef, identifier);

            //main part -> valid statement for actual phase
            if (stmtDef != null && validStatementDefinition != null && toBeSkipped.isEmpty()) {
                if (identifier.equals(Rfc6020Mapping.TYPE.getStatementName())) {
                    isType = true;
                } else {
                    startStatement(QName.create(validStatementDefinition.getModule(), getElementFullName
                            (inputReader)),  ref);
                    if (isStatementWithYinElement(identifier, stmtDef)) {
                        action = false;
                    }
                }
            } else {
                //if statement not found through all phases, throw exception
                if (writer.getPhase().equals(ModelProcessingPhase.FULL_DECLARATION)) {
                    throw new IllegalArgumentException(identifier.getLocalName() + " is not a YIN " +
                            "statement or use of extension. Source: " + ref);
                } else {
                    //otherwise skip it (statement not to be read yet)
                    action = false;
                    toBeSkipped.add(getElementFullName(inputReader));
                }
            }

            if (isType) {
                writeTypeStmtAndArg(inputReader, identifier, ref);
            } else if (action & isStatementWithArgument(identifier, stmtDef)) {
                argumentValue(inputReader, ref, identifier, false);
            } else {
                action = true;
            }
        }
    }

    private void exitStatement(XMLStreamReader inputReader) throws URISyntaxException {
        final String statementName = getElementFullName(inputReader);
        final QName identifier = new QName(new URI(inputReader.getNamespaceURI()), statementName);
        final StatementSourceReference ref = DeclarationInTextSource.atPosition(sourceName, inputReader
                .getLocation().getLineNumber(), inputReader.getLocation().getColumnNumber());
        final QName validStatementDefinition = Utils.getValidStatementDefinition(prefixes, stmtDef, identifier);

        if ((stmtDef != null && validStatementDefinition != null && toBeSkipped.isEmpty()) && !yinElement) {
            endStatement(ref);
        }

        //back to normal mode
        if (yinElement) {
            yinElement = false;
        }

        if (toBeSkipped.contains(statementName)) {
            toBeSkipped.remove(statementName);
        }
    }

    private void writeTextOnlyElement(XMLStreamReader inputReader, StatementSourceReference ref) {
        try {
            writer.argumentValue(inputReader.getElementText(), ref);
        } catch (XMLStreamException e) {
            LOG.warn("Current event is not a START_ELEMENT or a non text element is encountered ", ref, e);
        }
    }

    private void writeNormalizedAttributeValue(XMLStreamReader inputReader, QName
            identifier, StatementSourceReference ref) {
        final String attributeValue = getAttributeValue(inputReader, identifier, stmtDef);
        if (attributeValue != null)
            writer.argumentValue(attributeValue, ref);
    }

    private void writeTypeStmtAndArg(XMLStreamReader inputReader, QName identifier, StatementSourceReference ref) {
        String argument = getAttributeValue(inputReader, identifier, stmtDef);
        if (TypeUtils.isYangTypeBodyStmtString(argument)) {
            startStatement(new QName(YangConstants.RFC6020_YIN_NAMESPACE, argument), ref);
        } else {
            startStatement(new QName(YangConstants.RFC6020_YIN_NAMESPACE, Rfc6020Mapping
                    .TYPE.getStatementName().getLocalName()), ref);
        }
        argumentValue(inputReader, ref, identifier, false);
        isType = false;
    }

    private static String getElementFullName(XMLStreamReader inputReader) {
        if (!inputReader.getPrefix().isEmpty()) {
            return inputReader.getPrefix() + ":" + inputReader.getLocalName();
        } else {
            return inputReader.getLocalName();
        }
    }

    private static boolean isStatementWithArgument(QName identifier, QNameToStatementDefinition stmtDef) {
        if (stmtDef != null && stmtDef.get(Utils.trimPrefix(identifier)) == null) {
            return false;
        } else if (((StatementSupport) stmtDef.get(Utils.trimPrefix(identifier)))
                .getPublicView().getArgumentName() == null) {
            return false;
        }
        return true;
    }

    private static boolean isStatementWithYinElement(QName identifier, QNameToStatementDefinition stmtDef) {
        final StatementDefinition statementDefinition = stmtDef.get(Utils.trimPrefix(identifier));
        if (statementDefinition != null) {
            return ((Rfc6020Mapping) ((StatementSupport) statementDefinition).getPublicView()).isArgumentYinElement();
        } else {
            return false;
        }
    }

    private static String getAttributeValue(XMLStreamReader inputReader, QName identifier, QNameToStatementDefinition
            stmtDef) {
        String namespace = null;
        return inputReader.getAttributeValue(namespace, (((StatementSupport) stmtDef.get(Utils.trimPrefix(identifier)))
                .getPublicView()).getArgumentName().getLocalName());
    }
}