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
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YinStatementParserImpl {

    private static final Logger LOG = LoggerFactory.getLogger(YinStatementParserImpl.class);

    private final List<String> toBeSkipped = new ArrayList<>();
    private final String sourceName;
    private StatementWriter writer;
    private QNameToStatementDefinition stmtDef;
    private PrefixToModule prefixes;
    private String uriStr;
    private boolean isType = false;
    private boolean action = true;
    private boolean yinElement = false;

    public YinStatementParserImpl(final String sourceName) {
        this.sourceName = Preconditions.checkNotNull(sourceName);
    }

    /**
     *
     * This method is supposed to be called in pre-linkage phase, when YinStatementParserImpl instance has already been
     * created.
     * When done, start walking through YIN source
     *
     * @param writer - instance of StatementWriter to emit declared statements
     * @param stmtDef - map of valid statement definitions for linkage phase
     *
     */
    public void setAttributes(final StatementWriter writer, final QNameToStatementDefinition stmtDef) {
        this.writer = writer;
        this.stmtDef = stmtDef;
    }

    /**
     * This method is supposed to be called in any phase but pre-linkage, when YinStatementParserImpl instance has already
     * been created.
     * When done, start walking through YIN source
     *
     * @param writer - instance of StatementWriter to emit declared statements
     * @param stmtDef - map of valid statement definitions for any phase but linkage
     * @param prefixes - map of valid prefixes for any phase but linkage
     *
     */
    public void setAttributes(final StatementWriter writer, final QNameToStatementDefinition stmtDef, final PrefixToModule prefixes) {
        this.writer = writer;
        this.stmtDef = stmtDef;
        this.prefixes = prefixes;
    }

    /**
     * This method executes parsing YIN source and emitting declared statements via attached StatementWriter
     *
     * @param inputReader - instance of XMlStreamReader, allows forward, read-only access to XML.
     */
    public void walk(final XMLStreamReader inputReader) {
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
        } catch (final XMLStreamException e) {
            LOG.warn("Fatal error detecting the next state of XMLStreamReader", e);
        } catch (final URISyntaxException e) {
            LOG.warn("Given string {} violates RFC2396", uriStr, e);
        }
    }

    private void startStatement(final QName identifier, final StatementSourceReference ref) {
        writer.startStatement(identifier, ref);
    }

    private void argumentValue(final XMLStreamReader inputReader, final StatementSourceReference ref, final QName identifier, final boolean
            yinElement) {
        if (yinElement) {
            writeTextOnlyElement(inputReader, ref);
        } else {
            writeNormalizedAttributeValue(inputReader, identifier, ref);
        }
    }

    private void endStatement(final StatementSourceReference ref) {
        writer.endStatement(ref);
    }

    private void enterStatement(final XMLStreamReader inputReader) throws URISyntaxException {
        final StatementSourceReference ref = DeclarationInTextSource.atPosition(sourceName, inputReader
                .getLocation().getLineNumber(), inputReader.getLocation().getColumnNumber());
        uriStr = inputReader.getNamespaceURI();
        final URI namespace = new URI(uriStr);
        final String elementFullName = getElementFullName(inputReader);
        final QName identifier = new QName(namespace, Utils.trimPrefix(elementFullName));
        if (yinElement && toBeSkipped.isEmpty()) {
            //at yin element, it has be read as argument
            argumentValue(inputReader, ref, identifier, true);
        } else {
            if (isStatementWithYinElement(identifier, stmtDef)) {
                //at statement with yin element, so next statement will be read as argument
                yinElement = true;
            }

            final QName validStatementDefinition = Utils.getValidStatementDefinition(prefixes, stmtDef, elementFullName, namespace);

            //main part -> valid statement for actual phase
            if (stmtDef != null && validStatementDefinition != null && toBeSkipped.isEmpty()) {
                if (identifier.equals(Rfc6020Mapping.TYPE.getStatementName())) {
                    isType = true;
                } else {
                    startStatement(validStatementDefinition, ref);
                    if (isStatementWithYinElement(identifier, stmtDef)) {
                        action = false;
                    }
                }
            } else {
                //if statement not found through all phases, throw exception
                SourceException.throwIf(writer.getPhase().equals(ModelProcessingPhase.FULL_DECLARATION), ref,
                    "%s is not a YIN statement or use of extension.", identifier.getLocalName());

                //otherwise skip it (statement not to be read yet)
                action = false;
                toBeSkipped.add(getElementFullName(inputReader));
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

    private void exitStatement(final XMLStreamReader inputReader) throws URISyntaxException {
        final String statementName = getElementFullName(inputReader);
        final StatementSourceReference ref = DeclarationInTextSource.atPosition(sourceName, inputReader.getLocation()
                .getLineNumber(), inputReader.getLocation().getColumnNumber());
        final QName validStatementDefinition = Utils.getValidStatementDefinition(prefixes, stmtDef, statementName,
                new URI(inputReader.getNamespaceURI()));

        if ((stmtDef != null && validStatementDefinition != null && toBeSkipped.isEmpty()) && !yinElement) {
            endStatement(ref);
        }

        // back to normal mode
        if (yinElement) {
            yinElement = false;
        }

        if (toBeSkipped.contains(statementName)) {
            toBeSkipped.remove(statementName);
        }
    }

    private void writeTextOnlyElement(final XMLStreamReader inputReader, final StatementSourceReference ref) {
        try {
            writer.argumentValue(inputReader.getElementText(), ref);
        } catch (final XMLStreamException e) {
            LOG.warn("Current event is not a START_ELEMENT or a non text element is encountered ", ref, e);
        }
    }

    private void writeNormalizedAttributeValue(final XMLStreamReader inputReader, final QName
            identifier, final StatementSourceReference ref) {
        final String attributeValue = getAttributeValue(inputReader, identifier, stmtDef);
        if (attributeValue != null) {
            writer.argumentValue(attributeValue, ref);
        }
    }

    private void writeTypeStmtAndArg(final XMLStreamReader inputReader, final QName identifier, final StatementSourceReference ref) {
        final String argument = getAttributeValue(inputReader, identifier, stmtDef);
        if (TypeUtils.isYangTypeBodyStmtString(argument)) {
            startStatement(new QName(YangConstants.RFC6020_YIN_NAMESPACE, argument), ref);
        } else {
            startStatement(new QName(YangConstants.RFC6020_YIN_NAMESPACE, Rfc6020Mapping
                    .TYPE.getStatementName().getLocalName()), ref);
        }
        argumentValue(inputReader, ref, identifier, false);
        isType = false;
    }

    private static String getElementFullName(final XMLStreamReader inputReader) {
        if (!inputReader.getPrefix().isEmpty()) {
            return inputReader.getPrefix() + ":" + inputReader.getLocalName();
        } else {
            return inputReader.getLocalName();
        }
    }

    private boolean isStatementWithArgument(final QName identifier, final QNameToStatementDefinition stmtDef) {
        final StatementDefinition statementDefinition = getStatementDefinition(identifier, stmtDef);
        if (statementDefinition == null) {
            return false;
        } else if (((StatementSupport<?, ?, ?>) statementDefinition).getPublicView().getArgumentName() == null) {
            return false;
        }
        return true;
    }

    private boolean isStatementWithYinElement(final QName identifier, final QNameToStatementDefinition stmtDef) {
        final StatementDefinition statementDefinition = getStatementDefinition(identifier, stmtDef);
        if (statementDefinition == null) {
            return false;
        }
        return statementDefinition.isArgumentYinElement();
    }

    private String getAttributeValue(final XMLStreamReader inputReader, final QName identifier, final QNameToStatementDefinition
            stmtDef) {
        final String namespace = null;
        return inputReader.getAttributeValue(namespace, (((StatementSupport<?, ?, ?>) getStatementDefinition(identifier, stmtDef))
                .getPublicView()).getArgumentName().getLocalName());
    }

    private StatementDefinition getStatementDefinition(final QName identifier, final QNameToStatementDefinition stmtDef) {
        return stmtDef.getByNamespaceAndLocalName(identifier.getNamespace(),
                identifier.getLocalName());
    }
}