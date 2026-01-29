/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yin.source.dom;

import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import org.opendaylight.yangtools.yang.model.spi.meta.StatementDeclarations;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

// adapted from https://stackoverflow.com/questions/4915422/get-line-number-from-xml-node-java
final class SourceRefHandler extends DefaultHandler {
    private final ArrayDeque<Element> stack = new ArrayDeque<>();
    private final StringBuilder sb = new StringBuilder();
    private final Document doc;
    private final String file;

    private Locator documentLocator;

    SourceRefHandler(final Document doc, final String file) {
        this.doc = requireNonNull(doc);
        this.file = file;
    }

    @Override
    public void setDocumentLocator(final Locator locator) {
        // Save the locator, so that it can be used later for line tracking when traversing nodes.
        documentLocator = locator;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public void startElement(final String uri, final String localName, final String qName,
            final Attributes attributes) {
        addTextIfNeeded();
        final var element = doc.createElementNS(uri, qName);
        for (int i = 0, len = attributes.getLength(); i < len; i++) {
            element.setAttributeNS(attributes.getURI(i), attributes.getQName(i), attributes.getValue(i));
        }

        DefaultSourceRefProvider.setSourceRef(element,
            StatementDeclarations.inText(file, documentLocator.getLineNumber(), documentLocator.getColumnNumber()));
        stack.push(element);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public void endElement(final String uri, final String localName, final String qName) {
        addTextIfNeeded();
        final var closedElement = stack.pop();
        Node parentNode = stack.peek();
        if (parentNode == null) {
            // root element
            parentNode = doc;
        }

        parentNode.appendChild(closedElement);
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        sb.append(ch, start, length);
    }

    // Outputs text accumulated under the current node
    private void addTextIfNeeded() {
        if (sb.length() > 0) {
            stack.peek().appendChild(doc.createTextNode(sb.toString()));
            sb.setLength(0);
        }
    }
}
