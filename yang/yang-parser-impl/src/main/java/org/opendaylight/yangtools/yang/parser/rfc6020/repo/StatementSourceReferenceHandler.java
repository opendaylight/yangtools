/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc6020.repo;

import com.google.common.base.Preconditions;
import java.util.ArrayDeque;
import java.util.Deque;
import org.opendaylight.yangtools.yang.parser.spi.source.DeclarationInTextSource;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

// adapted from https://stackoverflow.com/questions/4915422/get-line-number-from-xml-node-java
final class StatementSourceReferenceHandler extends DefaultHandler {
    private static final Logger LOG = LoggerFactory.getLogger(StatementSourceReferenceHandler.class);
    private static final String USER_DATA_KEY = StatementSourceReference.class.getName();

    private final Deque<Element> stack = new ArrayDeque<>();
    private final StringBuilder sb = new StringBuilder();
    private final Document doc;
    private final String file;

    private Locator locator;

    StatementSourceReferenceHandler(final Document doc, final String file) {
        this.doc = Preconditions.checkNotNull(doc);
        this.file = file;
    }

    static StatementSourceReference extractRef(final Element element) {
        final Object value = element.getUserData(USER_DATA_KEY);
        if (value instanceof StatementSourceReference) {
            return (StatementSourceReference) value;
        }
        if (value != null) {
            LOG.debug("Ignoring {} attached to key {}", value, USER_DATA_KEY);
        }
        return null;
    }

    @Override
    public void setDocumentLocator(final Locator locator) {
        // Save the locator, so that it can be used later for line tracking when traversing nodes.
        this.locator = locator;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public void startElement(final String uri, final String localName, final String qName,
            final Attributes attributes) {
        addTextIfNeeded();
        final Element el = doc.createElementNS(uri, qName);
        for (int i = 0, len = attributes.getLength(); i < len; i++) {
            el.setAttributeNS(attributes.getURI(i), attributes.getQName(i), attributes.getValue(i));
        }

        final StatementSourceReference ref = DeclarationInTextSource.atPosition(file, locator.getLineNumber(),
            locator.getColumnNumber());
        el.setUserData(USER_DATA_KEY, ref, null);
        stack.push(el);
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) {
        addTextIfNeeded();
        final Element closedEl = stack.pop();
        Node parentEl = stack.peek();
        if (parentEl == null) {
            // root element
            parentEl = doc;
        }

        parentEl.appendChild(closedEl);
    }

    @Override
    public void characters(final char ch[], final int start, final int length) throws SAXException {
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
