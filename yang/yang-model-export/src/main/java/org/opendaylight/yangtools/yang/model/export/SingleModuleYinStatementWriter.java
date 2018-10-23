/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.concurrent.NotThreadSafe;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

@Deprecated
@Beta
@NotThreadSafe
final class SingleModuleYinStatementWriter implements StatementTextWriter {

    private final XMLStreamWriter writer;
    private final URI currentModuleNs;
    private final BiMap<String, URI> prefixToNamespace;
    private StatementDefinition currentStatement;

    private SingleModuleYinStatementWriter(final XMLStreamWriter writer, final URI moduleNamespace,
            final Map<String, URI> prefixToNs) {
        this.writer = writer;
        this.currentModuleNs = moduleNamespace;
        this.prefixToNamespace = HashBiMap.create(prefixToNs);
        initializeYinNamespaceInXml();
    }

    private void initializeYinNamespaceInXml() {
        try {
            final String defaultNs = writer.getNamespaceContext().getNamespaceURI(XMLConstants.NULL_NS_URI);
            if (defaultNs == null) {
                writer.setDefaultNamespace(YangConstants.RFC6020_YIN_NAMESPACE.toString());
            } else if (!YangConstants.RFC6020_YIN_NAMESPACE.toString().equals(defaultNs)) {
                // FIXME: Implement support for exporting YIN as part of other XML document.
                throw new UnsupportedOperationException(
                        "Not implemented support for nesting YIN in different XML element.");
            }
        } catch (final XMLStreamException e) {
            throw new IllegalStateException(e);
        }
    }

    static StatementTextWriter create(final XMLStreamWriter writer, final URI moduleNs,
            final Map<String, URI> prefixToNs) {
        return new SingleModuleYinStatementWriter(writer, moduleNs, prefixToNs);
    }

    @Override
    public void startStatement(final StatementDefinition statement) {
        currentStatement = requireNonNull(statement);
        try {
            writeStartXmlElement(statement.getStatementName());
            if (YangStmtMapping.MODULE.equals(statement) || YangStmtMapping.SUBMODULE.equals(statement)) {
                declareXmlNamespaces();
            }
        } catch (final XMLStreamException e) {
            // FIXME: Introduce proper expression
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void endStatement() {
        currentStatement = null;
        try {
            writeXmlEndElement();
        } catch (final XMLStreamException e) {
            // FIXME: Introduce proper expression
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void writeArgument(final String strRep) {
        checkArgumentApplicable();
        writeArgument0(strRep);
    }

    @Override
    public void writeArgument(final QName value) {
        checkArgumentApplicable();
        final String valueStr = toPrefixedString(value);
        writeArgument0(valueStr);
    }

    @Override
    public void writeArgument(final SchemaPath targetPath) {
        checkArgumentApplicable();
        final StringBuilder valueStr = new StringBuilder();
        if (targetPath.isAbsolute()) {
            valueStr.append("/");
        }
        final Iterator<QName> argIt = targetPath.getPathFromRoot().iterator();
        while (argIt.hasNext()) {
            valueStr.append(toPrefixedString(argIt.next()));
            if (argIt.hasNext()) {
                valueStr.append("/");
            }
        }
        writeArgument0(valueStr.toString());
    }

    @Override
    public void writeArgument(final RevisionAwareXPath xpath) {
        checkArgumentApplicable();
        // FIXME: This implementation assumes prefixes are unchanged
        // and were not changed in schema context.
        writeArgument0(xpath.toString());
    }

    private void writeArgument0(final String strRep) {
        try {
            if (isArgumentYinElement(currentStatement)) {
                writeStartXmlElement(currentStatement.getArgumentName());
                writeXmlText(strRep);
                writeXmlEndElement();
            } else {
                writeXmlArgument(currentStatement.getArgumentName(), strRep);
            }
        } catch (final XMLStreamException e) {
            // FIXME: throw proper exception
            throw new IllegalStateException(e);
        }
    }

    private static boolean isArgumentYinElement(final StatementDefinition currentStatement) {
        if (currentStatement instanceof YangStmtMapping || currentStatement instanceof ExtensionStatement) {
            return currentStatement.isArgumentYinElement();
        }
        return false;
    }

    private void checkArgumentApplicable() {
        checkState(currentStatement != null, "No statement is opened.");
        checkState(currentStatement.getArgumentName() != null, "Statement %s does not take argument.",
                currentStatement.getArgumentName());
    }

    private static String toPrefixedString(final @Nullable String prefix, final String localName) {
        if (prefix == null || prefix.isEmpty()) {
            return localName;
        }
        return prefix + ":" + localName;
    }

    private String toPrefixedString(final QName value) {
        final URI valueNs = value.getNamespace();
        final String valueLocal = value.getLocalName();
        if (currentModuleNs.equals(valueNs)) {
            return valueLocal;
        }
        final String prefix = ensureAndGetXmlNamespacePrefix(valueNs);
        return toPrefixedString(prefix, valueLocal);
    }

    private @Nullable String ensureAndGetXmlNamespacePrefix(final URI namespace) {
        if (YangConstants.RFC6020_YANG_NAMESPACE.equals(namespace)) {
         // YANG namespace does not have prefix if used in arguments.
            return null;

        }
        String prefix = writer.getNamespaceContext().getPrefix(namespace.toString());
        if (prefix == null) {
            // FIXME: declare prefix
            prefix = prefixToNamespace.inverse().get(namespace);
        }
        if (prefix == null) {
            throw new IllegalArgumentException("Namespace " + namespace + " is not bound to imported prefixes.");
        }
        return prefix;
    }

    private void writeXmlText(final String strRep) throws XMLStreamException {
        writer.writeCharacters(strRep);
    }

    private void declareXmlNamespaces() {
        try {
            writer.writeDefaultNamespace(YangConstants.RFC6020_YIN_NAMESPACE.toString());
            for (final Entry<String, URI> nsDeclaration : prefixToNamespace.entrySet()) {
                writer.writeNamespace(nsDeclaration.getKey(), nsDeclaration.getValue().toString());
            }
        } catch (final XMLStreamException e) {
            throw new IllegalStateException(e);
        }
    }

    private void writeXmlEndElement() throws XMLStreamException {
        writer.writeEndElement();
    }

    private void writeXmlArgument(final QName qname, final String value) throws XMLStreamException {
        writer.writeAttribute(qname.getNamespace().toString(), qname.getLocalName(), value);
    }

    private void writeStartXmlElement(final QName name) throws XMLStreamException {
        writer.writeStartElement(name.getNamespace().toString(), name.getLocalName());
    }
}
