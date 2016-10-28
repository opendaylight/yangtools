/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.xml;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;

/**
 * Set of utility methods for instantiating parser that deal with untrusted XML sources.
 *
 * @author Robert Varga
 */
@Beta
public final class UntrustedXML {
    private static final DocumentBuilderFactory DBF;
    static {
        final DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setCoalescing(true);
        f.setExpandEntityReferences(false);
        f.setIgnoringElementContentWhitespace(true);
        f.setIgnoringComments(true);
        f.setNamespaceAware(true);
        f.setXIncludeAware(false);
        try {
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setFeature("http://xml.org/sax/features/external-general-entities", false);
            f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (final ParserConfigurationException e) {
            throw new ExceptionInInitializerError(e);
        }
        DBF = f;
    }

    private static final SAXParserFactory SPF;
    static {
        final SAXParserFactory f = SAXParserFactory.newInstance();
        f.setNamespaceAware(true);
        f.setXIncludeAware(false);
        try {
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setFeature("http://xml.org/sax/features/external-general-entities", false);
            f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (final Exception e) {
            throw new ExceptionInInitializerError(e);
        }

        SPF = f;
    }

    /**
     * Create a new {@link DocumentBuilder} for dealing with untrusted XML data. This method is equivalent to
     * {@link DocumentBuilderFactory#newDocumentBuilder()}, except it does not throw a checked exception.
     *
     * @return A new DocumentBuilder
     * @throws UnsupportedOperationException if the runtime fails to instantiate a good enough builder
     */
    public static @Nonnull DocumentBuilder newDocumentBuilder() {
        try {
            return DBF.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new UnsupportedOperationException("Failed to instantiate a DocumentBuilder", e);
        }
    }

    /**
     * Create a new {@link SAXParser} for dealing with untrusted XML data. This method is equivalent to
     * {@link SAXParserFactory#newSAXParser()}, except it does not throw a checked exception.
     *
     * @return A new SAXParser
     * @throws UnsupportedOperationException if the runtime fails to instantiate a good enough builder
     */
    public static @Nonnull SAXParser newSAXParser() {
        try {
            return SPF.newSAXParser();
        } catch (ParserConfigurationException | SAXException e) {
            throw new UnsupportedOperationException("Failed to instantiate a SAXParser", e);
        }
    }
}
